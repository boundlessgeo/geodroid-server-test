package tests;

import static com.jayway.restassured.RestAssured.given;
import org.junit.Test;
import support.BaseTest;
import static support.Extra.*;

public class TestFeatureDelete extends BaseTest {

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
