package tests;

import com.jayway.restassured.RestAssured;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import support.ADB;
import support.BaseTest;
import support.Fixture;
import support.Reporter;
import support.Runner;

@RunWith(Runner.BlockRunner.class)
public class ProfileTests extends BaseTest {

    static String traceFile;

    /**
     * Copy trace files and rename to the test that just ran.
     */
    @Rule
    public TestWatcher watchman = new TestWatcher() {

        @Override
        protected void succeeded(Description description) {
            File trace = reporter.getFile(description.getMethodName() + ".trace");
            try {
                ADB.pull(getDeviceTraceFile(), trace.getAbsolutePath());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

    };

    /**
     * Try to find the trace file location on the device by inspecting
     * logs from dalvikvm
     * @return
     * @throws Exception
     */
    private static String getDeviceTraceFile() throws Exception {
        if (traceFile != null) {
            return traceFile;
        }
        // it's very fast to do this on the device
        String output = ADB.getOutput("shell", "logcat -d -s dalvikvm:I | grep trace | tail -n 1");
        // TRACE STARTED: '/mnt/sdcard/GeodroidServer.trace' 65536KB
        Matcher matcher = Pattern.compile("TRACE STARTED: '(.*)'").matcher(output);
        if (!matcher.find()) {
            throw new RuntimeException("Unable to locate trace file : " + output);
        }
        return traceFile = matcher.group(1);
    }

    @BeforeClass
    public static void initTracing() throws Exception {
        // the reporter is only really used for getting file locations for now
        reporter = Reporter.getProfileReporter();
        // enable tracing and restart
        ADB.execute("shell", "setprop", "log.tag.GeodroidServerTracing", "DEBUG");
        ADB.startService(true);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        // disable tracing and restart (or service will die under most normal use)
        ADB.execute("shell", "setprop", "log.tag.GeodroidServerTracing", "INFO");
        ADB.startService(true);
    }

    @Test
    public void testPerformanceGpkgPoints() throws Exception {
        tests.getFeatures(Fixture.VA_PLACES, true);
    }

    @Test
    public void testPerformanceGpkgMultiPoly() throws Exception {
        tests.getFeatures(Fixture.VA_PARKS, true);
    }

    @Test
    public void testPerformanceGpkgLine() throws Exception {
        tests.getFeatures(Fixture.VA_ROADS, true);
    }

    @Test
    public void testPerformanceTiles() throws Exception {
        int z = 1;
        int x = 1;
        int y = 1;
        tests.getTiles(Fixture.NE_TILES, z, x, y);
    }
}
