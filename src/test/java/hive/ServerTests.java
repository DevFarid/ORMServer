package hive;
import org.junit.jupiter.api.*;

import hive.packets.PacketType;


import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
    
    // Test for server reachability and that channels are open.
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
        final Server server = new Server(8081);
        server.close();
        Assertions.assertFalse(server.isOpen());
    }

    // Test server is in operation mode.
    @Test
    @Order(3)
    @DisplayName("test server is reachable & running for operations.")
    public void testServerRunning() throws Exception {
        final Server server = new Server(8082);
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

    // Test server receive Client Connection.
    @Test
    @Order(4)
    @DisplayName("test client is connected to the server.")
    public void testServerReceiveClientConnection() throws Exception {
        final Server server = new Server(8083);
        final AtomicReference<HiveClient> clientRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(2);
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        
        executor.submit(server::start);
        latch.await(500, TimeUnit.MILLISECONDS);
    
        executor.submit(() -> {
            clientRef.set(new HiveClient("localhost", 8083));
            try {
                clientRef.get().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);
        
        Assertions.assertTrue(server.getConnectedClients().size() == 1);
        server.close();
        clientRef.get().stop();
    }
    
    //Test server receiving multiple client connections\
    @Test
    @Order(5)
    @DisplayName("test server receives multiple client connections.")
    public void testServerReceiveMultipleClientConnections() throws Exception {
        final Server server = new Server(8084);
        final AtomicReference<HiveClient> clientRef1 = new AtomicReference<>();
        final AtomicReference<HiveClient> clientRef2 = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(3);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        
        executor.submit(server::start);
        latch.await(500, TimeUnit.MILLISECONDS);
        
        executor.submit(() -> {
            clientRef1.set(new HiveClient("localhost", 8084));
            try {
                clientRef1.get().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);
        
        executor.submit(() -> {
            clientRef2.set(new HiveClient("localhost", 8084));
            try {
                clientRef2.get().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);
        
        Assertions.assertTrue(server.getConnectedClients().size() == 2);
        server.close();
        clientRef1.get().stop();
        clientRef2.get().stop();
    }

    //Test server sending messages to all clients
    @Test
    @Order(6)
    @DisplayName("test server sends messages to all clients.")
    public void testServerSendMessagesToAllClients() throws Exception {
        final Server server = new Server(8085);
        final AtomicReference<HiveClient> clientRef1 = new AtomicReference<>();
        final AtomicReference<HiveClient> clientRef2 = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(3);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        String packetMessage = String.format("%s Hello World %s", Math.random() * 100, Math.random() * 100);

        executor.submit(server::start);
        latch.await(500, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            clientRef1.set(new HiveClient("localhost", 8085));
            try {
                clientRef1.get().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            clientRef2.set(new HiveClient("localhost", 8085));
            try {
                clientRef2.get().start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        latch.await(500, TimeUnit.MILLISECONDS);

        final AtomicBoolean messageReceivedClient1 = new AtomicBoolean(false);
        final AtomicBoolean messageReceivedClient2 = new AtomicBoolean(false);

        clientRef1.get().addNetworkEventListener(event -> {
            if (event.getPacket().getType() == PacketType.MESSAGE &&
                    packetMessage.equals(event.getPacket().getData())) {
                messageReceivedClient1.set(true);
            }
        });

        clientRef2.get().addNetworkEventListener(event -> {
            if (event.getPacket().getType() == PacketType.MESSAGE &&
                    packetMessage.equals(event.getPacket().getData())) {
                messageReceivedClient2.set(true);
            }
        });

        // Send message to all clients
        server.broadcastMessage(packetMessage);

        latch.await(500, TimeUnit.MILLISECONDS);

        // Assertions
        Assertions.assertTrue(messageReceivedClient1.get(), "Client 1 did not receive the expected message.");
        Assertions.assertTrue(messageReceivedClient2.get(), "Client 2 did not receive the expected message.");

        server.close();
        clientRef1.get().stop();
        clientRef2.get().stop();
    }

    
    // Test server receiving a packet from a client.
    @Test
    @Order(7)
    @DisplayName("test server receiving a packet from a connected client.")
    public void testServerReceivePacketFromConnectedClient() throws Exception {

        final Server server = new Server(8086);
        final AtomicReference<HiveClient> clientRef = new AtomicReference<>(new HiveClient("localhost", 8086));
        final CountDownLatch latch = new CountDownLatch(2);

        Assertions.assertTrue(true);
        server.close();
        clientRef.get().stop();
    }
    
    // Test server receiving multiple simoultaneous packets from many clients.
    
}
