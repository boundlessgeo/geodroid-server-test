package support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 *
 */
public class BaseTest {

    protected static Tests tests = new Tests();

    // ideally this wouldn't be static but it is for now to support
    // injecting it from suites or specific tests.
    protected static Reporter reporter;

    public static void setReporter(Reporter reporter) {
        BaseTest.reporter = reporter;
        tests.setReporter(reporter);
    }

    @BeforeClass
    public static void init() {
        Config.init();
    }

    protected static Fixture activeFixture() {
        return Config.getActiveFixture();
    }

    public static String dequote(String json) {
        return json.replaceAll("'", "\"");
    }

    /**
     * A rule to enable a Reporter across all tests.
     */
    public static class WithReporter implements TestRule {
        protected final Reporter reporter;

        public WithReporter() {
            this(new File("test-reports"));
        }

        public WithReporter(File baseDirectory) {
            this.reporter = new Reporter(baseDirectory);
            BaseTest.setReporter(reporter);
            Listener.register(reporter);
        }

        public Reporter reporter() {
            return reporter;
        }

        @Override
        public Statement apply(final Statement base, Description description) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    try {
                        base.evaluate();
                    } finally {
                        Listener.unregister(reporter);
                        reporter.finish();
                    }
                }
            };
        }

    }

    /**
     * A rule to support gathering output under a scenario directory.
     */
    public static class WithScenario extends WithReporter {

        public WithScenario() {
            super(getScenario());
            reporter.writeScenario();
        }

        private static File getScenario() {
            return new File("test-reports/scenarios", Scenario.scenario().getScenario());
        }

    }

    static abstract class NeedsReporter implements TestRule {
        final Reporter reporter;

        NeedsReporter(WithReporter with) {
            reporter = with.reporter;
        }
    }

    /**
     * Rule to support collecting all relevant device logging
     */
    public static class CollectDeviceLogs extends NeedsReporter {

        public CollectDeviceLogs(WithReporter with) {
            super(with);
        }

        @Override
        public Statement apply(final Statement base, Description description) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    Logcat log = Logcat.beginLogging(reporter.getFile("device.log"));
                    try {
                        base.evaluate();
                    } finally {
                        log.stopLogging();
                    }
                }
            };
        }
    }

    /**
     * Rule to support collecting GC related device logging. Delimits output
     * with for each test run.
     */
    public static class CollectGCLogs extends NeedsReporter {

        private final PrintWriter writer;

        public CollectGCLogs(WithReporter with) {
            super(with);
            try {
                writer = new PrintWriter(reporter.getFile("gclog.log"));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public Statement apply(final Statement base, final Description description) {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    Logcat stats = Logcat.collectGCStatistics();
                    try {
                        base.evaluate();
                    } finally {
                        reportStats(description, stats);
                    }
                }
            };
        }

        private void reportStats(Description desc, Logcat stats) {
            List<String> readLines = stats.readLines();
            writer.println(desc.getMethodName());
            for (String l: readLines) {
                writer.println(l);
            }
            writer.println();
            writer.flush();
        }
    }


}
