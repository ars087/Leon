package request_logger_store.component.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class DatabaseConnectionServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    private DatabaseConnectionService service;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isClosed()).thenReturn(false);
        service = new DatabaseConnectionService(dataSource);
    }

    @Test
    void testIsDatabaseAvailable_ReturnsTrue_WhenConnectionOk() throws SQLException {
        assertTrue(service.isDatabaseAvailable());
    }

    @Test
    void testIsDatabaseAvailable_ReturnsFalse_WhenSQLException() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        assertFalse(service.isDatabaseAvailable());
    }
}