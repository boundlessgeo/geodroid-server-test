package tests;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import static org.hamcrest.Matchers.*;
import fixture.DataSet;
import fixture.Fixture;
import java.util.ArrayList;
import java.util.List;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static tests.Extra.givenWithReport;

/**
 *
 */
@RunWith(value = Parameterized.class)
public class TestDataEndPoint {
    private final DataSet parent;
    private final DataSet child;

    @BeforeClass
    public static void init() {
        Config.init();
    }

    public TestDataEndPoint(DataSet parent, String child) {
        this.parent = parent;
        this.child = child.length() == 0 ? null :
                     parent.getChild(child.substring(1));
    }

    @Test
    public void run() {
        RequestSpecification request = givenWithReport().pathParam("dataset", parent.name);
        if (child == null) {
            ResponseSpecification resp;
            if (parent.type == DataSet.TopLevelType.WORKSPACE) {
                resp = request.expect()
                        .body("datasets", containsInAnyOrder(parent.childrenNames()));
            } else {
                resp = request.expect()
                        .body("name", is(parent.name));
                if (parent.getExpectedFeatureCount() != null) {
                    resp.body("count", is(parent.getExpectedFeatureCount()));
                }
            }
            resp.get("/data/{dataset}");
        } else {
            ResponseSpecification resp = request.pathParam("child", child.name).expect()
                    .body("name", is(child.name));
            if (child.getExpectedFeatureCount() != null) {
                    resp.body("count", is(child.getExpectedFeatureCount()));
            }
            resp.get("/data/{dataset}/{child}");
        }
    }

    @Parameterized.Parameters(name = "TestDataEndpoint-{0}{1}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: Fixture.allDataSets()) {
            data.add(new Object[] {ds, ""});
            for (DataSet cs: ds.children) {
                data.add(new Object[] {ds, "-" + cs.name});
            }
        }
        return data;
    }

}
