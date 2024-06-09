package hive.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.spring.DaoFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import hive.packets.DBPacket;
import misc.ReflectionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class will manage entities within the database in a CRUD manner.
 * It will translate the database entities into java class objects.
 * Created by SixEyes on 2024-06-01.
 */
public class AppRepository {
    private final Logger logger = Logger.getLogger(AppRepository.class.getName());
    private final ConnectionSource connectionSource;
    private final Map<String, Dao<?, Object>> entities = new HashMap<>();
    private boolean canInteractWithData = false;

    public AppRepository(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
        try {
            this.initializeEntities();
            this.canInteractWithData = true;
        } catch (URISyntaxException | IOException | SQLException e) {
            this.logger.log(Level.SEVERE, "Error initializing entities.", e);
        } finally {
            this.canInteractWithData = false;
        }
    }

    /**
     * Initialize the entities within the database.
     * @throws URISyntaxException if the URI is invalid.
     * @throws IOException if an I/O error occurs.
     * @throws SQLException if a database access error occurs.
     */
    private void initializeEntities() throws URISyntaxException, IOException, SQLException {
        String entitiesFolder = String.format("%s/src/main/java/hive/database/entities", Paths.get("").toAbsolutePath());
        File entitiesDirectory = new File(entitiesFolder);
        List<File> entityFiles = ReflectionUtil.findJavaFiles(entitiesDirectory);
        ReflectionUtil.compileJavaFiles(entityFiles);

        List<Class<?>> loadedClasses = ReflectionUtil.loadClass(entityFiles);
        for(Class<?> clazz : loadedClasses) {
            TableUtils.createTableIfNotExists(this.connectionSource, clazz);
            this.entities.put(clazz.getSimpleName(), DaoFactory.createDao(this.connectionSource, clazz));
        }

        if(!this.entities.isEmpty()) {
            this.logger.info(String.format("Loaded %s(%s) as database entity.", this.entities.keySet(), this.entities.size()));
        }
    }

    /**
     * Get the DAO object for the given entity name.
     * @param entityName entity name.
     * @return the DAO object for the given entity name.
     */
    public Dao<?, Object> getDAO(String entityName) {
        return this.entities.getOrDefault(entityName, null);
    }

    /**
     * Decompose the packet into the database.
     * Once the packet is decomposed, it will be identified as to what
     * type of packet it is, so that the operation can be performed accordingly.
     * @param packet packet to decompose.
     */
    public void decompose(DBPacket packet) {
        switch (packet.getCommandType()) {
            case SELECT:
                this.select(packet);
                break;
            case ALTER_TABLE:
                this.alter(packet);
                break;
            default:
                this.logger.warning("Unknown command type.");
        }
    }

    private void select(DBPacket packet) {
    }

    private void alter(DBPacket packet) {
    }

    /**
     * Checks if the server can interact with data (i.e. crud operations on the database).
     * To interact with data, the server must be open, running, and the database connection must be open.
     * @return true if the satisfactory conditions are met, false otherwise.
     */
    public boolean canInteractWithData(String tableName) {
        return this.canInteractWithData && this.connectionSource.isOpen(tableName);
    }

    public static void main(String[] args) {
    }
}
