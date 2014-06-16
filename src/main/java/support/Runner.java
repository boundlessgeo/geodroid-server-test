package support;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class Runner {

    public static class Block extends BlockJUnit4ClassRunner {

        public Block(Class<?> klass) throws InitializationError {
            super(klass);
        }

        @Override
        protected void runChild(FrameworkMethod method, RunNotifier notifier) {
            Listener listener = Listener.instance();
            notifier.addListener(listener);
            super.runChild(method, notifier);
            notifier.removeListener(listener);
        }

    }

    public static class Suite extends org.junit.runners.Suite {

        public Suite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
            super(klass, builder);
        }

        @Override
        protected void runChild(org.junit.runner.Runner runner, RunNotifier notifier) {
            Listener listener = Listener.instance();
            notifier.addListener(listener);
            super.runChild(runner, notifier);
            notifier.removeListener(listener);
        }

    }

}
