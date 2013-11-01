package tests;

import org.junit.Test;
import static tests.Extra.*;
import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.http.ContentType;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

public class TestRead {

    @BeforeClass
    public static void init() {
        Config.init();
    }

    @Test
    public void testPing() {
        expect().body(containsString("Geodroid Server"))
                .contentType(ContentType.HTML)
                .get("/");
    }

    @Test
    public void testData() {
        expect().body("air_runways", hasEntry("type", "dataset"))
                .body("ne", hasEntry("type", "workspace"))
                .get("/data");
    }

    @Test
    public void testAirRunways() {
        expect().body("name", is("air_runways"))
                .body("type", is("vector"))
                .body("bbox", hasSize(4))
                .body("count", is(319))
                .body("schema", hasKey("OBJECTID"))
                .body("features", is("/features/air_runways.json"))
                .get("/data/air_runways");
    }

    @Test
    public void testNe() {
        expect().body("type", is("workspace"))
                .body("datasets", containsInAnyOrder("populated_places", "tiles"))
                .get("/data/ne1");
    }

    @Test
    public void testFeatureServiceImage() {
        InputStream stream = get("/features/ne1/populated_places.png").asInputStream();
        assertTrue(isPNG(stream));
    }

    @Test
    public void testFeatureService() throws IOException {
        List<List<Float>> coords = get("/features/ne1/populated_places.json").body().jsonPath().getList("features.geometry.coordinates");
        Rectangle bounds = new Rectangle();
        for (List<Float> list: coords) {
            bounds.add(list.get(0), list.get(1));
        }
        System.out.println(bounds);
            assertFalse(true);

        expect().body("features", hasSize(186)).get("/features/ne1/populated_places.json");
        given().queryParam("limit", 1).expect().body("features", hasSize(1)).get("/features/ne1/populated_places.json");
        given().queryParam("filter", "fid=56").expect().body("features", hasSize(1)).get("/features/ne1/populated_places.json");
        given().queryParam("filter", "fid=56").expect().body("features", hasSize(1)).get("/features/ne1/populated_places.json");
        
    }

    @Test
    public void testNotExisting() {
        expect().body(is("Not Found")).when().get("/data/foo");
    }
}
