package tests;

import support.Preflight;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

@RunWith(AllTests.class)
public class PerfTestSuite {

    public static TestSuite suite() throws Exception {
        Preflight.preflight();
        TestSuite suite = new TestSuite(PerfTestSuite.class.getName());

        for (int i = 0; i < 3; i++) {
            suite.addTest(new JUnit4TestAdapter(TestDataEndPoint.class));
        }

        return suite;
    }
}
