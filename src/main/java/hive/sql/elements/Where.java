package hive.sql.elements;

import misc.Utils;

/**
 * This class is used to build SQL queries.
 * Created by SixEyes on 06/07/2024.
 */
public class Where {
    private String column;
    private ComparisonOp op;
    private String value;

    private Where(String column, ComparisonOp op, String value) {
        this.column = column;
        this.op = op;
        this.value = value;
    }

    private Where(String column, ComparisonOp op, String... value) {
        this.column = column;
        this.op = op;
        this.value(value);
    }

    public static Where of(String column, ComparisonOp op, String value) {
        return new Where(column, op, value);
    }

    public static Where of(String column, ComparisonOp op, String... value) {
        return new Where(column, op, value);
    }

    /**
     * Sets the column to be queried.
     * @param column The column to be queried.
     * @return The new modified Where object.
     */
    public Where column(String column) {
        this.column = column;
        return this;
    }

    /**
     * Specifies the comparison operator to be used.
     * @param op The comparison operator to be used.
     * @return The new modified Where object.
     */
    public Where op(ComparisonOp op) {
        this.op = op;
        return this;
    }

    /**
     * Specifies the value to be compared against.
     * @param value The value to be compared against.
     * @return The new modified Where object.
     */
    public Where value(String value) {
        this.value = value;
        return this;
    }

    /**
     * Specifies the values to be compared against.
     * @param values The values to be compared against.
     * @return The new modified Where object.
     */
    public Where value(String... values) {
        if(values.length > 0) {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < values.length; i++) {
                sb.append("'").append(values[i]).append("'");
                if (i < values.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
            this.value = sb.toString();
        }
        return this;
    }

    /**
     * Generates the where clause string.
     * @return The where clause string.
     */
    @Override
    public String toString() {
        if(value != null)
            return String.format("%s %s %s", column, op.getSymbol(), Utils.sqlFormatValue(value));
        else return String.format("%s %s", column, op.getSymbol());
    }

}
