package tests;

import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import fixture.DataSet;
import fixture.Fixture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 */
@RunWith(value = Parameterized.class)
public class TestFeatureOutputJSON {

    private final DataSet dataSet;

    @BeforeClass
    public static void init() {
        Config.init();
    }

    public TestFeatureOutputJSON(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Test
    public void run() throws IOException {
        RequestSpecification request = given().pathParam("dataset", dataSet.name);
        String route = "/features/{dataset}";
        if (dataSet.getParent() != null) {
            route = "/features/{workspace}/{dataset}";
            request.pathParam("workspace", dataSet.getParent().name);
        }
        request.expect()
            .body("features", hasSize(dataSet.getExpectedFeatureCount()))
            .get(route);
    }

    @Parameterized.Parameters(name = "TestFeatureOutputPNG-{0}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: Fixture.vectorDataSets()) {
            data.add(new Object[] {ds});
        }
        return data;
    }
}
