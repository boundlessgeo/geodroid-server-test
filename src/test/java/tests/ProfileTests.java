package tests;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import support.ADB;
import support.BaseTest;
import support.Fixture;
import support.Reporter;

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
                System.out.println(trace.getPath());
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
        // wait for service?
        Thread.sleep(500);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        // disable tracing and restart (or service will die under most normal use)
        ADB.execute("shell", "setprop", "log.tag.GeodroidServerTracing", "INFO");
        ADB.startService(true);
    }

    @Test
    public void testGetFeatureByIdGeoPkg() throws Exception {
        tests.getFeatureById(Fixture.VA_PLACES, 1);
    }
}
