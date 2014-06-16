package tests;

import support.DataSet;
import java.io.IOException;
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
public class TestFeatureOutputJSON extends BaseTest {

    private final DataSet dataSet;

    public TestFeatureOutputJSON(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Test
    public void run() throws IOException {
        tests.getAllFeatures(dataSet, false);
    }

    @Parameterized.Parameters(name = "TestFeatureOutputJSON-{0}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: activeFixture().vectorDataSets()) {
            data.add(new Object[] {ds});
        }
        return data;
    }
}
