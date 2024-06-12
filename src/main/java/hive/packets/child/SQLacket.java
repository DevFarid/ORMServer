package hive.packets.child;

import hive.packets.Packet;
import hive.packets.PacketType;

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
        return String.format("%s|", this.getType()).getBytes();
    }

    @Override
    public String toString() {
        return "{dbPacket: }";
    }
}
