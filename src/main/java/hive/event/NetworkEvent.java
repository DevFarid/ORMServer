package hive.event;

import hive.HiveClient;
import hive.packets.Packet;

// Define an event class
public class NetworkEvent {
    private final HiveClient client;
    private final Packet packet;

    public NetworkEvent(HiveClient client, Packet packet) {
        this.client = client;
        this.packet = packet;
    }

    public HiveClient getClient() {
        return client;
    }

    public Packet getPacket() {
        return packet;
    }
}
