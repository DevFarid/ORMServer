package hive.database;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DBConnection.class);
    private final Logger logger = Logger.getLogger(DBConnection.class.getName());
    private final ConnectionSource connectionSource;

    public DBConnection(DBEnv env) throws SQLException {
        this.connectionSource = new JdbcPooledConnectionSource(env.getDatabaseUrl());
    }

    public ConnectionSource getConnectionSource() {
        return this.connectionSource;
    }

    public void close() {
        try {
            this.connectionSource.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e.getCause());
        }
    }
}
