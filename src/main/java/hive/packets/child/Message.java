package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;

import java.nio.ByteBuffer;

/**
 * Represents a packet for sending messages between server & client.
 * Created by SixEyes on 06/03/2024.
 */
public class Message extends Packet {
    private final String message;

    public Message(String message) {
        super(PacketType.MESSAGE);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public byte[] serialize() {
        byte[] packetData = String.format("%s|%s", this.getType(), this.message).getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + packetData.length);
        buffer.putInt(packetData.length).put(packetData);
        return buffer.array();
    }


    @Override
    public String toString() {
        return String.format("{PacketType: %s, Message: %s}", this.getType(), this.message);
    }
}
