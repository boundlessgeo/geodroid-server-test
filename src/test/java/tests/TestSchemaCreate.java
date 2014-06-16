package tests;

import static com.jayway.restassured.RestAssured.given;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.equalTo;
import org.junit.Test;
import support.BaseTest;

public class TestSchemaCreate extends BaseTest {

    @Test
    public void testSchemaCreatePut() {
        List params = new ArrayList(Arrays.asList(
            "scratch",
            "widgets"
        ));
        String route = "/features/{workspace}/{dataset}";
        String json = "{ 'type': 'schema', 'properties': { 'geometry': { 'type': 'Point', 'crs': 'epsg:4326' }, " +
            "'name': { 'type': 'string' }} }";

        tests.givenWithRequestBodyReport().content(dequote(json))
            .expect().statusCode(201)
                .put(route, params.toArray());

        route = "/data/scratch/widgets";
        given().expect()
            .body("name", equalTo("widgets"))
            .body("schema.fid", equalTo("Integer"))
            .body("schema.geometry", equalTo("Point"))
                .get(route);
    }

}
