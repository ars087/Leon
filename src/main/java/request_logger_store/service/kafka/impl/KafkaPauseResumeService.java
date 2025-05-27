package request_logger_store.service.kafka.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Service;
import request_logger_store.service.kafka.KafkaControlService;

/**
 * Реализация сервиса управления состоянием Kafka-потребителя: приостановка и возобновление.
 * <p>
 * Позволяет управлять конкретным Kafka-слушателем по его идентификатору,
 * используя {@link KafkaListenerEndpointRegistry} для получения и контроля контейнера слушателя.
 */
@Service
public class KafkaPauseResumeService implements KafkaControlService {

    private final KafkaListenerEndpointRegistry registry;
    private static final Logger logger = LoggerFactory.getLogger(KafkaPauseResumeService.class);

    /**
     * Конструктор для создания экземпляра KafkaPauseResumeService.
     *
     * @param registry Регистратор Kafka-эндпоинтов, используется для получения контейнеров слушателей
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public KafkaPauseResumeService(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    /**
     * Получает контейнер слушателя Kafka по заранее заданному идентификатору.
     * <p>
     * Используется для управления поведением потребителя (пауза/возобновление).
     *
     * @return Контейнер слушателя Kafka
     * @throws IllegalStateException если контейнер с указанным ID не найден
     */
    private MessageListenerContainer getRequiredContainer() {
        MessageListenerContainer container = registry.getListenerContainer("myCustomConsumerId");
        if (container == null) {
            throw new IllegalStateException("Контейнер Kafka с ID 'myCustomConsumerId' не найден");
        }
        return container;
    }

    /**
     * Приостанавливает работу Kafka-потребителя, если он ещё не был остановлен.
     * <p>
     * После вызова этого метода потребитель перестаёт получать новые сообщения из топика.
     */
    @Override
    public void pause() {
        MessageListenerContainer messageListenerContainer = getRequiredContainer();
        if (!messageListenerContainer.isPauseRequested()) {
            messageListenerContainer.pause();
            logger.warn("[ПАУЗА] Kafka consumer приостановлен");
        }
    }

    /**
     * Возобновляет работу Kafka-потребителя, если он был приостановлен.
     * <p>
     * После вызова этого метода потребитель продолжает получать сообщения из топика.
     */
    @Override
    public void resume() {
        MessageListenerContainer messageListenerContainer = getRequiredContainer();
        if (messageListenerContainer.isPauseRequested()) {
            messageListenerContainer.resume();
            logger.warn("[ВОЗОБНОВЛЕНИЕ] Kafka consumer возобновил работу");
        }
    }

    /**
     * Проверяет, находится ли Kafka-потребитель в состоянии паузы.
     *
     * @return true, если потребитель приостановлен, иначе false
     */
    @Override
    public boolean isPaused() {
        return getRequiredContainer().isPauseRequested();
    }
}