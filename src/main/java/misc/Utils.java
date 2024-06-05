package misc;

import hive.packets.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public class Utils {

    /**
     * Deserializes a byte array into an object of {@code Packet} class.
     * @param data the byte array to deserialize.
     * @return the packet object deserialized from the byte array.
     */
    public static Packet deserializePacket(byte[] data) {
        String[] parts = new String(data).split("\\|");
        PacketType type = PacketType.valueOf(parts[0]);
        switch (type) {
            case MESSAGE:
                return new MSGPacket(parts[1]);
            case SQL:
                DBPacket packet = new DBPacket(parts[2], SQLCommandType.valueOf(parts[1]));
                boolean hasColumns = parts[3].isEmpty();

                if (!hasColumns) {
                    String[] columns = parts[3].split(",");
                    for (String column : columns) {
                        String[] keyValue = column.split("=");
                        packet.addColumn(keyValue[0], keyValue[1]);
                    }
                }

                if (parts.length > 4) {
                    packet.setCondition(parts[4]);
                }

                return packet;
            default:
                throw new IllegalArgumentException("Invalid packet type: " + type);
        }
    }

    /**
     * Pretty prints a list of {@code SocketChannel} clients' remote address.
     * @param clients the list of clients to print the remote address of
     * @return a string representation of the clients' remote address as a collection.
     * @throws IOException if an I/O error occurs.
     */
    public static String prettyPrintClients(List<SocketChannel> clients) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int i = 0;
        for (SocketChannel client : clients) {
            sb.append(client.getRemoteAddress());
            if(i < clients.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
