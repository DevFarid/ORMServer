package hive.sql.cmdbuilder;

import hive.sql.QueryBuilder;

public class AlterBuilder extends QueryBuilder {
    /**
     * Constructs a new QueryBuilder object.
     */
    public AlterBuilder() {
        super(SQLCommandType.ALTER);
    }

    @Override
    public String toString() {
        return "";
    }
}
