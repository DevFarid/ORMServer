package hive;

import hive.commands.CMDLoader;
import hive.database.Database;
import hive.database.Environment;
import hive.console.Console;
import hive.packets.child.*;
import hive.packets.Packet;
import misc.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * A java NIO server. Handles multiple clients using multiple threads.
 * The server can be stopped by typing "stop" in the console.
 * Created by SixEyes on 2024-04-07.
 */
public class Server extends Console implements AutoCloseable {

    private final Map<SocketChannel, Boolean> connectedClients = new HashMap<>();
    private final Database database;
    private final AtomicReference<String> passphrase = new AtomicReference<>();

    public Server(Environment env, int port) throws IOException, SQLException, IllegalArgumentException {
        super(true, port);
        addCommands(CMDLoader.SERVER.loadCommands(this));

        try {
            this.database = new Database(env);
        } catch (IllegalArgumentException e) {
            getLogger().log(Level.SEVERE, "Error authenticating.", e);
            throw e;
        }
        this.setupHandShakeKey();
    }

    private void setupHandShakeKey() {
        this.passphrase.set(
                Utils.hashPassword(
                        Utils.readFileRaw(
                                String.format(
                                        "%s/key.txt", Paths.get("").toAbsolutePath()
                                )
                        )
                )
        );
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
                                        Message message = (Message) receivedPacket;
                                        getLogger().info(String.format("[%s](MSGPacket): %s", clientChannel.getRemoteAddress(), message.getMessage()));
                                    }
                                    case SQL -> {
                                        if(!this.connectedClients.get(clientChannel)) {
                                            send(clientChannel, new Message("You are not authenticated."));
                                            break;
                                        }

                                        getLogger().info(String.format("[%s](DBPacket): %s", clientChannel.getRemoteAddress(), receivedPacket));
                                        this.database.decomposePacket((SQLacket) receivedPacket);
                                    }
                                    case AUTH -> {
                                        getLogger().info(String.format("[%s](AuthPacket): %s", clientChannel.getRemoteAddress(), receivedPacket));
                                        Auth auth = (Auth) receivedPacket;

                                        if(auth.getHashedPass().equals(this.passphrase.get())) {
                                            this.connectedClients.remove(clientChannel, true);
                                            getLogger().info(String.format("Authenticated %s", clientChannel.getRemoteAddress()));
                                        } else {
                                            getLogger().info(String.format("Failed to authenticate %s", clientChannel.getRemoteAddress()));
                                        }
                                    }
                                    // received a post from client
                                    case POST -> {
                                        Post post = (Post) receivedPacket;
                                        getLogger().info(String.format("[%s](POST): %s", clientChannel.getRemoteAddress(), post));
                                        if (post.getCommand().equals("db_synchronize")) {
                                            Response response = new Response();
                                            // TODO: send all available table entities to the client along with their respective Java class.
                                            send(clientChannel, response);
                                        }
                                    }
                                    // received a response from client
                                    case RESPONSE -> {

                                    }
                                }
                                notifyListeners(receivedPacket, getLogger());
                            }
                        }
                    }
                } catch (IOException | SQLException e) {
                    if(e instanceof IOException) {
                        getLogger().log(Level.SEVERE, "Error selecting.", e);
                    } else {
                        getLogger().log(Level.SEVERE, "Error decomposing packet.", e);
                    }
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
        this.connectedClients.put(clientChannel, false);
        getLogger().info(String.format("[+]: %s", clientChannel.getRemoteAddress()));
    }

    /**
     * Read data from a client.
     * @param key the selection key.
     * @throws IOException if an I/O error occurs.
     */
    public Packet read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        if(clientChannel == null) { return null; }

        ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
        int bytesRead = clientChannel.read(lengthBuffer);

        if(bytesRead == -1) {
            getLogger().info(String.format("[-]: %s", clientChannel.getRemoteAddress()));
            this.connectedClients.remove(clientChannel);
            key.cancel();
            clientChannel.close();
            return null;
        }

        if(bytesRead < Integer.BYTES) {
            getLogger().warning(String.format("[!] Packet length byte data was not sent properly from %s", clientChannel.getRemoteAddress()));
            return null;
        }

        lengthBuffer.flip();
        int length = lengthBuffer.getInt();

        ByteBuffer dataBuffer = ByteBuffer.allocate(length);
        bytesRead = clientChannel.read(dataBuffer);

        if (bytesRead == -1) {
            getLogger().info(String.format("[!] Packet data byte was not sent properly from %s.", clientChannel.getRemoteAddress()));
            return null;
        }

        while(bytesRead < length) {
            bytesRead += clientChannel.read(dataBuffer);
        }

        dataBuffer.flip();
        byte[] data = new byte[dataBuffer.remaining()];
        dataBuffer.get(data);
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
        this.connectedClients.forEach((client, _) -> {
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
        Message m = new Message(message);
        this.sendToAll(m);
        getLogger().info(String.format("Broadcasting: %s", m));
    }

    /**
     * Get a list of connected clients.
     * @return a set of connected clients.
     */
    public Set<SocketChannel> getConnectedClients() {
        return this.connectedClients.keySet();
    }

    /**
     * Checks if the server can interact with data (i.e. crud operations on the database).
     * To interact with data, the server must be open, running, and the database connection must be open.
     * @return true if the satisfactory conditions are met, false otherwise.
     */
    public boolean canInteractWithData() {
        boolean[] canInteract = {
                this.isOpen(),
                this.isRunning(),
                this.database.getConnectionSource() != null,
                this.database.getConnectionSource().isOpen(this.database.getEnvironment().getDatabaseUrl())
        };

        for(boolean can : canInteract) {
            if(!can) return false;
        }

        return true;
    }

    /**
     * Stops the server.
     */
    @Override
    public void stop() {
        try {
            if(getState().get()) {
                getState().set(false);
                this.getSelector().wakeup().close();
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
        try(Server server = new Server(Environment.DEV, 25565)) {
            server.start();
        } catch(IOException e) {
            System.out.printf(
                "Error starting client.\nCause: %s\nTrace: %s\n", e.getCause(), e.fillInStackTrace()
            );
        }
    }
}
