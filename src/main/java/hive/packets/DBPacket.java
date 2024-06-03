package hive.packets;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a packet for database operations.
 * Created by SixEyes on 06/03/2024.
 */
public class DBPacket extends Packet {
    private final String tableName;
    private final Map<String, String> columns;  // Column name and its value
    private String condition;
    private final SQLCommandType commandType;

    public DBPacket(String tableName, SQLCommandType commandType) {
        super(PacketType.SQL);
        this.tableName = tableName;
        this.commandType = commandType;
        this.columns = new HashMap<>();
    }

    public void addColumn(String columnName, String value) {
        columns.put(columnName, value);
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTableName() {
        return tableName;
    }

    public Map<String, String> getColumns() {
        return columns;
    }

    public String getCondition() {
        return condition;
    }

    public SQLCommandType getCommandType() {
        return commandType;
    }

    @Override
    public byte[] serialize() {
        if (condition != null && !condition.isEmpty()) {
            return String.format("%s|%s|%s|%s|%s", this.getType(), this.commandType, this.tableName, this.columns, this.condition).getBytes();
        } else {
            return String.format("%s|%s|%s|%s", this.getType(), this.commandType, this.tableName, this.columns).getBytes();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Command Type: ").append(commandType).append("\n");
        builder.append("Table: ").append(tableName).append("\n");
        builder.append("Columns: ").append(columns).append("\n");
        if (condition != null && !condition.isEmpty()) {
            builder.append("Condition: ").append(condition).append("\n");
        }
        return builder.toString();
    }
}
