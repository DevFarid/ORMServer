package hive.sql.cmdbuilder;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.SqlType;
import hive.sql.QueryBuilder;

public class AlterBuilder extends QueryBuilder {
    private String cursor;
    /**
     * Constructs a new QueryBuilder object.
     */
    public AlterBuilder() {
        super(SQLCommandType.ALTER_TABLE);
    }

    @Override
    public AlterBuilder table(String table) {
        return (AlterBuilder) super.table(table);
    }

    public AlterBuilder add(String new_column_name, SqlType dataType) {
        this.cursor = String.format("ADD COLUMN %s %s", new_column_name, dataType);
        return this;
    }

    @Override
    public AlterBuilder columns(String... columns) {
        if(columns.length > 1) throw new IllegalArgumentException("Only one column can be altered at a time.");
        return (AlterBuilder) super.columns(columns);
    }

    /**
     * Specifies the column to be altered.
     * @param oldColumn the old name of the column.
     * @return The new modified AlterBuilder object.
     */
    public AlterBuilder column(String oldColumn) {
        this.columns(oldColumn);
        return this;
    }

    /**
     * Specifies the new name for the column.
     * @param newColumn the new name for the column.
     * @return The new modified AlterBuilder object.
     */
    public AlterBuilder to(String newColumn) {
        this.cursor = String.format("RENAME COLUMN %s TO %s", getColumns()[0], newColumn);
        return this;
    }

    /**
     * Renames the table to a new name.
     * @param renameTestEntity the new name for the table.
     * @return The new modified AlterBuilder object.
     */
    public AlterBuilder renameTo(String renameTestEntity) {
        this.cursor = String.format("RENAME TO %s", renameTestEntity);
        return this;
    }

    /**
     * Builds the query.
     * @return The query string.
     */
    @Override
    public String toString() {
        return String.format("%s %s %s;", getCommandType().getSQL(), getTable(), this.cursor);
    }

    /**
     * Creates a new SelectBuilder object.
     * @return The new SelectBuilder object.
     */
    public static AlterBuilder builder() {
        return SQLCommandType.ALTER_TABLE.getBuilderInstance();
    }
}
