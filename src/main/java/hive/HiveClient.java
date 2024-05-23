package hive;

import hive.event.NetworkEvent;
import hive.event.NetworkEventListener;
import hive.packets.Packet;
import hive.packets.PacketType;
import misc.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Scanner;

public class HiveClient {
    private final Logger logger = Logger.getLogger(HiveClient.class.getName());
    private Selector selector;
    private SocketChannel clientChannel;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread scannerThread = scannerThread();
    private final List<NetworkEventListener> listeners = new ArrayList<>();

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
     * Thread to read messages from the console and send them to the server.
     */
    private Thread scannerThread() {
        return new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                if(!scanner.hasNextLine()) continue;
                String message = scanner.nextLine();
                if (message.isEmpty()) {
                    continue;
                }
                if (message.equals("stop")) {
                    try {
                        stop();
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error stopping client.", e);
                    }
                    break;
                }
                sendPacket(new Packet(PacketType.MESSAGE, "chat-channel", message));
            }
            scanner.close();
        });
    }

    /**
     * Starts the client and listens for messages from the server.
     */
    public void start() throws IOException {
        running.set(true);

        try {
            scannerThread = scannerThread();
            scannerThread.start();
            while (running.get()) {
                int selectedResult = this.selector.select();
                if (selectedResult == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = this.selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();

                        if (channel.isConnectionPending()) {
                            channel.finishConnect();
                        }

                        // Register
                        channel.register(this.selector, SelectionKey.OP_READ);
                        logger.info(String.format("Connected to server %s", channel.getRemoteAddress()));

                        sendPacket(new Packet(PacketType.MESSAGE, "message-chat", "New client connected."));
                    } else if(key.isReadable()) {
                        Packet packet = this.read();
                        if (packet != null) {
                            notifyListeners(packet);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error starting client.", e);
        } finally {
            stop();
        }
    }

    // method to send message to server
    public void sendPacket(Packet p) {
        try {
            byte[] serializedPacket = Utils.serializePacket(p);
            ByteBuffer buffer = ByteBuffer.wrap(serializedPacket);

            while (buffer.hasRemaining()) {
                clientChannel.write(buffer);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "error sending a packet!", e);
        }
    }

    protected Packet read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = this.clientChannel.read(buffer);
        if (read == -1) {
            logger.info("Server has closed the connection.");
            this.clientChannel.close();
            return null;
        }

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        Packet packet = Utils.deserializePacket(data);
        logger.info(String.format("Received packet from server: %s", packet));

        return packet;
    }

    /**
     * Gets the current client channel.
     * 
     * @return the {@code SocketChannel} channel of this client.
     */
    public SocketChannel getChannel() {
        return this.clientChannel;
    }

    /**
     * Adds a network event listener to the client.
     * @param listener the new listener to listen to.
     */
    public void addNetworkEventListener(NetworkEventListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a network event listener from the client.
     * @param listener the listener to remove.
     */
    public void removeNetworkEventListener(NetworkEventListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Notifies all listeners of a new message.
     * This fires the {@code onMessageReceived} event for all listeners.
     * @param packet the received packet.
     */
    private void notifyListeners(Packet packet) {
        NetworkEvent event = new NetworkEvent(this, packet);
        for (NetworkEventListener listener : listeners) {
            listener.onMessageReceived(event);
        }
    }

    /**
     * Stops the client.
     * 
     * @throws IOException any errors causing a shutdown failure.
     */
    public void stop() throws IOException {
        running.set(false);
        scannerThread.interrupt();
        this.selector.close();
        this.clientChannel.close();
        this.listeners.clear();
    }

    public static void main(String[] args) {
        HiveClient client = new HiveClient("localhost", 25565);
        try {
            client.start();
        } catch (IOException e) {
            System.out.printf(
                    "Error starting client.\nCause: %s\nTrace: %s\n", e.getCause(), e.fillInStackTrace());
        }
    }
}
