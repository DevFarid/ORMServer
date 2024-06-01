package hive.database;

import hive.Server;
import hive.ServerTests;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Tests the `DBConnection` class mostly with other respective classes.
 * Created by SixEyes on 06/01/2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseTests {
    private final Logger logger = Logger.getLogger(ServerTests.class.getName());
    private static final int DELAY_MS = 50;

    // Test for connection source once server starts..
    @Test
    @Order(1)
    @DisplayName("test server is open and reachable.")
    public void testServerOpen() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            try(Server server = new Server(DBEnv.DEV,8080)) {
                executor.submit(server::start);
                latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
                Assertions.assertTrue(server.canInteractWithData());
            }
        }
    }
}
