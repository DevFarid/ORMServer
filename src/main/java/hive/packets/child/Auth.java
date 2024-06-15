package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;
import misc.Utils;

import java.nio.ByteBuffer;

/**
 * AuthPacket is a packet that is sent to the server to authenticate the client.
 * Created by SixEyes on 06/09/2024.
 */
public class Auth extends Packet {

    private String username;
    private String hashedPass;

    public Auth(String username, String hashedPass) {
        super(PacketType.AUTH);
        this.username = username;
        this.hashedPass = Utils.hashPassword(hashedPass);
    }

    private Auth() {
        super(PacketType.AUTH);
    }

    public static Auth of(String username, String hashedPass) {
        return new Auth().setUser(username).setPass(hashedPass);
    }

    private Auth setUser(String username) {
        this.username = username;
        return this;
    }

    private Auth setPass(String hashedPass) {
        this.hashedPass = hashedPass;
        return this;
    }

    public String getHashedPass() {
        return this.hashedPass;
    }

    @Override
    public byte[] serialize() {
        byte[] packetData = String.format("%s|%s|%s", this.getType(), this.username, this.hashedPass).getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + packetData.length);
        buffer.putInt(packetData.length).put(packetData);
        return buffer.array();
    }

    @Override
    public String toString() {
        return String.format("AuthPacket{username='%s'}", this.username);
    }
}
