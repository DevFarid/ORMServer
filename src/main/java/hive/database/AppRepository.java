package hive.database;

import com.j256.ormlite.support.ConnectionSource;
import hive.packets.Packet;

/**
 * This class will manage entities within the database in a CRUD manner.
 * It will translate the database entities into java class objects.
 * Created by SixEyes on 2024-06-01.
 */
public class AppRepository {
    private final ConnectionSource connectionSource;

    public AppRepository(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    /**
     * Decompose the packet into the database.
     * Once the packet is decomposed, it will be identified as to what
     * type of packet it is, so that the operation can be performed accordingly.
     * @param packet packet to decompose.
     */
    public void decompose(Packet packet) {
    }

    public static void main(String[] args) {
    }
}
