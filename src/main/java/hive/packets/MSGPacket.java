package hive.packets;

/**
 * Represents a packet for sending messages between server & client.
 * Created by SixEyes on 06/03/2024.
 */
public class MSGPacket extends Packet {
    private final String message;

    public MSGPacket(String message) {
        super(PacketType.MESSAGE);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public byte[] serialize() {
        return String.format("%s|%s", this.getType(), this.message)
                .getBytes();
    }

    @Override
    public String toString() {
        return String.format("{PacketType: %s, Message: %s}", this.getType(), this.message);
    }
}
