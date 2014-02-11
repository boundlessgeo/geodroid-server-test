package tests;

import support.DataSet;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import support.BaseTest;
import support.Tests;

/**
 *
 */
@RunWith(value = Parameterized.class)
public class TestDataEndPoint extends BaseTest {
    private final DataSet parent;
    private final DataSet child;

    public TestDataEndPoint(DataSet parent, String child) {
        this.parent = parent;
        this.child = child.length() == 0 ? null :
                     parent.getChild(child.substring(1));
    }

    @Test
    public void run() {
        tests.getDataEndPoint(parent, child);
    }

    @Parameterized.Parameters(name = "TestDataEndpoint-{0}{1}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: activeFixture().allDataSets()) {
            data.add(new Object[] {ds, ""});
            for (DataSet cs: ds.children) {
                data.add(new Object[] {ds, "-" + cs.name});
            }
        }
        return data;
    }

}
