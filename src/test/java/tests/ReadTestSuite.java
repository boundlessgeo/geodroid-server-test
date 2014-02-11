package tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import support.BaseTest;

@RunWith(Suite.class)
@SuiteClasses({TestRead.class, TestData.class, TestDataEndPoint.class,
    TestFeatureOutputPNG.class, TestFeatureOutputJSON.class,
    TestFeatureGetByID.class, TestFeatureOffsetLimit.class})
public class ReadTestSuite extends BaseTest {

}
