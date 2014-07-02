package support;

import java.util.HashSet;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Due to the way Junit/Maven works, this has been implemented as an ugly
 * singleton class. As tests are not run in
 * parallel, there should be no concurrency issues...
 */
public class Listener extends RunListener {

    private static Listener instance;
    private final HashSet<RunListener> listeners = new HashSet<RunListener>();

    private Listener() {}

    public static Listener instance() {
        if (instance == null) {
            instance = new Listener();
        }
        return instance;
    }

    public static void register(RunListener listener) {
        instance().listeners.add(listener);
    }

    public static void unregister(RunListener listener) {
        instance().listeners.remove(listener);
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
