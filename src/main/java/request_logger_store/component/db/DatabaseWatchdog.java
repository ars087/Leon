package request_logger_store.component.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import request_logger_store.service.kafka.impl.KafkaPauseResumeService;

/**
 * Компонент-наблюдатель за состоянием базы данных.
 * <p>
 * Следит за доступностью БД в фоновом потоке и возобновляет работу Kafka-потребителя,
 * если соединение с БД было восстановлено.
 */
@Component
public class DatabaseWatchdog {

    private final KafkaPauseResumeService pauseResumeService;
    private final DatabaseConnectionService databaseConnectionService;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseWatchdog.class);

    /**
     * Конструктор для создания экземпляра DatabaseWatchdog.
     *
     * @param pauseResumeService        Сервис для управления состоянием Kafka-потребителя (пауза/возобновление)
     * @param databaseConnectionService Сервис для проверки доступности базы данных
     */
    public DatabaseWatchdog(
        KafkaPauseResumeService pauseResumeService,
        DatabaseConnectionService databaseConnectionService) {
        this.pauseResumeService = pauseResumeService;
        this.databaseConnectionService = databaseConnectionService;
    }

    /**
     * Запускает фоновый поток, который периодически проверяет:
     * <ul>
     *     <li>Приостановлен ли Kafka-потребитель</li>
     *     <li>Доступна ли база данных</li>
     * </ul>
     * Если потребитель на паузе, а БД снова доступна — возобновляет его работу.
     */
    public void checkAndResumeIfPossible() {
        logger.info(">>>>>>>DatabaseWatchdog");
        new Thread(() -> {
            while (true) {
                try {
                    logger.info(">>>>>>>Trying to connect");
                    Thread.sleep(5000);
                    if (pauseResumeService.isPaused() && databaseConnectionService.isDatabaseAvailable()) {
                        pauseResumeService.resume();
                        logger.warn("<<<<<<<<consumer start");
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.error(">>>>>>>>>>Error>>{}", e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}