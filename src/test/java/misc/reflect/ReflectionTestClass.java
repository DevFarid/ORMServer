package misc.reflect;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "ReflectionTestClass")
public class ReflectionTestClass {

    @DatabaseField(columnName = "id", generatedId = true, canBeNull = false, unique = true)
    public int id;
    public String name;
    public float price;

    public ReflectionTestClass() {
    }
}
