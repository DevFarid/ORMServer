package misc;

import hive.packets.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

public class Utils {
    public static Packet deserializePacket(byte[] data) {
        // Implement your deserialization logic here
        // For simplicity, assuming the data is serialized using a specific format
        // You may use libraries like Jackson or Gson for serialization/deserialization
        // For example, using a simple custom format: "CREATE|SomeData"
        String[] parts = new String(data).split("\\|");
        PacketType type = PacketType.valueOf(parts[0]);
        switch (type) {
            case MESSAGE:
                return new MSGPacket(parts[1]);
            case SQL:
                DBPacket packet = new DBPacket(parts[2], SQLCommandType.valueOf(parts[1]));
                boolean hasColumns = parts[3].equals("{}");

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

    private static void strParameterNotNull(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Parameter cannot be null");
        }
    }

    private static void strParameterNotEmpty(String str) {
        if(str.isEmpty()) {
            throw new IllegalArgumentException("Parameter cannot be empty");
        }
    }

    private static void strArrayParameterNotEmpty(String[] strArray) {
        if(strArray.length == 0) {
            throw new IllegalArgumentException("Parameter array cannot be empty");
        }
        for(String str : strArray) {
            strParameterNotNull(str);
            strParameterNotEmpty(str);
        }
    }

    private static void strArrayParameterNotNull(String[] strArray) {
        if(strArray == null) {
            throw new IllegalArgumentException("Parameter array cannot be null");
        }
    }

    public static void strNNorNE(String str) {
        strParameterNotNull(str);
        strParameterNotEmpty(str);
    }

    public static void strArrayNNorNE(String[] strArray) {
        strArrayParameterNotNull(strArray);
        strArrayParameterNotEmpty(strArray);
    }
}
