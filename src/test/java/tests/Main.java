package tests;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

/**
 *
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        String test = "tests.FullTestSuite";
        if (args.length == 1) {
            if ("test".equals(args[0])) {
                // already done
            } else if ("prof".equals(args[0])) {
                test = "tests.ProfileTests";
            } else if ("perf".equals(args[0])) {
                test = "tests.PerformanceTestSuite";
            } else {
                System.out.println("unknown test, options are: test, prof, perf");
                System.exit(1);
            }
        }

        JUnitCore c = new JUnitCore();
        c.addListener(new TextListener(System.out));
        c.run(Class.forName(test));
    }
}
