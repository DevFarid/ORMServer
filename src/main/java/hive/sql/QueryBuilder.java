package hive.sql;

import hive.packets.SQLCommandType;
import misc.Utils;

import java.util.logging.Logger;

public class QueryBuilder {

    private final Logger logger = Logger.getLogger(QueryBuilder.class.getName());

    String[] columns;
    String table;
    WhereAttacher whereAttacher;
    SQLCommandType commandType;

    public QueryBuilder() {}

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

    public String toString() {
        StringBuilder query = new StringBuilder();
        query.append(commandType.getSQL());
        query.append(String.format(" %s", String.join(", ", columns)));
        query.append(" FROM ").append(table);
        if (this.whereAttacher != null && this.whereAttacher.hasConditions()) {
            query.append(" WHERE ").append(this.whereAttacher);
        }
        return query.toString();
    }

    public QueryBuilder where(WhereAttacher whereAttacher) {
        this.whereAttacher = whereAttacher;
        return this;
    }

}
