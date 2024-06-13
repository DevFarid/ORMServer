package hive.database.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import hive.database.AbstractEntityClass;
import misc.Utils;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

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
        super();
    }

    public TestEntity(String name, int age, float salary) {
        super();
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

    @Override
    public AbstractEntityClass of(String... params) {
        AtomicInteger requiredFields = new AtomicInteger();
        Arrays.stream(this.getClass().getDeclaredFields()).forEach(field -> {
            if(field.isAnnotationPresent(DatabaseField.class)) {
                if(!field.getAnnotation(DatabaseField.class).generatedId()) {
                    requiredFields.getAndIncrement();
                }
            }
        });
        Utils.mustMatch(params, requiredFields.get());
        return new TestEntity(params[0], Integer.parseInt(params[1]), Float.parseFloat(params[2]));
    }
}
