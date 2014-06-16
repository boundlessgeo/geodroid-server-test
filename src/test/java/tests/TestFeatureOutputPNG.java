package tests;

import support.DataSet;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import support.BaseTest;

/**
 *
 */
@RunWith(value = Parameterized.class)
public class TestFeatureOutputPNG extends BaseTest {

    private final DataSet dataSet;
    private final String style;

    public TestFeatureOutputPNG(DataSet dataSet, String style) {
        this.dataSet = dataSet;
        this.style = style;
    }

    @Test
    public void run() throws Exception {
        byte[] data = tests.getFeatureAsImage(dataSet, style);
        if (reporter != null) {
            reporter.reportImage(String.format("TestFeatureOutputPNG-%s-%s.png", dataSet.name, style), data);
        }
    }

    @Parameterized.Parameters(name = "TestFeatureOutputPNG-{0}-{1}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: activeFixture().vectorDataSets()) {
            data.add(new Object[] {ds, ""});
            for (String style: ds.getAssociatedStyles()) {
                data.add(new Object[]{ds, style});
            }
        }
        return data;
    }
}
