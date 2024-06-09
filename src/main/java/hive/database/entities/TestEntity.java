package hive.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import hive.database.AbstractEntityClass;

import java.util.UUID;

@DatabaseTable(tableName = "TestEntity")
public class TestEntity extends AbstractEntityClass {

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false, unique = true)
    public UUID id;

    @DatabaseField(columnName = "name", canBeNull = false)
    public String name;

    @DatabaseField(columnName = "age", canBeNull = false)
    public int age;

    @DatabaseField(columnName = "salary")
    public float salary;

    public TestEntity() {
        this.id = UUID.randomUUID();
    }

    public TestEntity(String name, int age, float salary) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.age = age;
        this.salary = salary;
    }

    public UUID getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public float getSalary() {
        return this.salary;
    }
}
