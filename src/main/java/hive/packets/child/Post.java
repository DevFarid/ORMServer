package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;

import java.nio.ByteBuffer;

/**
 * A post is an act of sending an action to a receiver.
 * Created by SixEyes on 06/11/24.
 */
public class Post extends Packet {

    private final String commandAction;

    public Post(String commandAction) {
        super(PacketType.POST);
        this.commandAction = commandAction;
    }

    public String getCommand() {
        return this.commandAction;
    }

    @Override
    public byte[] serialize() {
        byte[] packetData = String.format("%s|%s", this.getType(), this.getCommand()).getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + packetData.length);
        buffer.putInt(packetData.length).put(packetData);
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("{Post:%s}", this.getCommand());
    }
}
