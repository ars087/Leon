package request_logger_store.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.sql.SQLException;
import java.time.Duration;

public class RetryProvider {
    public static int maxAttemptsDatabase = 3;
    private static final Logger logger = LoggerFactory.getLogger(RetryProvider.class);

    public static Retry getKafkaProducerRetry() {
        RetryConfig config = RetryConfig.<Throwable>custom()
            .maxAttempts(5)
            .intervalFunction(IntervalFunction.of(Duration.ofSeconds(1)))
            .retryExceptions(
                RuntimeException.class,
                JsonProcessingException.class
            )
            .build();
        return Retry.of("kafkaProducer", config);
    }

    public static Retry getDatabaseSaveRetry() {
        RetryConfig config = RetryConfig.<Throwable>custom()
            .maxAttempts(maxAttemptsDatabase)
            .waitDuration(Duration.ofSeconds(1))
            .retryExceptions(SQLException.class,
                DataAccessException.class,
                JsonProcessingException.class,
                RuntimeException.class
            )
            .build();

        Retry retry = Retry.of("database", config);

        retry.getEventPublisher()
            .onError(event -> logger.warn(">>>>Retry>>>>> Attempt #{} for '{}' failed, retries left: {}",
                event.getNumberOfRetryAttempts(),
                event.getName(),
                maxAttemptsDatabase - event.getNumberOfRetryAttempts()
            ));
        return retry;
    }
}
