package hive.event;

// Define a listener interface
public interface NetworkEventListener {
    void onMessageReceived(NetworkEvent event);
}
