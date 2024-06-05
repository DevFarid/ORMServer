package hive;

import hive.commands.ConsoleCommand;
import hive.console.Console;
import hive.packets.MSGPacket;
import hive.packets.Packet;
import misc.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Level;

/**
 * A java NIO server. Handles multiple clients using multiple threads.
 * The server can be stopped by typing "stop" in the console.
 * Created by SixEyes on 2024-04-07.
 */
public class Server extends Console implements AutoCloseable {

    private final List<SocketChannel> connectedClients = new ArrayList<>();

    public Server(int port) throws IOException {
        super(true, port);

        ConsoleCommand clientList = new ConsoleCommand("clients") {
            @Override
            public boolean requiresParams() {
                return false;
            }
        };
        clientList.setRunnableAction(() -> {
            getLogger().info("Connected clients:");
            getConnectedClients().forEach(client -> {
                try {
                    getLogger().info(client.getRemoteAddress().toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });

        ConsoleCommand broadcastCmd = new ConsoleCommand("broadcast") {
            @Override
            public boolean requiresParams() {
                return true;
            }
        };
        broadcastCmd.setRunnableAction(() -> {
            if(broadcastCmd.getParams().isEmpty()) {
                getLogger().info("No message to broadcast.");
                return;
            }
            broadcastMessage(String.join(" ", broadcastCmd.getParams()));
        });

        addCommands(clientList, broadcastCmd);
    }

    /**
     * Starts the server.
     * Starts a scanner thread that will listen for user commands.
     * This method will handle new connections and read data from clients.
     */
    @Override
    public void start() {
        // Start the server if it is not running.
        if(!getState().get()) {
            getLogger().info("Now accepting operations from clients.");
            getState().set(true);
            startConsole();

            while (getState().get()) {
                try {
                    // A channel is ready.
                    int selectedResult = this.getSelector().select();
                    if(selectedResult == 0) { continue; }

                    Set<SelectionKey> selectedKey = getSelector().selectedKeys();
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
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            Packet receivedPacket = read(key);
                            if(receivedPacket != null) {
                                switch (receivedPacket.getType()) {
                                    case MESSAGE -> {
                                        MSGPacket msgPacket = (MSGPacket) receivedPacket;
                                        getLogger().info(String.format("[%s](MSGPacket): %s", clientChannel.getRemoteAddress(), msgPacket.getMessage()));
                                    }
                                    case SQL -> getLogger().info(String.format("[%s](DBPacket): %s", clientChannel.getRemoteAddress(), receivedPacket));
                                }
                                notifyListeners(receivedPacket, getLogger());
                            }
                        }
                    }
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "Error selecting.", e);
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
        clientChannel.register(getSelector(), SelectionKey.OP_READ);
        this.connectedClients.add(clientChannel);
        getLogger().info(String.format("Accepted connection from %s", clientChannel.getRemoteAddress()));
    }

    /**
     * Read data from a client.
     * @param key the selection key.
     * @throws IOException if an I/O error occurs.
     */
    public Packet read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        if(clientChannel == null) { return null; }
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = clientChannel.read(buffer);

        if(read == -1) {
            getLogger().info(String.format("Connection closed by %s", clientChannel.getRemoteAddress()));
            this.connectedClients.remove(clientChannel);
            key.cancel();
            clientChannel.close();
            return null;
        }
        
        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return Utils.deserializePacket(data);
    }

    /**
     * Checks if the server is running. To consider the server to be running,
     * a check is done on see if both the server channel and the selector are open,
     * as well as the selector managing key operations.
     * @return {@code true} if the server is running, {@code false} otherwise.
     */
    public boolean isRunning() {
        return ( this.getChannel().isOpen() && this.getSelector().isOpen() ) && getState().get();
    }

    /**
     * Checks if the server is open. This means that the server channel and the selector are open.
     * But doesn't guarantee that the server is running, i.e. managing key operations (accept, read, write).
     * @return {@code true} if the server is reachable via internet, {@code false} otherwise.
     */
    public boolean isOpen() {
        return this.getChannel().isOpen() && this.getSelector().isOpen();
    }

    /**
     * Send a packet to a client.
     * @param client the client to send the packet to.
     * @param packet the packet to send.
     * @throws IOException if an I/O error occurs.
     */
    public void send(SocketChannel client, Packet packet) throws IOException {
        if(client == null) return;
        if(client.isOpen() && client.isConnected())
            client.write(ByteBuffer.wrap(packet.serialize()));
    }

    /**
     * Send a packet to all connected clients.
     * @param packet the packet to send.
     */
    public void sendToAll(Packet packet) {
        this.connectedClients.forEach(client -> {
            try {
                send(client, packet);
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Error sending packet to client.", e);
            }
        });
    }


    /**
     * Broadcast a message to all connected clients.
     * @param message the message to broadcast.
     */
    public void broadcastMessage(String message) {
        this.sendToAll(new MSGPacket(message));
        getLogger().info(String.format("Broadcasting: %s", message));
    }

    /**
     * Get a list of connected clients.
     * @return a list of connected clients.
     */
    public List<SocketChannel> getConnectedClients() {
        return this.connectedClients;
    }

    /**
     * Stops the server.
     */
    @Override
    public void stop() {
        try {
            if(getState().get()) {
                getState().set(false);
                this.getSelector().wakeup();
                this.getSelector().close();
                this.getChannel().close();
                this.closeObservers();
                stopConsole();
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error stopping server.", e);
        }
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
