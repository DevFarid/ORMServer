package hive.sql.cmdbuilder;

import hive.sql.QueryBuilder;
import hive.sql.elements.ComparisonOp;
import hive.sql.elements.Where;
import hive.sql.elements.WhereAttacher;
import misc.Utils;

/**
 * Builds SQL update queries.
 * Created by SixEyes on 06/10/2024.
 */
public class UpdateBuilder extends QueryBuilder {
    private final WhereAttacher whereAttacher = WhereAttacher.builder();
    private String[] values;

    public UpdateBuilder() {
        super(SQLCommandType.UPDATE);
    }

    @Override
    public UpdateBuilder table(String table) {
        return (UpdateBuilder) super.table(table);
    }

    public UpdateBuilder columns(String... columns) {
        return (UpdateBuilder) super.columns(columns);
    }

    public UpdateBuilder set(String... value) {
        Utils.strArrayNNorNE(getColumns());
        Utils.strArrayNNorNE(value);
        Utils.mustMatch(getColumns(), value);
        this.values = value;
        return this;
    }

    /**
     * Adds a single where clause to the query.
     *
     * @param column the column to filter.
     * @param op the comparison operator.
     * @return The new modified SelectBuilder object.
     */
    public UpdateBuilder where(String column, ComparisonOp op) {
        this.whereAttacher.add(Where.of(column, op), null);
        return this;
    }

    /**
     * Adds a single where clause to the query.
     *
     * @param column the column to filter.
     * @param op the comparison operator.
     * @param value the value for the filter.
     * @return The new modified SelectBuilder object.
     */
    public UpdateBuilder where(String column, ComparisonOp op, String value) {
        this.whereAttacher.add(Where.of(column, op, value), null);
        return this;
    }

    /**
     * Adds a single where clause to the query.
     *
     * @param column the column to filter.
     * @param op the comparison operator.
     * @param value the value for the filter.
     * @return The new modified SelectBuilder object.
     */
    public UpdateBuilder where(String column, ComparisonOp op, String... value) {
        this.whereAttacher.add(Where.of(column, op, value), null);
        return this;
    }

    /**
     * Adds a compounding where clause to the query.
     * @param where The where clause to add.
     * @param conjugation The conjunction to use.
     * @return The new modified SelectBuilder object.
     */
    public UpdateBuilder where(Where where, ComparisonOp conjugation) {
        this.whereAttacher.add(where, conjugation);
        return this;
    }

    @Override
    public String toString() {
        Utils.strArrayNNorNE(getColumns());
        Utils.strArrayNNorNE(values);

        StringBuilder query = new StringBuilder();
        query.append(getCommandType().getSQL());
        query.append(" ").append(getTable());

        query.append(" SET ");
        for (int i = 0; i < getColumns().length; i++) {
            query.append(getColumns()[i]).append(" = ").append(Utils.sqlFormatValue(values[i]));
            if (i < getColumns().length - 1) {
                query.append(", ");
            }
        }

        if (this.whereAttacher.hasConditions()) {
            query.append(" WHERE ").append(this.whereAttacher);
        }

        return query.append(";").toString();
    }

    public static UpdateBuilder builder() {
        return new UpdateBuilder();
    }
}
