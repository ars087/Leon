package request_logger_store.component.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Сервис для проверки доступности базы данных.
 * <p>
 * Предоставляет методы для проверки соединения с БД, что может быть использовано,
 * например, для определения необходимости приостановки Kafka-потребителя при недоступности БД.
 */
@Component
public class DatabaseConnectionService {

    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionService.class);

    /**
     * Конструктор для создания экземпляра DatabaseConnectionService.
     *
     * @param dataSource Источник данных, используемый для подключения к базе данных
     */
    public DatabaseConnectionService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Проверяет, доступна ли база данных, пытаясь установить соединение.
     *
     * @return true, если соединение установлено и не закрыто, иначе false
     */
    public boolean isDatabaseAvailable() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null && !connection.isClosed()) {
                logger.warn(">>>>>>Подключение восстановлено");
                return true;
            }
        } catch (SQLException e) {
            logger.error(">>>>>>Ошибка подключения к БД{}", e.getMessage());
        }
        return false;
    }
}