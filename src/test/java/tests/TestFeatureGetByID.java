package tests;

import static com.jayway.restassured.RestAssured.given;
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
        int id = new Random().nextInt(dataSet.getExpectedFeatureCount()) + 1;
        RequestSpecification request = given().pathParams(
                "workspace", dataSet.getParent().name,
                "dataset", dataSet.name,
                "id", Integer.toString(id));
        String route = "/features/{workspace}/{dataset}/{id}";
        request.expect()
            .body("features", hasSize(1))
            .body("features.id", hasItems(Integer.toString(id)))
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
