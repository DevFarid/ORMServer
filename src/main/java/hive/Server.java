package hive;

import hive.packets.Packet;
import misc.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A java NIO server. Handles multiple clients using multiple threads.
 * The server can be stopped by typing "stop" in the console.
 * Created by SixEyes on 2024-04-07.
 */
public class Server implements AutoCloseable {
    private final Logger logger = Logger.getLogger(Server.class.getName());
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Set<SocketChannel> connectedClients = new HashSet<>();

    public Server(int port) throws IOException {
        logger.info(String.format("Opening a socket on port %d", port));
        // Open selector and server channel
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.serverChannel.socket().bind(new InetSocketAddress(port));
        this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Scanner thread to stop the server from user I/O input.
     * @return a thread that listens for {@code Scanner} user input to stop the server.
     */
    private Thread scannerThread() {
        return new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    if(scanner.hasNextLine()) {
                        String message = scanner.nextLine().trim();
                        if(message.equalsIgnoreCase("stop")) {
                            try {
                                stop();
                            } catch (IOException e) {
                                logger.log(Level.SEVERE, "Error stopping server.", e);
                            }
                            break;
                        } else if(message.equalsIgnoreCase("clist")) {
                            Set<SocketChannel> clients = getConnectedClients();
                            StringBuilder sbuilder = new StringBuilder();
                            if(!clients.isEmpty()) {
                                clients.forEach(client -> {
                                    try {
                                        if(client.isOpen() && client.isConnected())
                                            sbuilder.append(client.getRemoteAddress()).append("\n");
                                    } catch (IOException e) {
                                        logger.log(Level.SEVERE, "Error retrieve the connected client list.", e);
                                    }
                                });
                                logger.info(String.format("Connected clients: \n%s", sbuilder.toString()));
                            }
                        }
                    }
                }
            }
        });
    }


    /**
     * Starts the server.
     * Starts a scanner thread that will listen for user commands.
     * This method will handle new connections and read data from clients.
     */
    public void start() {
        // Start the server if it is not running.
        if(!running.get()) {
            logger.info("Now accepting operations from clients.");
            running.set(true);

            // register scanner thread
            scannerThread().start();

            while (running.get()) {
                try {
                    // A channel is ready.
                    int selectedResult = this.selector.select();
                    if(selectedResult == 0) { continue; }

                    Set<SelectionKey> selectedKey = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKey.iterator();

                    while(keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();

                        if(!key.isValid()) {
                            continue;
                        }
                        
                        if(key.isAcceptable()) {
                            accept(key);
                        }

                        if(key.isReadable()) {
                            read(key);
                        }
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error selecting.", e);
                }

            }
        }
    }

    /**
     * Accept a connection from a client.
     * @param key the selection key.
     * @throws IOException if an I/O error occurs.
     */

    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        connectedClients.add(clientChannel);
        logger.info(String.format("Accepted connection from %s", clientChannel.getRemoteAddress()));
    }

    /**
     * Read data from a client.
     * @param key the selection key.
     * @throws IOException if an I/O error occurs.
     */
    public void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        if(clientChannel == null) { return; }
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = clientChannel.read(buffer);

        if(read == -1) {
            logger.info(String.format("Connection closed by %s", clientChannel.getRemoteAddress()));
            connectedClients.remove(clientChannel);
            key.cancel();
            clientChannel.close();
            return;
        }
        
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        Packet packet = Utils.deserializePacket(data);
        logger.info(String.format("Received packet from %s: %s", clientChannel.getRemoteAddress(), packet));
    }

    /**
     * Checks if the server is running. To consider the server to be running,
     * a check is done on see if both the server channel and the selector are open,
     * as well as the selector managing key operations.
     * @return {@code true} if the server is running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return ( this.serverChannel.isOpen() && this.selector.isOpen() ) && running.get();
    }

    /**
     * Checks if the server is open. This means that the server channel and the selector are open.
     * But doesn't guarantee that the server is running, i.e. managing key operations (accept, read, write).
     * @return {@code true} if the server is reachable via internet, {@code false} otherwise.
     */
    public boolean isOpen() {
        return this.serverChannel.isOpen() && this.selector.isOpen();
    }

    public Set<SocketChannel> getConnectedClients() {
        return this.connectedClients;
    }

    /**
     * Stops the server.
     * @throws IOException if an I/O error occurs.
     */
    public void stop() throws IOException {
        running.set(false);
        this.selector.wakeup();
        this.selector.close();
        this.serverChannel.close();
    }

    @Override
    public void close() throws Exception {
        stop();
    }

    public static void main(String[] args) throws Exception {
        try(Server server = new Server(25565)) {
            server.start();
        } catch(IOException e) {
            System.out.printf(
                "Error starting client.\nCause: %s\nTrace: %s\n", e.getCause(), e.fillInStackTrace()
            );
        }
        
    }
}
