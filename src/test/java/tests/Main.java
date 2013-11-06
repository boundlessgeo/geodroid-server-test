package tests;

import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;

/**
 *
 */
public class Main {

    public static void main(String[] args) throws Exception {
        JUnitCore c = new JUnitCore();
        c.addListener(new TextListener(System.out));
        c.addListener(new Reporter());
        c.run(TestSuite.class);
        InputStream stream = Main.class.getResourceAsStream("/report.css");
        IOUtils.copy(stream, new FileOutputStream("target/report.css"));
    }
}
