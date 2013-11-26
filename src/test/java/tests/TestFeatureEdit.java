package tests;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.junit.BeforeClass;
import org.junit.Test;
import static tests.Extra.givenWithRequestBodyReport;

public class TestFeatureEdit {

    @BeforeClass
    public static void init() {
        Config.init();
    }

    @Test
    public void testEditJSON() {
        Object[] params = new String[]{
            "va",
            "populated_places",
            "4"
        };
        String route = "/features/{workspace}/{dataset}/{id}";
        String json = "{'type':'Feature','geometry':{'type':'Point','coordinates':[1.2,3.4]},"
                + "'properties':{'NAMEASCII':'Hagersville','MIN_BBXMIN':42.42,'SCALERANK':42}}";
        
        givenWithRequestBodyReport().content(dequote(json))
            .expect().statusCode(200)
                .put(route, params);
        
        given().expect()
            .body("features[0].geometry.coordinates", hasItems(1.2f, 3.4f))
            .body("features[0].properties.NAMEASCII", equalTo("Hagersville"))
            .body("features[0].properties.MIN_BBXMIN", equalTo(42.42f))
            .body("features[0].properties.SCALERANK", equalTo(42))
                .get(route, params);
    }

    String dequote(String json) {
        return json.replaceAll("'", "\"");
    }
}
