package hive;

import hive.commands.CMDLoader;
import hive.console.Console;
import hive.packets.Packet;
import misc.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.Iterator;

/**
 * A client that connects to a server and sends/receives messages.
 * Created by SixEyes on 2024-04-07.
 */
public class HiveClient extends Console {
    public HiveClient(int port) throws IOException {
        super(false, port);
        addCommands(CMDLoader.CLIENT.loadCommands(this));
    }

    private void auth() throws IllegalArgumentException {
        this.runCommandIf("login", "admin",
                Utils.readFileRaw(
                        String.format(
                                "%s/key.txt", Paths.get("").toAbsolutePath()
                        )
                )
        );
    }

    /**
     * Starts the client and listens for messages from the server.
     */
    public void start() {
        getState().set(true);

        try {
            startConsole();
            while (getState().get()) {
                int selectedResult = this.getSelector().select();
                if (selectedResult == 0) {
                    continue;
                }

                Iterator<SelectionKey> keyIterator = this.getSelector().selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isConnectable()) {
                        SocketChannel channel = (SocketChannel) key.channel();

                        if (channel.isConnectionPending()) {
                            channel.finishConnect();

                            try {
                                this.auth();
                                this.synchronize();
                            } catch (IllegalArgumentException e) {
                                getLogger().log(Level.SEVERE, "Error authenticating.", e);
                                stop();
                            }
                        }

                        // Register
                        channel.register(getSelector(), SelectionKey.OP_READ);
                        getLogger().info(String.format("Connected to server %s", channel.getRemoteAddress()));

                    } else if(key.isReadable()) {
                        Packet packet = this.read();
                        if (packet != null) {
                            notifyListeners(packet, getLogger());
                        }
                    }
                }
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error starting client.", e);
        } finally {
            stop();
        }
    }

    private void synchronize() {

    }

    protected Packet read() throws IOException {

        ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
        int bytesRead = ((SocketChannel) this.getChannel()).read(lengthBuffer);
        if (bytesRead == -1) {
            getLogger().info("[!] Server has closed the connection.");
            this.getChannel().close();
            return null;
        }

        if(bytesRead < Integer.BYTES) {
            getLogger().warning("[!] Packet length byte was not sent properly from server.");
            return null;
        }

        lengthBuffer.flip();
        int length = lengthBuffer.getInt();

        ByteBuffer dataBuffer = ByteBuffer.allocate(length);
        bytesRead = ((SocketChannel) this.getChannel()).read(dataBuffer);

        if (bytesRead == -1) {
            getLogger().info("[!] Packet data byte was not sent properly from server.");
            return null;
        }

        dataBuffer.flip();
        byte[] data = new byte[dataBuffer.remaining()];
        dataBuffer.get(data);
        Packet packet = Utils.deserializePacket(data);
        getLogger().info(String.format("Received packet from server: %s", packet));

        return packet;
    }

    // method to send message to server
    public void sendPacket(Packet p) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(p.serialize());

            while (buffer.hasRemaining()) {
                ((SocketChannel) getChannel()).write(buffer);
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "error sending a packet!", e);
        }
    }

    /**
     * Stops the client.
     */
    @Override
    public void stop() {
        try {
            if (getState().get()) {
                getState().set(false);
                stopConsole();
                this.getSelector().close();
                this.getChannel().close();
                this.clearObservers();
            }
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error stopping client.", e);
        }
    }

    public static void main(String[] args) throws IOException {
        HiveClient client = new HiveClient(25565);
        client.start();
    }
}
