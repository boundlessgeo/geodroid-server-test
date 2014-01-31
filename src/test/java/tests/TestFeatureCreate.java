package tests;

import static com.jayway.restassured.RestAssured.given;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import org.junit.Test;
import support.BaseTest;
import static support.Extra.*;

public class TestFeatureCreate extends BaseTest {

    @Test
    public void testEditJSON() {
        List params = new ArrayList(Arrays.asList(
            "va",
            "populated_places"
        ));
        String route = "/features/{workspace}/{dataset}";
        String json = "{'type':'Feature','geometry':{'type':'Point','coordinates':[6.7,8.9]},"
                + "'properties':{'NAMEASCII':'Nowheresville','MIN_BBXMIN':42.42,'SCALERANK':42}}";

        givenWithRequestBodyReport().content(dequote(json))
            .expect().statusCode(201)
                .post(route, params.toArray());

        // until a returned id is available, we 'know' that it will be 16 :(
        route = route + "/{id}";
        params.add(16);
        given().expect()
            .body("features[0].geometry.coordinates", hasItems(6.7f, 8.9f))
            .body("features[0].properties.NAMEASCII", equalTo("Nowheresville"))
            .body("features[0].properties.MIN_BBXMIN", equalTo(42.42f))
            .body("features[0].properties.SCALERANK", equalTo(42))
                .get(route, params.toArray());
    }

}
