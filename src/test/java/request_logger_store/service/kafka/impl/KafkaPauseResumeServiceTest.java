package request_logger_store.service.kafka.impl;

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class KafkaPauseResumeServiceTest {

    @Mock
    private KafkaListenerEndpointRegistry registry;

    @Mock
    private MessageListenerContainer container;

    @InjectMocks
    private KafkaPauseResumeService pauseResumeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(registry.getListenerContainer("myCustomConsumerId")).thenReturn(container);
    }

    @Test
    void testPause_ShouldCallContainerPause() {
        when(container.isPauseRequested()).thenReturn(false);

        pauseResumeService.pause();

        verify(container, times(1)).pause();
    }

    @Test
    void testResume_ShouldCallContainerResume() {
        when(container.isPauseRequested()).thenReturn(true);

        pauseResumeService.resume();

        verify(container, times(1)).resume();
    }

    @Test
    void testIsPaused_ShouldReturnTrue_WhenPaused() {
        when(container.isPauseRequested()).thenReturn(true);

        boolean result = pauseResumeService.isPaused();

        assertTrue(result);
    }
}