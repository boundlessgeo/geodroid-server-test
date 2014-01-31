package tests;

import support.Preflight;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ReadTestSuite.class, WriteTestSuite.class})
public class FullTestSuite {

    @BeforeClass
    public static void setUp() throws Exception {
        Preflight.preflight();
    }
}
