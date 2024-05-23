package hive.packets;

import java.io.Serial;
import java.io.Serializable;
public class Packet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final PacketType packetType;
    private final String table;
    private final String data;

    public Packet(PacketType packetType, String table, String data) {
        this.packetType = packetType;
        this.table = table;
        this.data = data;
    }

    public PacketType getType() {
        return packetType;
    }

    public String getTable() {
        return table;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("Packet{packetType=%s, table='%s', data='%s'}", packetType, table, data);
    }
}
