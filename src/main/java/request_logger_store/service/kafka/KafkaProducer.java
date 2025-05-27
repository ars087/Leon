package request_logger_store.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.retry.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import request_logger_store.Dto.EventListDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Сервис для отправки событий в Kafka с поддержкой сериализации в JSON и повторными попытками.
 * <p>
 * Использует {@link Retry} для автоматического повтора отправки в случае ошибок,
 * а также {@link ExecutorService} для выполнения операций вне основного потока.
 */
@Service
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final Retry retry;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ExecutorService executor;

    /**
     * Конструктор для создания экземпляра KafkaProducer.
     *
     * @param retry        Настроенная стратегия повторных попыток (например, с заданным количеством попыток)
     * @param kafkaTemplate Шаблон Kafka для отправки сообщений
     * @param executor      Пул потоков для асинхронного выполнения задач
     */
    public KafkaProducer(
        @Qualifier("kafkaProducerRetry") Retry retry,
        KafkaTemplate<String, String> kafkaTemplate,
        @Qualifier("kafkaEventExecutor") ExecutorService executor) {
        this.retry = retry;
        this.kafkaTemplate = kafkaTemplate;
        this.executor = executor;
    }

    /**
     * Асинхронно отправляет событие в Kafka после преобразования его в JSON.
     * <p>
     * Если произойдёт ошибка при отправке, будет выполнено несколько повторных попыток
     * согласно настройкам, указанным в {@link Retry}.
     *
     * @param eventDto Объект события, который нужно отправить в Kafka
     */
    public void sendEventToBroker(EventListDto eventDto) {
        logger.info(">>>>>>>>>");
        Runnable task = () -> {
            try {
                String json = mapper.writeValueAsString(eventDto);
                CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("new-event-topic",
                    "new-event-topic-key", json).toCompletableFuture();
                future.thenAccept(result -> {
                    logger.info(">>>>>>>Сообщение отправлено в Kafka");
                    logger.info(">>>>>>>Topic: {}", result.getRecordMetadata().topic());
                    logger.info(">>>>>>>Offset {}", result.getRecordMetadata().offset());
                }).exceptionally(ex -> {
                    logger.error("<<<<<<Не удалось отправить сообщение", ex);
                    return null;
                });

            } catch (JsonProcessingException e) {
                logger.error("<<<<<<<<<<<Ошибка сериализации DTO,{} ", e.getMessage());
            }
        };

        Runnable decoratedTask = Retry.decorateRunnable(retry, task);
        executor.submit(decoratedTask);
    }
}