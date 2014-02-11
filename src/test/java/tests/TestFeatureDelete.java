package tests;

import static com.jayway.restassured.RestAssured.given;
import org.junit.Test;
import support.BaseTest;
import support.Tests;

public class TestFeatureDelete extends BaseTest {

    @Test
    public void testDelete() {
        Object[] params = new String[]{
            "scratch",
            "widgets",
            "1"
        };
        String route = "/features/{workspace}/{dataset}/{id}";

        tests.givenWithRequestReport()
            .expect().statusCode(200)
                .delete(route, params);

        given()
            .expect().statusCode(404)
                .get(route, params);
    }
}
