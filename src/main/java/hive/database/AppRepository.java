package hive.database;

import com.j256.ormlite.support.ConnectionSource;

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
}
