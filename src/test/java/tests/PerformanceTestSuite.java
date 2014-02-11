package tests;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import support.BaseTest;
import support.Fixture;

public class PerformanceTestSuite extends BaseTest {

    @ClassRule
    public static final WithScenario scenario = new WithScenario();

    @ClassRule
    public static final CollectDeviceLogs logs = new CollectDeviceLogs(scenario);

    @Rule
    public CollectGCLogs testLogs = new CollectGCLogs(scenario);

    static {
        scenario.reporter().toggleCSVReportingMode();
    }

    @Test
    public void testPerformance() throws Exception {
        for (int i = 0; i < 10; i++) {
            tests.getFeatureById(Fixture.VA_PLACES, 1);
        }
    }
}
