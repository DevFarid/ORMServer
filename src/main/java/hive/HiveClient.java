package hive;

import hive.packets.Packet;
import hive.packets.PacketType;
import misc.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

public class HiveClient {
    private final Logger logger = Logger.getLogger(HiveClient.class.getName());
    private Selector selector;
    private SocketChannel clientChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread messageThread;

    public HiveClient(String host, int port) {
        try {
            this.selector = Selector.open();
            this.clientChannel = SocketChannel.open();
            this.clientChannel.configureBlocking(false);
            this.clientChannel.connect(new InetSocketAddress(host, port));
            this.clientChannel.register(selector, SelectionKey.OP_CONNECT);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating client.", e);
        }
    }

    /**
     * Starts the client and listens for messages from the server.
     */
    public void start() {
        running.set(true);

        try {
            messageThread = scannerThread();
            messageThread.start();
            while (running.get()) {
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();

                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }
                        
                        // Register
                        channel.register(selector, SelectionKey.OP_READ);
                        logger.info(String.format("Connected to server %s", channel.getRemoteAddress()));

                        sendPacket(new Packet(PacketType.MESSAGE, "message-chat", "Hello from client"));
                    }

                    //
                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.close();
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error starting client.", e);
        }


    }

    /**
     * Thread to read messages from the console and send them to the server.
     */
    private Thread scannerThread() {
        return new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                if (message.equals("stop")) {
                    stop();
                    break;
                }
                sendPacket(new Packet(PacketType.MESSAGE, "message-chat", message));
            }
            scanner.close();
        });
    }

    /**
     * Stops the client.
     */
    public void stop() {
        running.set(false);
        messageThread.interrupt();
        try {
            selector.close();
            clientChannel.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error stopping client.", e);
        }
    }

    // method to send message to server
    public void sendPacket(Packet p) {
        try {
            byte[] serializedPacket = Utils.serialize(p);
            ByteBuffer buffer = ByteBuffer.wrap(serializedPacket);

            while(buffer.hasRemaining()) {
                clientChannel.write(buffer);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "error sending a packet!", e);
        }
    }

    public static void main(String[] args) {
        HiveClient client = new HiveClient("localhost", 25565);
        client.start();
    }
}
