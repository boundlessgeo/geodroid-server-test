package tests;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import support.BaseTest;
import support.Fixture;
import support.Runner;

@RunWith(Runner.Block.class)
public class TestAPIPerformance extends BaseTest {

    protected final int lowRunCount = 3;
    protected final int runCount = 10;

    @ClassRule
    public static final BaseTest.WithScenario scenario = new BaseTest.WithScenario();

    @ClassRule
    public static final BaseTest.CollectDeviceLogs logs = new BaseTest.CollectDeviceLogs(scenario);

    @Rule
    public BaseTest.CollectGCLogs testLogs = new BaseTest.CollectGCLogs(scenario);

    static {
        scenario.reporter().toggleCSVReportingMode();
    }

    @Test
    public void testPerformanceGpkgPoints() throws Exception {
        for (int i = 0; i < lowRunCount; i++) {
            tests.getAllFeatures(Fixture.VA_PLACES, true);
        }
    }

    @Test
    public void testPerformanceGpkgMultiPoly() throws Exception {
        for (int i = 0; i < lowRunCount; i++) {
            tests.getAllFeatures(Fixture.VA_PARKS, true);
        }
    }

    @Test
    public void testPerformanceGpkgLine() throws Exception {
        for (int i = 0; i < lowRunCount; i++) {
            tests.getAllFeatures(Fixture.VA_ROADS, true);
        }
    }

    @Test
    public void testPerformanceTiles() throws Exception {
        for (int i = 0; i < runCount; i++) {
            int z = 1;
            int x = 1;
            int y = 1;
            tests.getTiles(Fixture.NE_TILES, z, x, y);
        }
    }
}