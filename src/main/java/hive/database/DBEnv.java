package hive.database;

/**
 * Enum class to represent the different database environments.
 * Each environment has a different database URL.
 * Created by SixEyes on 06/01/2024.
 */
public enum DBEnv {
    DEV, BETA, PROD, LOSSLESS;

    public String getDatabaseUrl() {
        return switch (this) {
            case DEV, PROD, BETA -> String.format("jdbc:sqlite:\\SQLite\\%s\\" + "data" + ".db", this.name().toLowerCase());
            case LOSSLESS -> String.format("jdbc:sqlite::memory:\\SQLite\\%s\\" + "lossless" + ".db", this.name().toLowerCase());
        };
    }
}
