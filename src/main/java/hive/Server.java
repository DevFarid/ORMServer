package hive;

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

/**
 * A java NIO server. Handles multiple clients using multiple threads.
 * The server can be stopped by typing "stop" in the console.
 * Created by SixEyes on 2024-04-07.
 */
public class Server {
    private final Logger logger = Logger.getLogger(Server.class.getName());
    private final ServerSocketChannel serverChannel;
    private final Selector selector;

    // State variable of the server.
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
            Scanner scanner = new Scanner(System.in);
            while(true) {
                if(scanner.next().equals("stop")) {
                    try {
                        stop();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
            scanner.close();
        });
    }

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

    // Accept a connection.
    public void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        logger.info(String.format("Accepted connection from %s", clientChannel.getRemoteAddress()));
    }

    public void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        if(clientChannel == null) { return; }
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = clientChannel.read(buffer);

        if(read == -1) {
            logger.info(String.format("Connection closed by %s", clientChannel.getRemoteAddress()));
            key.cancel();
            clientChannel.close();
            return;
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        logger.info(String.format("Received message from %s: %s", clientChannel.getRemoteAddress(), new String(bytes)));
    }

    // Stop the server.
    public void stop() throws IOException {
        if(running.get()) {
            running.set(false);
            this.selector.wakeup();
            this.selector.close();
            this.serverChannel.close();
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(25565);
        server.start();
    }
}
