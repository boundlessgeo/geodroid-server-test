package support;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class Runner {

    public static class BlockRunner extends BlockJUnit4ClassRunner {

        public BlockRunner(Class<?> klass) throws InitializationError {
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

}
