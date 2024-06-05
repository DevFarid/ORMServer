package hive;

import hive.console.Console;
import hive.packets.MSGPacket;
import hive.packets.Packet;
import misc.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.Iterator;

public class HiveClient extends Console {

    public HiveClient(int port) throws IOException {
        super(false, port);
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
                        }

                        // Register
                        channel.register(getSelector(), SelectionKey.OP_READ);
                        getLogger().info(String.format("Connected to server %s", channel.getRemoteAddress()));

                        sendPacket(new MSGPacket("New client connected."));
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

    protected Packet read() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = ((SocketChannel) this.getChannel()).read(buffer);
        if (read == -1) {
            getLogger().info("Server has closed the connection.");
            this.getChannel().close();
            return null;
        }

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        Packet packet = Utils.deserializePacket(data);
        getLogger().info(String.format("Received packet from server: %s", packet));

        return packet;
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
