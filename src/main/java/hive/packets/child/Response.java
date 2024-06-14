package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;

import java.nio.ByteBuffer;

/**
 * A packet that acts as a response to an act.
 * Created by SixEyes on 06/11/24.
 */
public class Response extends Packet {

    public Response() {
        super(PacketType.RESPONSE);
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
        return "{Response:}";
    }
}
