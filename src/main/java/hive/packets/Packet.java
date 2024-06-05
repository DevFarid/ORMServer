package hive.packets;

import java.io.Serial;
import java.io.Serializable;

/**
 * Packet class is an abstract class that represents a packet that is sent between the server and the client.
 * Created by SixEyes on 04/07/2024.
 */
public abstract class Packet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final PacketType packetType;

    public abstract byte[] serialize();

    public Packet(PacketType packetType) {
        this.packetType = packetType;
    }

    public PacketType getType() {
        return this.packetType;
    }

    @Override
    public String toString() {
        return String.format("Packet{packetType=%s}", packetType);
    }
}
