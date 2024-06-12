package misc.querybuilder;

import com.j256.ormlite.field.SqlType;
import hive.sql.cmdbuilder.AlterBuilder;
import org.junit.jupiter.api.*;

/**
 * This class contains tests for the QueryBuilder class.
 * Created by SixEyes on 06/07/2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AlterTests {

    @Test
    @Order(1)
    @DisplayName("test-1: Alter table add column query")
    public void testAlterTableAddColumn() {
        String query = AlterBuilder.builder()
                .table("TestEntity")
                .add("birthdate", SqlType.DATE)
                .toString();
        Assertions.assertEquals("ALTER TABLE TestEntity ADD COLUMN birthdate DATE;", query);
        System.out.println(query);
    }

    @Test
    @Order(2)
    @DisplayName("test-2: Alter table add column query; different data type")
    public void testAlterTableAddColumnDiffType() {
        String query = AlterBuilder.builder()
                .table("TestEntity")
                .add("image", SqlType.BLOB)
                .toString();
        Assertions.assertEquals("ALTER TABLE TestEntity ADD COLUMN image BLOB;", query);
        System.out.println(query);
    }

    @Test
    @Order(3)
    @DisplayName("test-3: Alter table rename a column")
    public void testAlterTableRenameColumn() {
        String query = AlterBuilder.builder()
                .table("TestEntity")
                .column("old_column_name")
                .to("new_column_name")
                .toString();
        Assertions.assertEquals("ALTER TABLE TestEntity RENAME COLUMN old_column_name TO new_column_name;", query);
        System.out.println(query);
    }

    @Test
    @Order(4)
    @DisplayName("test-4: Alter table rename a table")
    public void testAlterTableRenameTable() {
        String query = AlterBuilder.builder()
                .table("TestEntity")
                .renameTo("RenameTestEntity")
                .toString();
        Assertions.assertEquals("ALTER TABLE TestEntity RENAME TO RenameTestEntity;", query);
        System.out.println(query);
    }

}
