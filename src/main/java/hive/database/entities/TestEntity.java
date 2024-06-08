package hive.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.UUID;

@DatabaseTable(tableName = "TestEntity")
public class TestEntity {

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false, unique = true)
    public UUID id;

    public TestEntity() {
        this.id = UUID.randomUUID();
    }
}
