package request_logger_store.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.support.Acknowledgment;
import request_logger_store.Dto.EventDto;
import request_logger_store.Dto.EventListDto;
import request_logger_store.component.db.BaseChecking;
import request_logger_store.config.mapper.Mapper;
import request_logger_store.model.Event;
import request_logger_store.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.mockito.Mockito.*;

class KafkaConsumerTest {

    @Mock
    private Mapper mapper;

    @Mock
    private EventRepository repository;

    @Mock
    private Retry retry;

    @Mock
    private BaseChecking baseChecking;

    @InjectMocks
    private KafkaConsumer kafkaConsumer;

    @Mock
    private Acknowledgment acknowledgment;

    @Mock
    private ConsumerRecord<String, String> record;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
           }

    @Test
    void testListenOrder_ShouldCallBaseChecking_WhenSaveFails() throws Exception {
        when(record.value()).thenReturn("{\"\": {}}");
        when(record.key()).thenReturn("testKey");

        doThrow(new RuntimeException("DB error")).when(repository).save(any(Event.class));

        kafkaConsumer.listenEvent(record, acknowledgment);

        verify(baseChecking, times(1)).checking();
    }
}