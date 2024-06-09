package hive.sql;

import hive.sql.cmdbuilder.SQLCommandType;
import misc.Utils;

import java.util.logging.Logger;

/**
 * This class is used to build SQL queries.
 * Created by SixEyes on 06/07/2024.
 */
public abstract class QueryBuilder {
    private final Logger logger = Logger.getLogger(QueryBuilder.class.getName());
    private String[] columns;
    private String table;
    private final SQLCommandType commandType;

    /**
     * Constructs a new QueryBuilder object.
     * @param commandType The type of SQL command to build.
     */
    public QueryBuilder(SQLCommandType commandType) {
        this.commandType = commandType;
    }

    /**
     * Selects the columns to be queried.
     * @param columns The columns to be queried.
     * @return The new modified QueryBuilder object.
     */
    public QueryBuilder columns(String... columns) {
        try {
            Utils.strArrayNNorNE(columns);
            this.columns = columns;
            return this;
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Specifies the table to query from.
     * @param table The table to query from.
     * @return The new modified QueryBuilder object.
     */
    public QueryBuilder table(String table) {
        try {
            Utils.strNNorNE(table);
            this.table = table;
            return this;
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
        }
        return null;
    }

    public abstract String toString();

    /**
     * Gets the type of SQL command to build.
     * @return The type of SQL command to build.
     */
    public SQLCommandType getCommandType() {
        return this.commandType;
    }

    /**
     * Gets the columns to be queried.
     * @return The columns to be queried.
     */
    public String[] getColumns() {
        return this.columns;
    }

    /**
     * Gets the table to query from.
     * @return The table to query from.
     */
    public String getTable() {
        return this.table;
    }

    /**
     * Creates a new QueryBuilder object.
     * @return The new QueryBuilder object.
     */
    public static <T extends QueryBuilder> T builder(SQLCommandType commandType) {
        return commandType.getBuilderInstance();
    }
}
