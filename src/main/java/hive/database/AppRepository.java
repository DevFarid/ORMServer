package hive.database;

import com.j256.ormlite.support.ConnectionSource;
import hive.packets.DBPacket;
import misc.ReflectionUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
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

    public AppRepository(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
        try {
            this.initializeEntities();
        } catch (URISyntaxException e) {
            this.logger.log(Level.SEVERE, "Error initializing entities.", e);
        }
    }

    private void initializeEntities() throws URISyntaxException {
        String entitiesFolder = String.format("%s/src/main/java/hive/database/entities", Paths.get("").toAbsolutePath());
        File entitiesDirectory = new File(entitiesFolder);
        List<File> entityFiles = ReflectionUtil.findJavaFiles(entitiesDirectory);

    }

    /**
     * Decompose the packet into the database.
     * Once the packet is decomposed, it will be identified as to what
     * type of packet it is, so that the operation can be performed accordingly.
     * @param packet packet to decompose.
     */
    public void decompose(DBPacket packet) {

    }

    public static void main(String[] args) {
    }
}
