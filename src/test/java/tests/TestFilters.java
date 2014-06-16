package tests;

import com.jayway.restassured.path.json.JsonPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import support.BaseTest;
import support.DataSet;
import support.Fixture;

/**
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
@RunWith(value = Parameterized.class)
public class TestFilters extends BaseTest {
    private final Scenario scenario;

    public TestFilters(Scenario scenario) {
        this.scenario = scenario;
    }

    @Test
    public void run() throws IOException {
        JsonPath body = tests.getFeatures(scenario.ds, false, "filter", scenario.filter).body().jsonPath();
        int x = 0;
        int expected = scenario.expectedSize();
        assertEquals(expected, body.getList("features").size());

        // expectations are ordered in response
        for (int i = 0; i < scenario.featureExpectations.size(); i++) {
            Object[] kv = scenario.featureExpectations.get(i);
            for (int j = 0; j < kv.length; j+=2) {
                String path = "features[" + i + "]";
                if ("id".equals(kv[j])) {
                    path += ".id";
                } else {
                    path += ".properties." + kv[j];
                }
                Object value = body.get(path);
                Assert.assertEquals("for path " + path, kv[j+1], value);
                x++;
            }
        }

        // contains are unordered
        Object[] kv = scenario.contains;
        for (int j = 0; j < kv.length; j += 2) {
            // jsonpath to search for the feature with property value
            String path = "features.findAll{ f -> f.properties." + kv[j] + "==\"" + kv[j + 1] + "\"}";
            List<Object> list = body.getList(path);
            assertEquals(1, list.size());
            x++;
        }
        // make sure something happened
        assertTrue(x >= expected);
    }

    @Parameterized.Parameters(name = "TestFilters-{0}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        data.add(new Object[] {scenario(Fixture.VA_PLACES, "in (5)")
                .expect("NAME", "Lancaster", "id", "5")});
        data.add(new Object[] {scenario(Fixture.VA_PLACES, "NAME LIKE 'Lancast%'")
                .expect("NAME", "Lancaster", "id", "5")});
        data.add(new Object[] {scenario(Fixture.VA_PLACES, "MAX_POP10 eq 209489")
                .expect("NAME", "Lancaster", "id", "5")});
        data.add(new Object[] {scenario(Fixture.VA_PLACES, "MAX_POP10 IN (209489,79662)")
                .contains("NAME", "Lancaster", "NAME", "Hagerstown")});
        data.add(new Object[] {scenario(Fixture.VA_PLACES, "NOT_EXISTING = 0")
                .expect(0)});
        data.add(new Object[] {scenario(Fixture.STATES_STATES, "STATE_NAME LIKE 'Calif%'")
                .expect("STATE_NAME", "California")});
        data.add(new Object[] {scenario(Fixture.STATES_STATES, "NOT_EXISTING = 0")
                .expect(0)});
        return data;
    }

    static Scenario scenario(DataSet ds, String filter) {
        return new Scenario(ds, filter);
    }

    static class Scenario {

        int expect = -1;
        List<Object[]> featureExpectations = new ArrayList<Object[]>();
        Object[] contains = new Object[0];
        final String filter;
        final DataSet ds;

        Scenario expect(int count) {
            this.expect = count;
            return this;
        }

        Scenario expect(Object... kv) {
            featureExpectations.add(kv);
            return this;
        }

        Scenario contains(Object... kv) {
            contains = kv;
            return this;
        }

        private Scenario(DataSet ds, String filter) {
            this.ds = ds;
            this.filter = filter;
        }

        int expectedSize() {
            return expect >= 0 ? expect : featureExpectations.size() > 0 ? featureExpectations.size() : contains.length / 2;
        }

        public String toString() {
            return ds.name + " " + filter;
        }
    }
}
