package request_logger_store.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import request_logger_store.Dto.EventListDto;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;

class KafkaProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private Retry retry;

    @Mock
    private ExecutorService executor;

    @InjectMocks
    private KafkaProducer kafkaProducer;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        kafkaProducer = new KafkaProducer(retry, kafkaTemplate, executor);
    }

    @Test
    void testSendEventToBroker_ShouldSerializeAndSend() throws Exception {
        EventListDto dto = new EventListDto(); // предположим, что есть конструктор или сеттеры
        ProducerRecord<String, String> record = new ProducerRecord<>("new-event-topic", "key", mapper.writeValueAsString(dto));

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));

        kafkaProducer.sendEventToBroker(dto);

        verify(executor, times(1)).submit(any(Runnable.class));
    }

}