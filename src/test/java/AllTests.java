import hive.ServerTests;
import hive.database.DatabaseTests;
import misc.ReflectionTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({DatabaseTests.class, ServerTests.class, ReflectionTests.class})
public class AllTests {
}