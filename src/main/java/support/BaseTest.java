package support;

import org.junit.BeforeClass;

/**
 *
 */
public class BaseTest {

    @BeforeClass
    public static void init() {
        Config.init();
    }

}
