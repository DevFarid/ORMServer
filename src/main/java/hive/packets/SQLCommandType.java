package hive.packets;

/**
 * Enumerates the different types of SQL commands that can be sent to the server.
 * Created by SixEyes on 06/03/2024.
 */
public enum SQLCommandType {
    SELECT,
    SELECT_DISTINCT,
    UPDATE,
    DELETE,
    INSERT_INTO,
    CREATE_DATABASE,
    ALTER_DATABASE,
    CREATE_TABLE,
    ALTER_TABLE,
    DROP_TABLE,
    CREATE_INDEX,
    DROP_INDEX;

    public String getSQL() {
        return name().replace("_", " ");
    }
}
