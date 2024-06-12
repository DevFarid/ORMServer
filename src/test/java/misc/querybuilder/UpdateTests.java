package misc.querybuilder;

import hive.sql.cmdbuilder.UpdateBuilder;
import hive.sql.elements.ComparisonOp;
import org.junit.jupiter.api.*;

/**
 * A test class that will test the update functionality of the query builder.
 * Created by SixEyes on 06/10/2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdateTests {

    @Test
    @Order(0)
    @DisplayName("test-0: Update column value based on a condition.")
    public void testUpdate() {
        String query = UpdateBuilder.builder()
                .table("TestEntity")
                .columns("salary")
                .set("salary * 1.10")
                .where("name", ComparisonOp.EQUALS, "John")
                .toString();
        Assertions.assertEquals("UPDATE TestEntity SET salary = salary * 1.10 WHERE name = 'John';", query);
        System.out.println(query);
    }

    @Test
    @Order(1)
    @DisplayName("test-1: Update multiple columns value based on a condition.")
    public void testUpdateTwo() {
        String query = UpdateBuilder.builder()
                .table("TestEntity")
                .columns("salary", "name")
                .set("salary * 1.10", "Franklin")
                .where("name", ComparisonOp.EQUALS, "John")
                .toString();
        Assertions.assertEquals("UPDATE TestEntity SET salary = salary * 1.10, name = 'Franklin' WHERE name = 'John';", query);
        System.out.println(query);
    }
}
