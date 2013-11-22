package tests;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({TestRead.class, TestData.class, TestDataEndPoint.class, 
    TestFeatureOutputPNG.class, TestFeatureOutputJSON.class, 
    TestFeatureGetByID.class, TestFeatureOffsetLimit.class})
public class ReadTestSuite {

    @BeforeClass
    public static void setUp() throws Exception {
        Preflight.preflight();
    }

}
