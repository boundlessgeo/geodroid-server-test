package tests;

import static com.jayway.restassured.RestAssured.given;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import org.junit.BeforeClass;
import org.junit.Test;
import static tests.Extra.dequote;
import static tests.Extra.givenWithRequestBodyReport;

public class TestSchemaCreate {

    @BeforeClass
    public static void init() {
        Config.init();
    }

    @Test
    public void testSchemaCreatePut() {
        List params = new ArrayList(Arrays.asList(
            "va",
            "widgets"
        ));
        String route = "/features/{workspace}/{dataset}";
        String json = "{ 'type': 'schema', 'properties': { 'geometry': { 'type': 'Point', 'crs': 'epsg:4326' }, " +
            "'name': { 'type': 'string' }} }";

        givenWithRequestBodyReport().content(dequote(json))
            .expect().statusCode(201)
                .put(route, params.toArray());

        route = "/data/va/widgets";
        given().expect()
            .body("name", equalTo("widgets"))
            .body("schema.fid", equalTo("String"))
            .body("schema.geometry", equalTo("Point"))
                .get(route);
    }

}
