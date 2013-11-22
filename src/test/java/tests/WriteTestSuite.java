package tests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestFeatureEdit.class, TestFeatureCreate.class, TestFeatureDelete.class})
public class WriteTestSuite {

    @BeforeClass
    public static void setUp() throws Exception {
        Preflight.preflight();
    }
}
