package tests;

import tests.ExtraMatchers;
import com.jayway.restassured.RestAssured;
import org.junit.Test;
import static tests.ExtraMatchers.*;
import static com.jayway.restassured.RestAssured.*;
import java.io.InputStream;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;

public class TestRead {

    @BeforeClass
    public static void init() {
        RestAssured.baseURI = "http://192.168.0.15";
        RestAssured.port = 8000;
    }

    public void testPing() {
        expect().body(is("")).when().get("/");
    }

    @Test
    public void testData() {
        expect().body("air_runways", hasEntry("type", "dataset"))
                .body("ne", hasEntry("type", "workspace"))
                .when().get("/data");
    }

    @Ignore
    public void testAirRunways() {
        expect().body("name", is("air_runways"))
                .body("type", is("vector"))
                .body("bbox", hasSize(4))
                .body("count", is(319))
                .body("schema", hasKey("OBJECTID"))
                .body("features", is("/features/air_runways.json"))
                .when().get("/data/air_runways");
    }

    @Test
    public void testNe() {
        expect().body("type", is("workspace"))
                .body("datasets", containsInAnyOrder("populated_places", "tiles"))
                .when().get("/data/ne");
    }

    @Test
    public void testFeatureService() {
        InputStream stream = get("/features/ne/populated_places.png").asInputStream();
        assertTrue(isPNG(stream));
    }

    @Test
    public void testNotExisting() {
        expect().body(is("Not Found")).when().get("/data/foo");
    }
}
