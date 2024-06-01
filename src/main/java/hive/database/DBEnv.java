package hive.database;

public enum DBEnv {
    DEV, BETA, PROD;

    public String getDatabaseUrl() {
        return switch (this) {
            case DEV, PROD, BETA -> String.format("jdbc:sqlite:\\SQLite\\%s\\" + "data" + ".db", this.name().toLowerCase());
            default -> throw new IllegalArgumentException("Unknown environment: " + this);
        };
    }
}
