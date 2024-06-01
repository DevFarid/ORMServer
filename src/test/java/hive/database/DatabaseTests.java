package hive.database;

import hive.Server;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Tests the `DBConnection` class mostly with other respective classes.
 * Created by SixEyes on 06/01/2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTests {
    private static final int DELAY_MS = 50;

    // Test for connection source once server starts.
    @Test
    @Order(1)
    @DisplayName("test database is closed and reachable.")
    public void testServerDatabaseOpen() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            try(Server server = new Server(DBEnv.DEV,8080)) {
                executor.submit(server::start);
                latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
                Assertions.assertTrue(server.canInteractWithData());
            }
        }
    }

    // Test for connection source reachability once server is closed.
    @Test
    @Order(1)
    @DisplayName("test database is closed and unreachable.")
    public void testServerDatabaseClosed() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        Server server = new Server(DBEnv.DEV,8080);

        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        server.close();
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        Assertions.assertFalse(server.canInteractWithData());
    }
}
