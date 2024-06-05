package hive;
import hive.database.DBEnv;
import hive.packets.DBPacket;
import hive.packets.MSGPacket;
import hive.packets.Packet;
import hive.packets.SQLCommandType;
import org.junit.jupiter.api.*;


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
    private static final int DELAY_MS = 50;
    // Test for server reachability and that channels are open.
    @Test
    @Order(1)
    @DisplayName("test server is open and reachable.")
    public void testServerOpen() throws Exception {
        try(Server server = new Server(DBEnv.DEV,8080)) {
            Assertions.assertTrue(server.isOpen());
        }
    }

    // Test for reachability once server is closed.
    @Test
    @Order(2)
    @DisplayName("test server is unreachable once closed.")
    public void testServerUnreachable() throws Exception {
        final Server server = new Server(DBEnv.DEV,8081);
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
    @DisplayName("test server is reachable & running for operations.")
    public void testServerRunning() throws Exception {
        final Server server = new Server(DBEnv.DEV,8082);
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
    @DisplayName("test client is connected to the server.")
    public void testServerReceiveClientConnection() throws Exception {
        final Server server = new Server(DBEnv.DEV,8083);
        final AtomicReference<HiveClient> clientRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(2);
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        
        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
    
        executor.submit(() -> {
            try {
                clientRef.set(new HiveClient(8083));
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
    @DisplayName("test server receives multiple client connections.")
    public void testServerReceiveMultipleClientConnections() throws Exception {
        final Server server = new Server(DBEnv.DEV,8084);
        final AtomicReference<HiveClient> clientRef1 = new AtomicReference<>();
        final AtomicReference<HiveClient> clientRef2 = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(3);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        
        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
        
        executor.submit(() -> {
            try {
                clientRef1.set(new HiveClient( 8084));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef1.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);
        
        executor.submit(() -> {
            try {
                clientRef2.set(new HiveClient(8084));
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
    @DisplayName("test server sends messages to all clients.")
    public void testServerSendMessagesToAllClients() throws Exception {
        final Server server = new Server(DBEnv.DEV,8085);
        final AtomicReference<HiveClient> clientRef1 = new AtomicReference<>();
        final AtomicReference<HiveClient> clientRef2 = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(4);
        final ExecutorService executor = Executors.newFixedThreadPool(3);
        String packetMessage = String.format("%s Hello World %s", Math.random() * 100, Math.random() * 100);

        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            try {
                clientRef1.set(new HiveClient(8085));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef1.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            try {
                clientRef2.set(new HiveClient(8085));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef2.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        final AtomicBoolean messageReceivedClient1 = new AtomicBoolean(false);
        final AtomicBoolean messageReceivedClient2 = new AtomicBoolean(false);

        clientRef1.get().addNetworkEventListener(event -> {
            MSGPacket msgPacket = (MSGPacket) event.getPacket();
            if (packetMessage.equals(msgPacket.getMessage())) {
                messageReceivedClient1.set(true);
            }
        });

        clientRef2.get().addNetworkEventListener(event -> {
            MSGPacket msgPacket = (MSGPacket) event.getPacket();
            if (packetMessage.equals(msgPacket.getMessage())) {
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
    @DisplayName("test server receiving a packet from a connected client.")
    public void testServerReceivePacketFromConnectedClient() throws Exception {

        final Server server = new Server(DBEnv.DEV,8086);
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
                clientRef1.set(new HiveClient(8086));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef1.get().start();
        });

        executor.submit(() -> {
            try {
                clientRef2.set(new HiveClient(8086));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef2.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        server.addNetworkEventListener(event -> {
            MSGPacket msgPacket = (MSGPacket) event.getPacket();
            if ("Hello Server from Client 1".equals(msgPacket.getMessage())) {
                messageReceivedClient1.set(true);
            } else if("Hello Server from Client 2".equals(msgPacket.getMessage())) {
                messageReceivedClient2.set(true);
            }
        });

        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        clientRef1.get().sendPacket(new MSGPacket("Hello Server from Client 1"));
        clientRef2.get().sendPacket(new MSGPacket("Hello Server from Client 2"));


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
    @DisplayName("test server receiving a DBPacket from a connected client.")
    public void testDBPacket() throws Exception {
        final Server server = new Server(DBEnv.DEV, 8087);
        final AtomicReference<HiveClient> clientRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(4);
        final ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(server::start);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        executor.submit(() -> {
            try {
                clientRef.set(new HiveClient( 8087));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            clientRef.get().start();
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        final AtomicBoolean packetReceived = new AtomicBoolean(false);
        server.addNetworkEventListener(event -> {
            DBPacket dbPacket = (DBPacket) event.getPacket();
            if (dbPacket.getCommandType() == SQLCommandType.SELECT) {

                if(dbPacket.getTableName().equalsIgnoreCase("users")) {

                    if(dbPacket.getColumns().containsKey("id")) {

                        if(dbPacket.getColumns().containsKey("first_name")) {

                            if(dbPacket.getCondition().equalsIgnoreCase("WHERE last_name = DOE")) {

                                if(dbPacket.getColumns().get("id").equals("1")) {

                                    if(dbPacket.getColumns().get("first_name").equals("JOHN")) {

                                        packetReceived.set(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        DBPacket packet = new DBPacket("users", SQLCommandType.SELECT);
        packet.addColumn("id", "1");
        packet.addColumn("first_name", "JOHN");
        packet.setCondition("WHERE last_name = DOE");
        clientRef.get().sendPacket(packet);
        latch.await(DELAY_MS, TimeUnit.MILLISECONDS);

        Assertions.assertTrue(packetReceived.get(), "Server did not receive the expected DBPacket from the client.");

        clientRef.get().stop();
        server.close();
        executor.close();
    }

}
