package request_logger_store.config;

import io.github.resilience4j.retry.Retry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryConfig {
    @Bean
    public Retry kafkaProducerRetry() {
        return RetryProvider.getKafkaProducerRetry();
    }

    @Bean("kafkaConsumerRetry")
    public Retry kafkaConsumerRetry() {
        return RetryProvider.getDatabaseSaveRetry();
    }

}
