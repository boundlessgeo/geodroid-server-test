package tests;

import static com.jayway.restassured.RestAssured.given;
import org.junit.BeforeClass;
import org.junit.Test;
import static tests.Extra.*;

public class TestFeatureDelete {

    @BeforeClass
    public static void init() {
        Config.init();
    }

    @Test
    public void testDelete() {
        Object[] params = new String[]{
            "va",
            "populated_places",
            "4"
        };
        String route = "/features/{workspace}/{dataset}/{id}";

        givenWithRequestReport()
            .expect().statusCode(200)
                .delete(route, params);

        given()
            .expect().statusCode(404)
                .get(route, params);
    }
}
