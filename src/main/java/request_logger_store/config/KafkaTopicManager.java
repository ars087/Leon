package request_logger_store.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicManager {
    @Bean
    public NewTopic ordersTopic() {
        return new NewTopic("new-event-topic", 3, (short) 1);
    }

}
