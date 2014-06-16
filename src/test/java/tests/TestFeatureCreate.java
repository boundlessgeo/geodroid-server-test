package tests;

import static com.jayway.restassured.RestAssured.given;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import org.junit.Test;
import support.BaseTest;

public class TestFeatureCreate extends BaseTest {

    @Test
    public void testEditJSON() {
        List params = new ArrayList(Arrays.asList(
            "scratch",
            "widgets"
        ));
        String route = "/features/{workspace}/{dataset}";
        String json = "{'type':'Feature','geometry':{'type':'Point','coordinates':[6.7,8.9]},"
                + "'properties':{'name':'Nowheresville'}}";

        tests.givenWithRequestBodyReport().content(dequote(json))
            .expect().statusCode(201)
                .post(route, params.toArray());

        // until a returned id is available, we 'know' that it will be 16 :(
        route = route + "/{id}";
        params.add(1);
        given().expect()
            .body("features[0].geometry.coordinates", hasItems(6.7f, 8.9f))
            .body("features[0].properties.name", equalTo("Nowheresville"))
                .get(route, params.toArray());
    }

}
