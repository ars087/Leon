package request_logger_store.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.retry.Retry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import request_logger_store.Dto.EventListDto;
import request_logger_store.component.db.BaseChecking;
import request_logger_store.config.mapper.Mapper;
import request_logger_store.model.Event;
import request_logger_store.repository.EventRepository;

/**
 * Сервис для потребления сообщений из Kafka и последующего их сохранения в базу данных.
 * <p>
 * Сообщения обрабатываются асинхронно с возможностью повторных попыток записи в БД
 * при возникновении ошибок, используя {@link Retry}.
 */
@Service
public class KafkaConsumer {

    private final Mapper mapper;
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private final EventRepository repository;
    private final Retry retrySaveEvent;
    private final BaseChecking baseChecking;

    /**
     * Конструктор для создания экземпляра KafkaConsumer.
     *
     * @param mapper        Объект для преобразования JSON-строки в Java-объекты
     * @param repository    Репозиторий для взаимодействия с базой данных
     * @param retry         Настроенная стратегия повторных попыток для сохранения события
     * @param baseChecking  Компонент для проверки состояния базы данных в случае ошибок
     */
    public KafkaConsumer(
        Mapper mapper,
        EventRepository repository,
        @Qualifier("kafkaConsumerRetry") Retry retry,
        BaseChecking baseChecking) {
        this.mapper = mapper;
        this.repository = repository;
        this.retrySaveEvent = retry;
        this.baseChecking = baseChecking;
    }

    /**
     * Метод-слушатель, который получает сообщения из Kafka по указанному топику.
     * <p>
     * После получения сообщения:
     * - Проверяет, не пустое ли оно.
     * - Десериализует JSON в DTO-объект.
     * - Сохраняет событие в БД с поддержкой повторных попыток.
     * - Подтверждает offset только в случае успешного сохранения.
     * <p>
     * При возникновении ошибки сохранения вызывается проверка состояния БД через {@link BaseChecking#checking()}.
     *
     * @param record         Полученное сообщение из Kafka
     * @param acknowledgment Объект для ручного подтверждения offset'а
     */
    @KafkaListener(
        topics = "new-event-topic",
        groupId = "event-group",
        containerFactory = "manualKafkaListenerContainerFactory",
        id = "myCustomConsumerId"
    )
    public void listenEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        logger.info(">>>>>KafkaConsumer");
        String messageKey = record.key();
        String messageValue = record.value();

        if (!StringUtils.hasText(messageValue)) {
            logger.warn("<<<<Получено пустое сообщение с ключом {}", messageKey);
            return;
        }
        logger.info(">>>>>Сообщение получено из Kafka. Ключ: {}, Содержимое: {}", messageKey, messageValue);
        try {
            Retry.decorateRunnable(retrySaveEvent, () -> {
                String jsonList;
                try {
                    EventListDto eventListDto = mapper.objectMapper().readValue(messageValue, EventListDto.class);
                    jsonList = mapper.objectMapper().writeValueAsString(eventListDto.getEventDto());
                } catch (JsonProcessingException e) {
                    logger.error(">>>>>Ошибка десериализации JSON из Kafka. Ключ: {}, Сообщение: {}", messageKey, messageValue);
                    throw new RuntimeException(e.getMessage());
                }
                repository.save(new Event(jsonList));
            }).run();

            acknowledgment.acknowledge();
            logger.info(">>>>>Offset commit>>>>>DB");

        } catch (Exception e) {
            logger.error(">>>>>Ошибка сохранения в БД, проверяю состояние... {}", e.getMessage());
            baseChecking.checking();
        }
    }
}
