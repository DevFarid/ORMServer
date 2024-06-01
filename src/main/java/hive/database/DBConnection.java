package hive.database;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A database connection class that holds the connection source and the repository.
 * The repository is used to interact with the database in an ORM/DAO manner.
 * Created by SixEyes on 2024-06-01.
 */
public class DBConnection {
    private final Logger logger = Logger.getLogger(DBConnection.class.getName());
    private final ConnectionSource connectionSource;
    private final AppRepository appRepository;

    public DBConnection(DBEnv env) throws SQLException {
        this.connectionSource = new JdbcPooledConnectionSource(env.getDatabaseUrl());
        this.appRepository = new AppRepository(this.connectionSource);
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
    public AppRepository getAppRepository() {
        return appRepository;
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
