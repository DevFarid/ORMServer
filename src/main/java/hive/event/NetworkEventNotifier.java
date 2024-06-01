package hive.event;

import hive.packets.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A network event notifier that notifies all listeners of a new message.
 * Created by SixEyes on 2024-05-01.
 */
public abstract class NetworkEventNotifier {
    private final List<NetworkEventListener> listeners = new ArrayList<>();

    /**
     * Adds a network event listener to the notifier.
     * @param listener the new listener to listen to.
     */
    public void addNetworkEventListener(NetworkEventListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes a network event listener from the notifier.
     * @param listener the listener to remove.
     */
    public void removeNetworkEventListener(NetworkEventListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Notifies all listeners of a new message.
     * This fires the {@code onMessageReceived} event for all listeners.
     * @param packet the received packet.
     */
    protected void notifyListeners(Packet packet, final Logger logger) {
        NetworkEvent event = new NetworkEvent(this, packet);
        for (NetworkEventListener listener : listeners) {
            listener.onMessageReceived(event);
            logger.info(("OBSERVERS NOTIFIED"));
        }
    }

    protected void clearObservers() {
        this.listeners.clear();
    }
}

