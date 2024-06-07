package hive.packets;

/**
 * Represents a packet for database operations.
 * Created by SixEyes on 06/03/2024.
 */
public class DBPacket extends Packet {

    public DBPacket() {
        super(PacketType.SQL);
    }

    @Override
    public byte[] serialize() {
        return String.format("%s|", this.getType()).getBytes();
    }

    @Override
    public String toString() {
        return "";
    }
}
