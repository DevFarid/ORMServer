package misc;

import hive.packets.Packet;
import hive.packets.PacketType;

import java.io.IOException;

public class Utils {
    public static byte[] serialize(Packet packet) throws IOException {
        return (packet.getPacketType() + "|" + packet.getTable() + "|" + packet.getData()).getBytes();
    }

    public static Packet deserializePacket(byte[] data) {
        // Implement your deserialization logic here
        // For simplicity, assuming the data is serialized using a specific format
        // You may use libraries like Jackson or Gson for serialization/deserialization
        // For example, using a simple custom format: "CREATE|SomeData"
        String[] parts = new String(data).split("\\|");
        PacketType type = PacketType.valueOf(parts[0]);
        String table = parts[1];
        String packetData = parts[2];
        return new Packet(type, table, packetData);
    }
}
