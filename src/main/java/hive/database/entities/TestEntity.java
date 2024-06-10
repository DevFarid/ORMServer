package hive.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import hive.database.AbstractEntityClass;

@DatabaseTable(tableName = "TestEntity")
public class TestEntity extends AbstractEntityClass {

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false, unique = true)
    public int id;

    @DatabaseField(columnName = "name", canBeNull = false)
    public String name;

    @DatabaseField(columnName = "age", canBeNull = false)
    public int age;

    @DatabaseField(columnName = "salary")
    public float salary;

    public TestEntity() {
    }

    public TestEntity(String name, int age, float salary) {
        this.name = name;
        this.age = age;
        this.salary = salary;
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
