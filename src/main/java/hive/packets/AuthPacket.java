package hive.packets;

import misc.Utils;

/**
 * AuthPacket is a packet that is sent to the server to authenticate the client.
 * Created by SixEyes on 06/09/2024.
 */
public class AuthPacket extends Packet {

    private String username;
    private String hashedPass;

    public AuthPacket(String username, String hashedPass) {
        super(PacketType.AUTH);
        this.username = username;
        this.hashedPass = Utils.hashPassword(hashedPass);
    }

    private AuthPacket() {
        super(PacketType.AUTH);
    }

    public static AuthPacket of(String username, String hashedPass) {
        return new AuthPacket().setUser(username).setPass(hashedPass);
    }

    private AuthPacket setUser(String username) {
        this.username = username;
        return this;
    }

    private AuthPacket setPass(String hashedPass) {
        this.hashedPass = hashedPass;
        return this;
    }

    public String getHashedPass() {
        return this.hashedPass;
    }

    @Override
    public byte[] serialize() {
        return String.format("%s|%s|%s", this.getType(), this.username, this.hashedPass).getBytes();
    }

    @Override
    public String toString() {
        return String.format("AuthPacket{username='%s'}", this.username);
    }
}
