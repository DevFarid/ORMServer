package hive;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test coverage for the {@code Server} server class.
 * Created by SixEyes, R-E-M-O on 05-01-2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerTests {
    private final Logger logger = Logger.getLogger(ServerTests.class.getName());
    // Test for server reachability and open state.
    @Test
    @Order(1)
    @DisplayName("test server is open and reachable.")
    public void testServerOpen() throws Exception {
        try(Server server = new Server(8080)) {
            Assertions.assertTrue(server.isOpen());
        }
    }

    // Test for unreachability once server is closed.
    @Test
    @Order(2)
    @DisplayName("test server is unreachable once closed.")
    public void testServerUnreachable() throws Exception {
        final Server server = new Server(8181);
        server.close();
        Assertions.assertFalse(server.isOpen());
    }

    // Test server is in operation mode.
    @Test
    @Order(3)
    @DisplayName("test server is reachable & running for operations.")
    public void testServerRunning() throws Exception {
        final Server server = new Server(8282);
        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Assertions.assertFalse(server.isRunning());
        executor.submit(() -> {
            try {
                server.start();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error starting server in the background.", e);
            }
        });

        latch.await(500, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(server.isRunning());
        server.close();
    }

    // Test client connecting to server.
    @Test
    @Order(4)
    @DisplayName("test client is connected to the server.")
    public void testClientConnected() throws Exception {
        final Server server = new Server(8383);
        final AtomicReference<HiveClient> clientRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(2);
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        
        executor.submit(server::start);
        latch.await(500, TimeUnit.MILLISECONDS);
    
        executor.submit(() -> {
            clientRef.set(new HiveClient("localhost", 8383));
            try {
                clientRef.get().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);
        
        Assertions.assertTrue(server.getConnectedClients().size() == 1);
        server.close();
    }
    
    //Test server receiving multiple client connections and sending messages to all clients
    
    
}
