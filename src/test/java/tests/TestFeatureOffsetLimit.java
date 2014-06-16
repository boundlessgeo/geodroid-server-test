package tests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import support.BaseTest;

@RunWith(value = Parameterized.class)
public class TestFeatureOffsetLimit extends BaseTest {

    private final int offset;
    private final int limit;

    public TestFeatureOffsetLimit(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    @Test
    public void run() throws IOException {
        String route = "/features/va/populated_places";
        String[] ids = new String[limit];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Integer.toString(offset + i + 1);
        }
        tests.givenWithRequestReport().queryParam("offset", offset).queryParam("limit", limit)
            .expect()
                .body("features", hasSize(limit))
                .body("features.id", hasItems(ids))
            .get(route);
    }

    @Parameterized.Parameters(name = "TestFeatureOffsetLimit-{0}-{1}")
    public static List<Object[]> data() {
        // va/populated_places has 15 features
        int features = 15;
        List<Object[]> data = new ArrayList<Object[]>();
        for (int i = 0; i < features; i++) {
            data.add(new Object[] {i, features - i});
        }
        return data;
    }
}