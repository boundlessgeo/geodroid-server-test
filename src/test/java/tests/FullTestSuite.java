package tests;

import support.Preflight;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import support.BaseTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ReadTestSuite.class, WriteTestSuite.class})
public class FullTestSuite extends BaseTest {

    @ClassRule
    public static final WithScenario scenario = new WithScenario();

    @BeforeClass
    public static void setUp() throws Exception {
        Preflight.preflight();
    }
}
