package misc;

import hive.sql.ComparisonOp;
import hive.sql.QueryBuilder;
import hive.sql.Where;
import hive.sql.WhereAttacher;
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
        String query = new QueryBuilder()
                .select("name")
                .from("users")
                .toString();
        Assertions.assertEquals("SELECT name FROM users", query);
        System.out.println(query);
    }

    @Test
    @Order(2)
    @DisplayName("test-2: Select query w/ single where clause")
    public void testSelectWhere() {
        String query = new QueryBuilder()
                .select("id")
                .from("TestEntity")
                .where(
                        WhereAttacher.builder()
                                .add(
                                        new Where()
                                                .column("name")
                                                .op(ComparisonOp.EQUALS)
                                                .value("John")
                                        , null
                                )
                )
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE (name = John)", query);
        System.out.println(query);
    }

    @Test
    @Order(3)
    @DisplayName("test-3: Select query w/ multi select statements & one where clause")
    public void testMultiSelectWhere() {
        String query = new QueryBuilder()
                .select("id", "age")
                .from("TestEntity")
                .where(
                        WhereAttacher.builder()
                                .add(
                                        new Where()
                                                .column("name")
                                                .op(ComparisonOp.EQUALS)
                                                .value("John")
                                        , null
                                )
                )
                .toString();
        Assertions.assertEquals("SELECT id, age FROM TestEntity WHERE (name = John)", query);
        System.out.println(query);
    }

    @Test
    @Order(4)
    @DisplayName("test-4: Select query w/ two where clauses")
    public void testSelectWhereMoreFilter() {
        String query = new QueryBuilder()
                .select("id")
                .from("TestEntity")
                .where(
                        WhereAttacher.builder()
                                .add(
                                        new Where()
                                                .column("name")
                                                .op(ComparisonOp.EQUALS)
                                                .value("John")
                                        , ComparisonOp.AND
                                )
                                .add(
                                        new Where()
                                                .column("age")
                                                .op(ComparisonOp.GREATER_THAN)
                                                .value("18")
                                        , null
                                )
                )
                .toString();
        Assertions.assertEquals("SELECT id FROM TestEntity WHERE (name = John AND age > 18)", query);
        System.out.println(query);
    }

}
