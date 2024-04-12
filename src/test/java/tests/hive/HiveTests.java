package tests.hive;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import hive.HiveClient;
import hive.Server;

import org.javatuples.Pair;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HiveTests {

    public static Pair<AtomicReference<Server>, ListenableFuture<Boolean>> setupServer(int port) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AtomicReference<Server> eval = new AtomicReference<>();
        Future<?> serverStartFuture = executorService.submit(() -> {
            try (final Server server = new Server(port)) {
                eval.set(server);
                eval.get().start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executorService);
        ListenableFuture<Boolean> serverRunningFuture = listeningExecutor.submit(serverStartFuture::isDone);

        return Pair.with(eval, serverRunningFuture);
    }

    public static Pair<AtomicReference<HiveClient>, ListenableFuture<Boolean>> setupClient(int port) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        AtomicReference<HiveClient> clientRef = new AtomicReference<>();
    
        Future<?> clientStartFuture = executorService.submit(() -> {
            try {
                HiveClient client = new HiveClient("localhost", port); 
                clientRef.set(client);
                client.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    
        ListeningExecutorService listeningExecutor = MoreExecutors.listeningDecorator(executorService);
        ListenableFuture<Boolean> clientRunningFuture = listeningExecutor.submit(clientStartFuture::isDone);
    
        return Pair.with(clientRef, clientRunningFuture);
    }
    

    /**
     * Test if the server is up and running.
     * @throws IOException
     */
    @Test
    @Order(1)
    void testServerUp() throws IOException {
        Pair<AtomicReference<Server>, ListenableFuture<Boolean>> server = setupServer(8080);
        server.getValue1().addListener(() -> {
            try {
                Assertions.assertTrue((server.getValue0() == null || server.getValue0().get() == null) ? false : server.getValue0().get().isRunning());
                server.getValue0().get().stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Executors.newSingleThreadExecutor());
    }

    /**
     * Test if the client is initialized but not connected.
     * @throws IOException
     */
    @Test
    @Order(2)
    void testClientInitButNotConnected() throws IOException {
        Pair<AtomicReference<HiveClient>, ListenableFuture<Boolean>> client = setupClient(8080);
        client.getValue1().addListener(() -> {
            try {
                Assertions.assertTrue((client.getValue0() == null || client.getValue0().get() == null) ? false : client.getValue0().get().isRunning());
                Assertions.assertTrue((client.getValue0() == null || client.getValue0().get() == null) ? false : !client.getValue0().get().isOnline());
                client.getValue0().get().stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, Executors.newSingleThreadExecutor());
    }

    /**
     * Test if the client is connected to the server.
     * @throws IOException
     */
    @Test
    @Order(3)
    void testClientConnectToServer() throws IOException {
        Pair<AtomicReference<Server>, ListenableFuture<Boolean>> server = setupServer(8080);
        Pair<AtomicReference<HiveClient>, ListenableFuture<Boolean>> client = setupClient(8080);

        // wait for server.
        server.getValue1().addListener(()->{
            // wait for client.
            client.getValue1().addListener(()-> {
                server.getValue0().get().getConnectedClients().forEach(clientChannel -> {
                    try {
                        Assertions.assertTrue(
                            client.getValue0().get().getChannel().isConnected() && 
                            server.getValue0().get().getConnectedClients().contains(client.getValue0().get().getChannel())
                        );
                        
                        client.getValue0().get().stop();
                        server.getValue0().get().stop();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }, Executors.newSingleThreadExecutor());

        }, Executors.newSingleThreadExecutor());
    }


    @Test
    @Order(4)
    void testTwoClientsConnectToServer() {
        Pair<AtomicReference<Server>, ListenableFuture<Boolean>> server = setupServer(8080);
        Pair<AtomicReference<HiveClient>, ListenableFuture<Boolean>> clientOne = setupClient(8080);
        Pair<AtomicReference<HiveClient>, ListenableFuture<Boolean>> clientTwo = setupClient(8080);

        server.getValue1().addListener(()->{

            clientOne.getValue1().addListener(()->{

                clientTwo.getValue1().addListener(()->{

                    server.getValue0().get().getConnectedClients().forEach(clientChannel -> {
                        try {
                            Assertions.assertTrue(
                                clientOne.getValue0().get().getChannel().isConnected() && 
                                server.getValue0().get().getConnectedClients().contains(clientOne.getValue0().get().getChannel())
                            );

                            Assertions.assertTrue(
                                clientTwo.getValue0().get().getChannel().isConnected() && 
                                server.getValue0().get().getConnectedClients().contains(clientTwo.getValue0().get().getChannel())
                            );

                            clientOne.getValue0().get().stop();
                            clientTwo.getValue0().get().stop();
                            server.getValue0().get().stop();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });

                }, Executors.newSingleThreadExecutor());

            }, Executors.newSingleThreadExecutor());

        }, Executors.newSingleThreadExecutor());
    }
}
