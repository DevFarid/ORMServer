package hive.sql;

import hive.packets.SQLCommandType;
import misc.Utils;

import java.util.logging.Logger;

/**
 * This class is used to build SQL queries.
 * Created by SixEyes on 06/07/2024.
 */
public class QueryBuilder {
    private final Logger logger = Logger.getLogger(QueryBuilder.class.getName());
    String[] columns;
    String table;
    WhereAttacher whereAttacher = WhereAttacher.builder();
    SQLCommandType commandType;

    /**
     * Selects the columns to be queried.
     * @param columns The columns to be queried.
     * @return The new modified QueryBuilder object.
     */
    public QueryBuilder select(String... columns) {
        try {
            Utils.strArrayNNorNE(columns);
            this.columns = columns;
            this.commandType = SQLCommandType.SELECT;
            return this;
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Selects distinct rows to be queried.
     * @param columns The columns to select.
     * @return The new modified QueryBuilder object.
     */
    public QueryBuilder selectDistinct(String... columns) {
        try {
            Utils.strArrayNNorNE(columns);
            this.columns = columns;
            this.commandType = SQLCommandType.SELECT_DISTINCT;
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
    public QueryBuilder from(String table) {
        try {
            Utils.strNNorNE(table);
            this.table = table;
            return this;
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Adds a single where clause to the query.
     * @param where The where clause to add.
     * @return The new modified QueryBuilder object.
     */
    public QueryBuilder where(Where where) {
        this.whereAttacher.add(where, null);
        return this;
    }

    /**
     * Adds a compounding where clause to the query.
     * @param where The where clause to add.
     * @param conjugation The conjunction to use.
     * @return The new modified QueryBuilder object.
     */
    public QueryBuilder where(Where where, ComparisonOp conjugation) {
        this.whereAttacher.add(where, conjugation);
        return this;
    }

    /**
     * Builds the query.
     * @return The query string.
     */
    @Override
    public String toString() {
        StringBuilder query = new StringBuilder();
        query.append(commandType.getSQL());
        query.append(String.format(" %s", String.join(", ", columns)));
        query.append(" FROM ").append(table);
        if (this.whereAttacher != null && this.whereAttacher.hasConditions()) {
            query.append(" WHERE ").append(this.whereAttacher);
        }
        return query.append(";").toString();
    }

    /**
     * Creates a new QueryBuilder object.
     * @return The new QueryBuilder object.
     */
    public static QueryBuilder builder() {
        return new QueryBuilder();
    }
}
