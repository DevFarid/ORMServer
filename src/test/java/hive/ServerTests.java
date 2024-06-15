package hive;
import hive.database.Environment;
import hive.packets.*;
import hive.packets.child.Message;
import hive.packets.child.SQLacket;
import org.junit.jupiter.api.*;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Test coverage for the {@code Server} server class.
 * Created by SixEyes, R-E-M-O on 05-01-2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerTests {
    private static final int DELAY_MS = 50;

    private int generateRandomPort() {
        return 1000 + (new Random()).nextInt(9000);
    }

    // Test for server reachability and that channels are open.
    @Test
    @Order(1)
    @DisplayName("test-1: server is open and reachable.")
    public void testServerOpen() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        Server server = new Server(Environment.DEV,generateRandomPort());

        Assertions.assertTrue(server.isOpen());
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        server.close();
    }

    // Test for reachability once server is closed.
    @Test
    @Order(2)
    @DisplayName("test-2: server is unreachable once closed.")
    public void testServerUnreachable() throws Exception {
        final Server server = new Server(Environment.DEV,generateRandomPort());
        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
        server.close();
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        Assertions.assertFalse(server.isOpen());
        executor.close();
    }

    // Test server is in operation mode.
    @Test
    @Order(3)
    @DisplayName("test-3: server is reachable & running for operations.")
    public void testServerRunning() throws Exception {
        final Server server = new Server(Environment.DEV,generateRandomPort());
        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        
        Assertions.assertFalse(server.isRunning());
        executor.submit(server::start);

        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(server.isRunning());
        server.close();
        executor.close();
    }

    // Test server receive Client Connection.
    @Test
    @Order(4)
    @DisplayName("test-4: client is connected to the server.")
    public void testServerReceiveClientConnection() throws Exception {
        int port = generateRandomPort();
        final Server server = new Server(Environment.DEV,port);
        final AtomicReference<HiveClient> clientRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(2);
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        
        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
    
        executor.submit(() -> {
            try {
                clientRef.set(new HiveClient(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
        
        Assertions.assertTrue(server.getConnectedClients().size() == 1);
        server.close();
        clientRef.get().stop();
        executor.close();
    }
    
    //Test server receiving multiple client connections
    @Test
    @Order(5)
    @DisplayName("test-5: server receives multiple client connections.")
    public void testServerReceiveMultipleClientConnections() throws Exception {
        int port = generateRandomPort();
        final Server server = new Server(Environment.DEV,port);
        final AtomicReference<HiveClient> clientRef1 = new AtomicReference<>();
        final AtomicReference<HiveClient> clientRef2 = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(3);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        
        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
        
        executor.submit(() -> {
            try {
                clientRef1.set(new HiveClient( port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef1.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
        
        executor.submit(() -> {
            try {
                clientRef2.set(new HiveClient(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef2.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
        
        Assertions.assertTrue(server.getConnectedClients().size() == 2);
        server.close();
        clientRef1.get().stop();
        clientRef2.get().stop();
        executor.close();
    }

    //Test server sending messages to all clients
    @Test
    @Order(6)
    @DisplayName("test-6: server sends messages to all clients.")
    public void testServerSendMessagesToAllClients() throws Exception {
        int port = generateRandomPort();
        final Server server = new Server(Environment.DEV,port);
        final AtomicReference<HiveClient> clientRef1 = new AtomicReference<>();
        final AtomicReference<HiveClient> clientRef2 = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(4);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        String packetMessage = String.format("%s Hello World %s", Math.random() * 100, Math.random() * 100);

        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            try {
                clientRef1.set(new HiveClient(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef1.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            try {
                clientRef2.set(new HiveClient(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef2.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        final AtomicBoolean messageReceivedClient1 = new AtomicBoolean(false);
        final AtomicBoolean messageReceivedClient2 = new AtomicBoolean(false);

        clientRef1.get().addNetworkEventListener(event -> {
            Message message = (Message) event.getPacket();
            if (packetMessage.equals(message.getMessage())) {
                messageReceivedClient1.set(true);
            }
        });

        clientRef2.get().addNetworkEventListener(event -> {
            Message message = (Message) event.getPacket();
            if (packetMessage.equals(message.getMessage())) {
                messageReceivedClient2.set(true);
            }
        });

        // Send message to all clients
        server.broadcastMessage(packetMessage);

        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        // Assertions
        Assertions.assertTrue(messageReceivedClient1.get(), "Client 1 did not receive the expected message.");
        Assertions.assertTrue(messageReceivedClient2.get(), "Client 2 did not receive the expected message.");

        server.close();
        clientRef1.get().stop();
        clientRef2.get().stop();

        executor.close();
    }

    
    // Test server receiving a packet from two clients.
    @Test
    @Order(7)
    @DisplayName("test-7: server receiving a packet from a connected client.")
    public void testServerReceivePacketFromConnectedClient() throws Exception {
        int port = generateRandomPort();
        final Server server = new Server(Environment.DEV,port);
        final AtomicReference<HiveClient> clientRef1 = new AtomicReference<>();
        final AtomicReference<HiveClient> clientRef2 = new AtomicReference<>();

        final CountDownLatch latch = new CountDownLatch(4);
        final ExecutorService executor = Executors.newFixedThreadPool(3);


        final AtomicBoolean messageReceivedClient1 = new AtomicBoolean(false);
        final AtomicBoolean messageReceivedClient2 = new AtomicBoolean(false);

        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            try {
                clientRef1.set(new HiveClient(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef1.get().start();
        });

        executor.submit(() -> {
            try {
                clientRef2.set(new HiveClient(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef2.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        server.addNetworkEventListener(event -> {
            Message message = (Message) event.getPacket();
            if ("Hello Server from Client 1".equals(message.getMessage())) {
                messageReceivedClient1.set(true);
            } else if("Hello Server from Client 2".equals(message.getMessage())) {
                messageReceivedClient2.set(true);
            }
        });

        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        clientRef1.get().sendPacket(new Message("Hello Server from Client 1"));
        clientRef2.get().sendPacket(new Message("Hello Server from Client 2"));


        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        // Assertions
        Assertions.assertTrue(messageReceivedClient1.get(), "Server did not receive the expected message from Client 1.");
        Assertions.assertTrue(messageReceivedClient2.get(), "Server did not receive the expected message from Client 2.");

        clientRef1.get().stop();
        clientRef2.get().stop();
        server.close();
        executor.close();
    }

    // test server receiving a DBPacket from a single client
    @Test
    @Order(8)
    @DisplayName("test-8: server receiving a DBPacket from a connected client.")
    public void testDBPacket() throws Exception {
        int port = generateRandomPort();
        final Server server = new Server(Environment.DEV, port);
        final AtomicReference<HiveClient> clientRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(4);
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            try {
                clientRef.set(new HiveClient( port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        final AtomicBoolean packetReceived = new AtomicBoolean(false);
        server.addNetworkEventListener(event -> {
            if(event.getPacket().getType() == PacketType.SQL)
                packetReceived.set(true);
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        SQLacket packet = new SQLacket();
        clientRef.get().sendPacket(packet);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(packetReceived.get(), "Server did not receive the expected DBPacket from the client.");

        clientRef.get().stop();
        server.close();
        executor.close();
    }

    // Test client receiving a file from server
    @Test
    @Order(9)
    @DisplayName("test-9: server client a file from server.")
    public void testFilePacket() throws Exception {
        int port = generateRandomPort();
        final Server server = new Server(Environment.DEV, port);
        final AtomicReference<HiveClient> clientRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(4);
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            try {
                clientRef.set(new HiveClient(port));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        final AtomicBoolean packetReceived = new AtomicBoolean(false);
        server.addNetworkEventListener(event -> {
            if(event.getPacket().getType() == PacketType.FILE) {
                packetReceived.set(true);
            }
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        Path filePath = Paths.get("").toAbsolutePath().resolve("src/test/resources/test.txt");
        byte[] fileBytes = Files.readAllBytes(filePath);
        hive.packets.child.File filePacket = new hive.packets.child.File("test.txt", fileBytes);
        clientRef.get().sendPacket(filePacket);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(packetReceived.get(), "Server did not receive the expected FilePacket from the client.");

        clientRef.get().stop();
        server.close();
        executor.close();
    }
}
