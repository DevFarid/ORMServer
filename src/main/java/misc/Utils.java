package misc;

import hive.packets.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static Packet deserializePacket(byte[] data) {
        // Implement your deserialization logic here
        // For simplicity, assuming the data is serialized using a specific format
        // You may use libraries like Jackson or Gson for serialization/deserialization
        // For example, using a simple custom format: "CREATE|SomeData"
        String[] parts = new String(data).split("\\|");
        PacketType type = PacketType.valueOf(parts[0]);
        return switch (type) {
            case MESSAGE -> new MSGPacket(parts[1]);
            case SQL -> new DBPacket();
        };

    }

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
