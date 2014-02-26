package tests;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import support.BaseTest;
import support.Fixture;
import support.Runner;

@RunWith(Runner.BlockRunner.class)
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
    public void testPerformanceGpkgPoints() throws Exception {
        for (int i = 0; i < 10; i++) {
            tests.getFeatures(Fixture.VA_PLACES, true);
        }
    }

    @Test
    public void testPerformanceGpkgMultiPoly() throws Exception {
        for (int i = 0; i < 10; i++) {
            tests.getFeatures(Fixture.VA_PARKS, true);
        }
    }

    @Test
    public void testPerformanceGpkgLine() throws Exception {
        for (int i = 0; i < 10; i++) {
            tests.getFeatures(Fixture.VA_ROADS, true);
        }
    }

    @Test
    public void testPerformanceTiles() throws Exception {
        for (int i = 0; i < 10; i++) {
            int z = 1;
            int x = 1;
            int y = 1;
            tests.getTiles(Fixture.NE_TILES, z, x, y);
        }
    }
}
