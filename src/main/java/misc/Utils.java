package misc;

import hive.packets.*;

import java.io.IOException;

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
                String[] columns = parts[3].split(",");
                for (String column : columns) {
                    String[] keyValue = column.split(":");
                    packet.addColumn(keyValue[0], keyValue[1]);
                }
                if (parts.length > 4) {
                    packet.setCondition(parts[4]);
                }
                return packet;
            default:
                throw new IllegalArgumentException("Invalid packet type: " + type);
        }

    }
}
