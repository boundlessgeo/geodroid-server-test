package tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import support.BaseTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestSchemaCreate.class, TestFeatureCreate.class,
   TestFeatureEdit.class, TestFeatureDelete.class})
public class WriteTestSuite extends BaseTest {

}
