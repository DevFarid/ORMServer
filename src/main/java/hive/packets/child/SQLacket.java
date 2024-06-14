package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;

import java.nio.ByteBuffer;

/**
 * Represents a packet for SQL database operations.
 * Created by SixEyes on 06/03/2024.
 */
public class SQLacket extends Packet {


    public SQLacket() {
        super(PacketType.SQL);
    }

    @Override
    public byte[] serialize() {
        byte[] data = String.format("%s|", this.getType()).getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + data.length);
        buffer.putInt(data.length).put(data);
        return buffer.array();
    }

    @Override
    public String toString() {
        return "{dbPacket: }";
    }
}
