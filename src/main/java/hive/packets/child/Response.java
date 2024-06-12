package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;

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
        return String.format("%s|", this.getType())
                .getBytes();
    }

    @Override
    public String toString() {
        return "{Response:}";
    }
}
