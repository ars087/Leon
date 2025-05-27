package request_logger_store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableKafka
public class RequestLoggerStoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(RequestLoggerStoreApplication.class, args);
    }
}
