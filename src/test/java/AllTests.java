import hive.ServerTests;
import hive.database.DatabaseTests;
import misc.ReflectionTests;

import misc.querybuilder.AlterTests;
import misc.querybuilder.SelectTests;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        DatabaseTests.class, ServerTests.class, ReflectionTests.class,
        SelectTests.class, AlterTests.class
})
public class AllTests {
}