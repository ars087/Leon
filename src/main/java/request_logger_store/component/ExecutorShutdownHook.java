package request_logger_store.component;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

/**
 * Компонент для корректного завершения работы {@link ExecutorService} при выключении Spring-контекста.
 * <p>
 * Использует аннотацию {@link PreDestroy}, чтобы остановить пул потоков перед завершением работы приложения,
 * предотвращая утечки ресурсов и обеспечивая корректное завершение задач.
 */
@Component
public class ExecutorShutdownHook {

    private final ExecutorService executor;

    /**
     * Конструктор для инъекции пула потоков.
     *
     * @param executor Пул потоков, который должен быть остановлен при завершении работы приложения
     */
    public ExecutorShutdownHook(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Метод, вызываемый перед уничтожением бина.
     * <p>
     * Прерывает выполнение всех задач и освобождает ресурсы, связанные с пулом потоков.
     * Используется для корректного завершения работы приложения.
     */
    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}