package misc.querybuilder;

import hive.sql.cmdbuilder.SQLCommandType;
import hive.sql.cmdbuilder.SelectBuilder;
import hive.sql.elements.ComparisonOp;
import hive.sql.QueryBuilder;
import hive.sql.elements.Where;
import org.junit.jupiter.api.*;

/**
 * This class contains tests for the QueryBuilder class.
 * Created by SixEyes on 06/07/2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SelectTests {

    @Test
    @Order(1)
    @DisplayName("test-1: Select query w/o where clause")
    public void testColumns() {
        String query = QueryBuilder.builder(SQLCommandType.SELECT)
                .columns("id")
                .table("TestEntity")
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity;", query);
        System.out.println(query);
    }

    @Test
    @Order(2)
    @DisplayName("test-2: Select query w/ single where clause")
    public void testColumnsWhere() {
        String query = SelectBuilder.builder(false)
                .columns("id")
                .table("TestEntity")
                .where("name", ComparisonOp.EQUALS, "John")
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE name = John;", query);
        System.out.println(query);
    }

    @Test
    @Order(3)
    @DisplayName("test-3: Select query w/ multi select statements & one where clause")
    public void testMultiColumnsWhere() {
        String query = SelectBuilder.builder(false)
                .columns("id", "age")
                .table("TestEntity")
                .where("name", ComparisonOp.EQUALS, "John")
                .toString();
        Assertions.assertEquals("SELECT id, age FROM TestEntity WHERE name = John;", query);
        System.out.println(query);
    }

    @Test
    @Order(4)
    @DisplayName("test-4: Select query w/ two where clauses")
    public void testColumnsWhereMoreFilter() {
        String query = SelectBuilder.builder(false)
                .columns("id")
                .table("TestEntity")
                .where(Where.of("name", ComparisonOp.EQUALS, "John"), ComparisonOp.AND)
                .where(Where.of("age", ComparisonOp.GREATER_THAN, "18"), ComparisonOp.AND)
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE name = John AND age > 18;", query);
        System.out.println(query);
    }

    @Test
    @Order(5)
    @DisplayName("test-5: Select query w/ two where clauses")
    public void testColumnsWhereConjugatedFilter() {
        String query = SelectBuilder.builder(false)
                .columns("id")
                .table("TestEntity")
                .where(Where.of("name", ComparisonOp.EQUALS, "John"), ComparisonOp.AND)
                .where(Where.of("age", ComparisonOp.GREATER_THAN, "18"), ComparisonOp.OR)
                .where(Where.of("salary", ComparisonOp.GREATER_THAN, "10000"), null)
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE name = John AND age > 18 OR salary > 10000;", query);
        System.out.println(query);
    }

    @Test
    @Order(6)
    @DisplayName("test-6: test where in clause")
    public void testWhereIn() {
        String query = SelectBuilder.builder(false)
                .columns("id")
                .table("TestEntity")
                .where("name", ComparisonOp.IN, "Bob", "Fred", "Harry")
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE name IN ('Bob', 'Fred', 'Harry');", query);
        System.out.println(query);
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("test-7: test where is not null clause")
    public void testWhereIsNotNull() {
        String query = SelectBuilder.builder(false)
                .columns("id")
                .table("TestEntity")
                .where("name", ComparisonOp.IS_NOT_NULL)
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE name IS NOT NULL;", query);
        System.out.println(query);
    }

    @Test
    @Order(8)
    @DisplayName("test-8: test select distinct query")
    public void testColumnsDistinct() {
        String query = SelectBuilder.builder(true)
                .columns("name")
                .table("TestEntity")
                .where("age", ComparisonOp.GREATER_THAN_OR_EQUALS, "18")
                .toString();
        Assertions.assertEquals("SELECT DISTINCT name FROM TestEntity WHERE age >= 18;", query);
        System.out.println(query);
    }

    @Test
    @Order(9)
    @DisplayName("test-9: test select with order by clause")
    public void testColumnsOrderBy() {
        String query = SelectBuilder.builder(false)
                .columns("name", "age")
                .table("TestEntity")
                .orderBy("salary", false)
                .orderBy("age", true)
                .toString();
        Assertions.assertEquals("SELECT name, age FROM TestEntity ORDER BY salary DESC, age ASC;", query);
        System.out.println(query);
    }
}
