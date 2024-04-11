package tests.hive;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import hive.Server;
import org.junit.jupiter.api.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@TestMethodOrder(MethodOrderer.Random.class)
public class HiveTests {

    public void setUp() {

    }


    public void tearDown() {

    }

    @Test
    @Order(1)
    void setupServer() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        // Submit the server startup task to the executor
        Future<?> serverStartFuture = executorService.submit(() -> {
            try (final Server server = new Server(8080)) {
                // ... (Potentially add logic while the server is running)
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Use Guava's ListenableFuture
        ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executorService);
        ListenableFuture<Boolean> serverRunningFuture = listeningExecutor.submit(serverStartFuture::isDone);

        // Add a listener to the ListenableFuture
        serverRunningFuture.addListener(() -> {
            try {
                Assertions.assertTrue(serverRunningFuture.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService); // Executor for running the assertion

        executorService.shutdown(); // Important!
    }
}
