package hive.event;

import hive.packets.Packet;

/**
 * A network event that holds the sender and the packet.
 * Created by SixEyes on 2024-05-01.
 */
public final class NetworkEvent {
    private final NetworkEventNotifier packetSender;
    private final Packet packet;

    public NetworkEvent(NetworkEventNotifier sender, Packet packet) {
        this.packetSender = sender;
        this.packet = packet;
    }

    public NetworkEventNotifier getSender() {
        return this.packetSender;
    }

    public Packet getPacket() {
        return this.packet;
    }
}
