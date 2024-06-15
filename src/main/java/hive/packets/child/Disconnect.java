package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;

/**
 * Represents a packet for disconnecting from the server.
 * Created by SixEyes on 06/15/2024.
 */
public class Disconnect extends Packet {

    public Disconnect() {
        super(PacketType.DISCONNECT);
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }
}
