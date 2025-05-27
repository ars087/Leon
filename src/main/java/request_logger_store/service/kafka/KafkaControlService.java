package request_logger_store.service.kafka;

public interface KafkaControlService {
    void pause();
    void resume();
    boolean isPaused();
}
