package misc.querybuilder;

import hive.sql.QueryBuilder;
import org.junit.jupiter.api.*;

import hive.sql.cmdbuilder.SQLCommandType;
/**
 * This class contains tests for the QueryBuilder class.
 * Created by SixEyes on 06/07/2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AlterTests {

    @Test
    @Order(1)
    @DisplayName("test-1: Alter table query")
    public void testAlterTable() {
        String query = QueryBuilder.builder(SQLCommandType.ALTER)
                .columns("TestEntity")
                .toString();
        Assertions.assertEquals("ALTER TABLE TestEntity;", query);
        System.out.println(query);
    }

    @Test
    @Order(2)
    @DisplayName("test-2: Alter table add column query")
    public void testAlterTableAddColumn() {
        String query = QueryBuilder.builder(SQLCommandType.ALTER)
                .columns("TestEntity")
                .columns("name", "VARCHAR(255)")
                .toString();
        Assertions.assertEquals("ALTER TABLE TestEntity ADD COLUMN name VARCHAR(255);", query);
        System.out.println(query);
    }

}
