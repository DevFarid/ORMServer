package hive.sql.cmdbuilder;

import hive.sql.*;
import hive.sql.elements.*;

/**
 * Created by SixEyes on 06/08/2024.
 */
public class SelectBuilder extends QueryBuilder {
    private final WhereAttacher whereAttacher = WhereAttacher.builder();
    private final OrderBuilder orderBuilder = OrderBuilder.builder();
    private boolean isDistinct = false;
    /**
     * Constructs a new SelectBuilder object.
     */
    public SelectBuilder(boolean isDistinct) {
        super(SQLCommandType.SELECT);
        this.isDistinct = isDistinct;
    }

    /**
     * default constructor for SelectBuilder, when not specified.
     */
    public SelectBuilder() {
        super(SQLCommandType.SELECT);
    }

    @Override
    public SelectBuilder columns(String... columns) {
        return (SelectBuilder) super.columns(columns);
    }

    @Override
    public SelectBuilder table(String table) {
        return (SelectBuilder) super.table(table);
    }

    /**
     * Adds a single where clause to the query.
     *
     * @param column the column to filter.
     * @param op the comparison operator.
     * @return The new modified SelectBuilder object.
     */
    public SelectBuilder where(String column, ComparisonOp op) {
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
    public SelectBuilder where(String column, ComparisonOp op, String value) {
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
    public SelectBuilder where(String column, ComparisonOp op, String... value) {
        this.whereAttacher.add(Where.of(column, op, value), null);
        return this;
    }

    /**
     * Adds a compounding where clause to the query.
     * @param where The where clause to add.
     * @param conjugation The conjunction to use.
     * @return The new modified SelectBuilder object.
     */
    public SelectBuilder where(Where where, ComparisonOp conjugation) {
        this.whereAttacher.add(where, conjugation);
        return this;
    }

    /**
     * Adds an order by clause to the query.
     *
     * @param column the column to order by.
     * @param ascending represents whether the sorting will be done in ascending order, false otherwise.
     * @return The new modified SelectBuilder object.
     */
    public SelectBuilder orderBy(String column, boolean ascending) {
        this.orderBuilder.add(Order.builder(column, ascending));
        return this;
    }

    /**
     * Builds the query.
     * @return The query string.
     */
    @Override
    public String toString() {
        StringBuilder query = new StringBuilder();
        query.append(getCommandType().getSQL());
        if(this.isDistinct) {
            query.append(" DISTINCT");
        }
        query.append(String.format(" %s", String.join(", ", getColumns())));
        query.append(" FROM ").append(getTable());
        if (this.whereAttacher.hasConditions()) {
            query.append(" WHERE ").append(this.whereAttacher);
        }
        if (this.orderBuilder.hasOrders()) {
            query.append(" ").append(this.orderBuilder);
        }
        return query.append(";").toString();
    }

    /**
     * Creates a new SelectBuilder object.
     * @return The new SelectBuilder object.
     */
    public static SelectBuilder builder(boolean isDistinct) {
        return SQLCommandType.SELECT.parameter(boolean.class).init(isDistinct).getBuilderInstance();
    }
}
