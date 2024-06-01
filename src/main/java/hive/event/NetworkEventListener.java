package hive.event;

/**
 * A network event listener that listens for new messages.
 * Created by SixEyes on 2024-05-01.
 */
public interface NetworkEventListener {
    void onMessageReceived(NetworkEvent event);
}
