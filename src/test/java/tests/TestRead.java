package tests;

import org.junit.Test;
import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;
import org.junit.BeforeClass;

/**
 * Tests for non-data specific functionality
 */
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
    public void testNotExisting() {
        expect().body(is("not found : /data/foo")).when().get("/data/foo");
    }

}
