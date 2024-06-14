package hive.database;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import hive.packets.child.SQLacket;
import misc.Utils;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A database connection class that holds the connection source and the repository.
 * The repository is used to interact with the database in an ORM/DAO manner.
 * Created by SixEyes on 2024-06-01.
 */
public class Database {
    private final Logger logger = Logger.getLogger(Database.class.getName());
    private final Environment environment;
    private final JdbcConnectionSource connectionSource;
    private final Repository repository;
    private final static String username = "admin";

    public Database(Environment environment) throws SQLException, IllegalArgumentException {
        this.environment = environment;
        this.connectionSource = new JdbcPooledConnectionSource(this.environment.getDatabaseUrl());
        this.auth();
        this.repository = new Repository(this.connectionSource);
    }

    private void auth() throws IllegalArgumentException {
        this.connectionSource.setUsername(username);
        this.connectionSource.setPassword(
                Utils.hashPassword(Utils.randomString(16))
        );
    }

    /**
     * Get the connection source.
     * @return JDBC pooled {@code ConnectionSource} source.
     */
    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    /**
     * Get the app repository.
     * @return app repository instance.
     */
    public Repository getAppRepository() {
        return this.repository;
    }

    /**
     * Decompose the packet into the database.
     * @param packet packet to decompose.
     */
    public void decomposePacket(SQLacket packet) throws SQLException {
        this.repository.decompose(packet);
    }

    /**
     * Get the database environment.
     * @return database environment enum.
     */
    public Environment getEnvironment() {
        return this.environment;
    }

    /**
     * Close the connection source to prevent memory leaks.
     */
    public void close() {
        try {
            this.connectionSource.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
    }
}
