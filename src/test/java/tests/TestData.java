package tests;

import fixture.DataSet;
import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import com.jayway.restassured.specification.ResponseSpecification;
import fixture.Fixture;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class TestData {

    @BeforeClass
    public static void init() {
        Config.init();
    }

    @Test
    public void testData() {
        ResponseSpecification expect = expect();
        for (DataSet df: Fixture.allDataSets()) {
            expect.body(df.name, hasEntry("type", df.type.toString().toLowerCase()));
        }
        expect.when().get("/data");
    }
    
}
