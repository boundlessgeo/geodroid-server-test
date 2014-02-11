package tests;

import support.DataSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import support.BaseTest;
import support.Tests;

@RunWith(value = Parameterized.class)
public class TestFeatureGetByID extends BaseTest {

    private final DataSet dataSet;

    public TestFeatureGetByID(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Test
    public void run() throws IOException {
        // select a random id - JSON seems to start w/ 0 and gpkg 1 so make sure
        // this doesn't break things
        int id = dataSet.getExpectedFeatureCount() == 1 ? 1 :
            new Random().nextInt(dataSet.getExpectedFeatureCount() - 1) + 1;
        tests.getFeatureById(dataSet, id);
    }

    @Parameterized.Parameters(name = "TestFeatureGetByID-{0}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: activeFixture().vectorDataSets()) {
            if (ds.getExpectedFeatureCount() > 0) {
                data.add(new Object[] {ds});
            }
        }
        return data;
    }
}
