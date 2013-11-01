package tests;

import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({TestRead.class})
public class TestSuite {

    @BeforeClass
    public static void setUp() throws Exception {
        Config.init();
        Fixture fixture = new Fixture();
        Logger logger = Logger.getGlobal();
        logger.info("downloading fixture data");
        fixture.getData();
        logger.info("installing fixture data");
        fixture.installData();
    }

}
