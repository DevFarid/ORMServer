package hive.packets;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a packet for database operations.
 * Created by SixEyes on 06/03/2024.
 */
public class DBPacket extends Packet {
    private final String tableName;
    private final Map<String, String> columns = new HashMap<>();  // Column name and its value
    private String condition = null;
    private final SQLCommandType commandType;

    public DBPacket(String tableName, SQLCommandType commandType) {
        super(PacketType.SQL);
        this.tableName = tableName;
        this.commandType = commandType;
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
        return this.columns;
    }

    public String getCondition() {
        return this.condition;
    }

    public SQLCommandType getCommandType() {
        return this.commandType;
    }

    @Override
    public byte[] serialize() {
        String columns = this.columns.entrySet().stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);
        if (condition != null && !condition.isEmpty()) {
            return String.format("%s|%s|%s|%s|%s", this.getType(), this.commandType, this.tableName, columns, this.condition).getBytes();
        } else {
            return String.format("%s|%s|%s|%s", this.getType(), this.commandType, this.tableName, columns).getBytes();
        }
    }

    @Override
    public String toString() {
        return String.format("{SQLCMD: %s, Table: %s, Columns: %s, Condition: %s}", commandType, tableName, columns, condition);
    }
}
