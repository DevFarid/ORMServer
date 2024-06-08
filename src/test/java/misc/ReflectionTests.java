package misc;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.junit.jupiter.api.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * tests the `Reflection Util` class.
 * Created by SixEyes 06/02/2024.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
 public class ReflectionTests {
    private static final Logger logger = Logger.getLogger(ReflectionTests.class.getName());

    // test getting class name from file.
    @Test
    @Order(1)
    @DisplayName("test getting class name from file.")
    public void testClassNameFromFile() {
        String fileName = "ReflectionTestClass.java";
        File f = new File(String.format("reflect/%s", fileName));
        Assertions.assertNotNull(f);

        String className = fileName.substring(0, fileName.length() - 5);
        String expected = ReflectionUtil.getClassName(f);

        Assertions.assertEquals(className, expected);
    }

    // test finding java files in directory.
    @Test
    @Order(2)
    @DisplayName("test finding java files in directory.")
    public void testFindJavaFiles() {
        File directory = new File("src/test/java/misc/reflect");
        Assertions.assertNotNull(directory);

        File[] files = directory.listFiles();
        Assertions.assertNotNull(files);

        int expected = 1;
        int actual = ReflectionUtil.findJavaFiles(directory).size();

        Assertions.assertEquals(expected, actual);
    }

    // test compiling java files.
    @Test
    @Order(3)
    @DisplayName("test compiling java files.")
    public void testCompileJavaFiles() {
        String fileName = "ReflectionTestClass";
        File file = new File(String.format("src/test/java/misc/reflect/%s.java", fileName));
        Assertions.assertNotNull(file);

        try {
            ReflectionUtil.compileJavaFiles(List.of(file));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error compiling java files.", e);
        }

        Assertions.assertTrue(ReflectionUtil.wasCompiled(List.of(file)));
    }

    // test loading a class
    @Test
    @Order(4)
    @DisplayName("test loading a class.")
    public void testLoadClass() {
        String className = "ReflectionTestClass";
        File file = new File(String.format("src/test/java/misc/reflect/%s.java", className));
        Assertions.assertNotNull(file);

        try {
            ReflectionUtil.compileJavaFiles(List.of(file));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error compiling java files.", e);
        }
        Assertions.assertTrue(ReflectionUtil.wasCompiled(List.of(file)));

        try {
            List<Class<?>> clazz = ReflectionUtil.loadClass(List.of(file));
            Assertions.assertNotNull(clazz);
            Assertions.assertEquals(1, clazz.size());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading class.", e);
        }
    }

    // test getting member fields
    @Test
    @Order(5)
    @DisplayName("test getting member fields.")
    public void testGetMemberFields() {
        String className = "ReflectionTestClass";
        File file = new File(String.format("src/test/java/misc/reflect/%s.java", className));
        Assertions.assertNotNull(file);

        try {
            ReflectionUtil.compileJavaFiles(List.of(file));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error compiling java files.", e);
        }
        Assertions.assertTrue(ReflectionUtil.wasCompiled(List.of(file)));

        try {
            List<Class<?>> clazz = ReflectionUtil.loadClass(List.of(file));
            Assertions.assertNotNull(clazz);
            Assertions.assertEquals(1, clazz.size());

            Class<?> clazz0 = clazz.getFirst();
            Field[] fields = ReflectionUtil.getMemberFieldsFromClazz(clazz0);


            Assertions.assertNotNull(fields);
            Assertions.assertEquals(3, fields.length);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading class.", e);
        }
    }

    // test class and its fields are annotated
    @Test
    @Order(6)
    @DisplayName("test annotated types & primary key.")
    public void testClassAndFieldsAnnotated() {
        String className = "ReflectionTestClass";
        File file = new File(String.format("src/test/java/misc/reflect/%s.java", className));
        Assertions.assertNotNull(file);

        try {
            ReflectionUtil.compileJavaFiles(List.of(file));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error compiling java files.", e);
        }
        Assertions.assertTrue(ReflectionUtil.wasCompiled(List.of(file)));

        try {
            List<Class<?>> loadedClasses = ReflectionUtil.loadClass(List.of(file));
            Assertions.assertNotNull(loadedClasses);
            Assertions.assertEquals(1, loadedClasses.size());

            Class<?> loadedClass = loadedClasses.getFirst();
            Field f = ReflectionUtil.fieldHasAnnotation(ReflectionUtil.getMemberFieldsFromClazz(loadedClass), DatabaseField.class);

            Assertions.assertTrue(ReflectionUtil.classHasAnnotation(loadedClass, DatabaseTable.class));
            Assertions.assertNotNull(f);
            Assertions.assertTrue(ReflectionUtil.isFieldPrimaryKey(f));

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
