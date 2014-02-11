package support;

import java.util.HashSet;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Due to the way Junit/Maven work, this is an ugly singleton class. Surefire
 * will instantiate the class before any test is run. As tests are not run in
 * parallel, there should be no concurrency issues.
 */
public class Listener extends RunListener {

    private static Listener instance;
    private final HashSet<RunListener> listeners = new HashSet<RunListener>();

    public Listener() {
        if (instance != null) {
            throw new RuntimeException();
        }
        instance = this;
    }

    public static void register(RunListener listener) {
        instance.listeners.add(listener);
    }

    public static void unregister(RunListener listener) {
        instance.listeners.remove(listener);
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        for (RunListener l: listeners) {
            l.testFailure(failure);
        }
    }

    @Override
    public void testFinished(Description description) throws Exception {
        for (RunListener l: listeners) {
            l.testFinished(description);
        }
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        for (RunListener l: listeners) {
            l.testRunFinished(result);
        }
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        for (RunListener l: listeners) {
            l.testRunStarted(description);
        }
    }

    @Override
    public void testStarted(Description description) throws Exception {
        for (RunListener l: listeners) {
            l.testStarted(description);
        }
    }
}
