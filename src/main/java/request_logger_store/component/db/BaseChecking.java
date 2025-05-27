package request_logger_store.component.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import request_logger_store.service.kafka.KafkaControlService;

/**
 * Компонент для проверки доступности базы данных и управления состоянием Kafka-потребителя.
 * <p>
 * Если при сохранении данных в БД происходит ошибка, вызывается метод {@link #checking()},
 * который:
 * <ul>
 *     <li>Проверяет доступность базы данных</li>
 *     <li>При необходимости приостанавливает Kafka-потребителя</li>
 *     <li>Запускает проверку восстановления соединения с БД через {@link DatabaseWatchdog}</li>
 * </ul>
 */
@Component
public class BaseChecking {

    private final DatabaseConnectionService databaseConnectionService;
    private final KafkaControlService kafkaControlService;
    private final DatabaseWatchdog databaseWatchdog;
    private static final Logger logger = LoggerFactory.getLogger(BaseChecking.class);

    /**
     * Конструктор для создания экземпляра BaseChecking.
     *
     * @param databaseConnectionService Сервис для проверки доступности базы данных
     * @param kafkaControlService       Сервис для управления состоянием Kafka-потребителя (пауза/возобновление)
     * @param databaseWatchdog          Компонент для отслеживания восстановления соединения с БД
     */
    public BaseChecking(
        DatabaseConnectionService databaseConnectionService,
        KafkaControlService kafkaControlService,
        DatabaseWatchdog databaseWatchdog) {
        this.databaseConnectionService = databaseConnectionService;
        this.kafkaControlService = kafkaControlService;
        this.databaseWatchdog = databaseWatchdog;
    }

    /**
     * Основной метод проверки состояния базы данных.
     * <p>
     * Если база данных недоступна:
     * <ul>
     *     <li>Приостанавливается Kafka-потребитель</li>
     *     <li>Запускается watchdog для отслеживания восстановления подключения</li>
     * </ul>
     */
    public void checking() {
        logger.error(">>>>>>>>> BaseChecking");
        if (!databaseConnectionService.isDatabaseAvailable()) {
            kafkaControlService.pause();
            logger.error("<<<<<<<<<<База данных недоступна, консьюмер установлен на паузу");
            databaseWatchdog.checkAndResumeIfPossible();
        }
    }
}