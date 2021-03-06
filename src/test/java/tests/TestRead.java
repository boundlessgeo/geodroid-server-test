package tests;

import org.junit.Test;
import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.http.ContentType;
import static org.hamcrest.Matchers.*;
import support.BaseTest;

/**
 * Tests for non-data specific functionality
 */
public class TestRead extends BaseTest {

    @Test
    public void testPing() {
        expect().body(containsString("Geodroid Server"))
                .contentType(ContentType.HTML)
                .get("/");
    }
    
    @Test
    public void testNotExisting() {
        expect().body(is("No such workspace: foo")).when().get("/data/foo");
    }

}
