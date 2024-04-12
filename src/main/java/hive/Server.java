package hive;

import hive.packets.Packet;
import misc.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashSet;
/**
 * A java NIO server. Handles multiple clients using multiple threads.
 * The server can be stopped by typing "stop" in the console.
 * Created by SixEyes on 2024-04-07.
 */
public class Server implements AutoCloseable {
    private final Logger logger = Logger.getLogger(Server.class.getName());
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final Set<SocketChannel> connectedClients = new HashSet<SocketChannel>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public Server(int port) throws IOException {
        logger.info(String.format("Starting server on port %d", port));
        // Open selector and server channel
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.serverChannel.configureBlocking(false);
        this.serverChannel.socket().bind(new InetSocketAddress(port));
        this.serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Add shutdown hook to close channels. Code might be redundant here, need to double-check.
        Runtime.getRuntime().addShutdownHook((new Thread(() -> {
            try {
                serverChannel.close();
                selector.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing server.", e);
            }
        })));
    }

    /**
     * Scanner thread to stop the server from user I/O input.
     * @return a thread that listens for {@code Scanner} user input to stop the server.
     */
    private Thread scannerThread() {
        return new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    if (scanner.next().equals("stop")) {
                        try {
                            stop();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
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
            running.set(true);

            // register scanner thread
            scannerThread().start();

            while (running.get()) {
                try {
                    // A channel is ready.
                    selector.select();
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
        try(ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel()) {
            SocketChannel clientChannel = serverChannel.accept();
            clientChannel.configureBlocking(false);
            clientChannel.register(selector, SelectionKey.OP_READ);
            logger.info(String.format("Accepted connection from %s", clientChannel.getRemoteAddress()));

            connectedClients.add(clientChannel);
        }
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
            key.cancel();
            connectedClients.remove(clientChannel);
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
     * Stops the server.
     * @throws IOException if an I/O error occurs.
     */
    public void stop() throws IOException {
        if(running.get()) {
            running.set(false);
            this.selector.wakeup();
            this.selector.close();
            this.serverChannel.close();
        }
    }

    public static void main(String[] args) throws Exception {
        try(Server server = new Server(25565)) {
            server.start();
        }
    }

    public boolean isRunning() {
        return running.get() && serverChannel.isOpen();
    }

    public Set<SocketChannel> getConnectedClients() {
        return connectedClients;
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
