package request_logger_store.component.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import request_logger_store.service.kafka.impl.KafkaPauseResumeService;

import static org.mockito.Mockito.*;

class BaseCheckingTest {

    @Mock
    private DatabaseConnectionService connectionService;

    @Mock
    private KafkaPauseResumeService pauseService;

    @Mock
    private DatabaseWatchdog watchdog;

    @InjectMocks
    private BaseChecking baseChecking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testChecking_ShouldPauseConsumer_WhenDbUnavailable() {
        when(connectionService.isDatabaseAvailable()).thenReturn(false);

        baseChecking.checking();

        verify(pauseService, times(1)).pause();
        verify(watchdog, times(1)).checkAndResumeIfPossible();
    }

    @Test
    void testChecking_ShouldNotPause_WhenDbAvailable() {
        when(connectionService.isDatabaseAvailable()).thenReturn(true);

        baseChecking.checking();

        verifyNoInteractions(pauseService);
        verifyNoInteractions(watchdog);
    }
}