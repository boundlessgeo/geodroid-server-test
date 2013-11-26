package tests;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import fixture.DataSet;
import fixture.Fixture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.hamcrest.Matchers.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static tests.Extra.*;

@RunWith(value = Parameterized.class)
public class TestFeatureGetByID {

    private final DataSet dataSet;

    @BeforeClass
    public static void init() {
        Config.init();
    }

    public TestFeatureGetByID(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Test
    public void run() throws IOException {
        // select a random id - JSON seems to start w/ 0 and gpkg 1 so make sure
        // this doesn't break things
        int id = dataSet.getExpectedFeatureCount() == 1 ? 1 :
            new Random().nextInt(dataSet.getExpectedFeatureCount() - 1) + 1;
        RequestSpecification request = givenWithRequestReport().pathParams(
                "workspace", dataSet.getParent().name,
                "dataset", dataSet.name,
                "id", Integer.toString(id));
        String route = "/features/{workspace}/{dataset}/{id}";
        request.expect()
            .body("features", hasSize(1))
            .body("features.id", hasItems(Integer.toString(id)))
            .statusCode(200)
                .get(route);
    }

    @Parameterized.Parameters(name = "TestFeatureGetByID-{0}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: Fixture.vectorDataSets()) {
            if (ds.getExpectedFeatureCount() > 0) {
                data.add(new Object[] {ds});
            }
        }
        return data;
    }
}
