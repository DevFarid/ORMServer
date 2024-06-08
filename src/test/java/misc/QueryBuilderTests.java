package misc;

import hive.sql.ComparisonOp;
import hive.sql.QueryBuilder;
import hive.sql.Where;
import org.junit.jupiter.api.*;

/**
 * This class contains tests for the QueryBuilder class.
 * Created by SixEyes on 06/07/2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class QueryBuilderTests {

    @Test
    @Order(1)
    @DisplayName("test-1: Select query w/o where clause")
    public void testSelect() {
        String query = QueryBuilder.builder()
                .select("id")
                .from("TestEntity")
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity;", query);
        System.out.println(query);
    }

    @Test
    @Order(2)
    @DisplayName("test-2: Select query w/ single where clause")
    public void testSelectWhere() {
        String query = QueryBuilder.builder()
                .select("id")
                .from("TestEntity")
                .where(
                        new Where()
                                .column("name")
                                .op(ComparisonOp.EQUALS)
                                .value("John")
                )
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE name = John;", query);
        System.out.println(query);
    }

    @Test
    @Order(3)
    @DisplayName("test-3: Select query w/ multi select statements & one where clause")
    public void testMultiSelectWhere() {
        String query = QueryBuilder.builder()
                .select("id", "age")
                .from("TestEntity")
                .where(new Where()
                        .column("name")
                        .op(ComparisonOp.EQUALS)
                        .value("John")
                )
                .toString();
        Assertions.assertEquals("SELECT id, age FROM TestEntity WHERE name = John;", query);
        System.out.println(query);
    }

    @Test
    @Order(4)
    @DisplayName("test-4: Select query w/ two where clauses")
    public void testSelectWhereMoreFilter() {
        String query = QueryBuilder.builder()
                .select("id")
                .from("TestEntity")
                .where(new Where()
                                .column("name")
                                .op(ComparisonOp.EQUALS)
                                .value("John")
                        , ComparisonOp.AND)
                .where(new Where()
                                .column("age")
                                .op(ComparisonOp.GREATER_THAN)
                                .value("18")
                        ,null)
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE name = John AND age > 18;", query);
        System.out.println(query);
    }

    @Test
    @Order(5)
    @DisplayName("test-5: Select query w/ two where clauses")
    public void testSelectWhereConjugatedFilter() {
        String query = QueryBuilder.builder()
                .select("id")
                .from("TestEntity")
                .where(new Where()
                                .column("name")
                                .op(ComparisonOp.EQUALS)
                                .value("John")
                        , ComparisonOp.AND
                )
                .where(new Where()
                                .column("age")
                                .op(ComparisonOp.GREATER_THAN)
                                .value("18")
                        , ComparisonOp.OR)
                .where(new Where()
                                .column("salary")
                                .op(ComparisonOp.GREATER_THAN)
                                .value("10000")
                        , null)
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE name = John AND age > 18 OR salary > 10000;", query);
        System.out.println(query);
    }

    @Test
    @Order(6)
    @DisplayName("test-6: test where in clause")
    public void testWhereIn() {
        String query = QueryBuilder.builder()
                        .select("name")
                        .from("customers")
                        .where(new Where()
                                        .column("name")
                                        .op(ComparisonOp.IN)
                                        .value("Bob", "Fred", "Harry")
                                , null
                        )
                .toString();
        Assertions.assertEquals("SELECT name FROM customers WHERE name IN ('Bob', 'Fred', 'Harry');", query);
        System.out.println(query);
    }

    @Test
    @Order(7)
    @DisplayName("test-7: test where is not null clause")
    public void testWhereIsNotNull() {
        String query = QueryBuilder.builder()
                        .select("name")
                        .from("customers")
                                .where(
                                        new Where()
                                                .column("name")
                                                .op(ComparisonOp.IS_NOT_NULL)

                                )
                .toString();
        Assertions.assertEquals("SELECT name FROM customers WHERE name IS NOT NULL;", query);
        System.out.println(query);
    }
}
