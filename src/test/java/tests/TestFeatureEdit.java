package tests;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import support.BaseTest;
import support.Tests;

public class TestFeatureEdit extends BaseTest {

    @Test
    public void testEditJSON() {
        Object[] params = new String[]{
            "scratch",
            "widgets",
            "1"
        };
        String route = "/features/{workspace}/{dataset}/{id}";
        String json = "{'type':'Feature','geometry':{'type':'Point','coordinates':[1.2,3.4]},"
                + "'properties':{'name':'Elsewhereville'}}";
        
        tests.givenWithRequestBodyReport().content(dequote(json))
            .expect().statusCode(200)
                .put(route, params);
        
        given().expect()
            .body("features[0].geometry.coordinates", hasItems(1.2f, 3.4f))
            .body("features[0].properties.name", equalTo("Elsewhereville"))
                .get(route, params);
    }

}
