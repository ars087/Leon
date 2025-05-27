package request_logger_store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {

    @Bean("kafkaEventExecutor")
    public ExecutorService executorService() {
        return Executors.newSingleThreadExecutor();
    }

}
