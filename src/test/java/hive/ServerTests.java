package hive;

import com.google.common.util.concurrent.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerTests {
    private final Logger logger = Logger.getLogger(ServerTests.class.getName());
    private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
    private Server server;

    public ListenableFuture<?> setup() {
        return executor.submit(() -> {
            try {
                server = new Server(8080);
                server.start();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error starting server.", e);
            }
        });
    }

    public void teardown() throws IOException {
        server.stop();
    }

    @Test
    @Order(1)
    public void testServerRunning() {
        ListenableFuture<?> serverFuture = setup();
        Futures.addCallback(serverFuture, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                Assertions.assertTrue(server.isRunning());
                try {
                    teardown();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public void onFailure(Throwable t) {
                logger.log(Level.SEVERE, "Error starting server.", t);
            }
        }, executor);
    }
}
