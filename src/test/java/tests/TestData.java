package tests;

import support.DataSet;
import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import com.jayway.restassured.specification.ResponseSpecification;
import org.junit.Test;
import support.BaseTest;

/**
 *
 */
public class TestData extends BaseTest {

    @Test
    public void testData() {
        ResponseSpecification expect = tests.givenWithReport().expect();
        for (DataSet df: activeFixture().allDataSets()) {
            expect.body(df.name, hasEntry("type", df.type.toString().toLowerCase()));
        }
        expect.when().get("/data");
    }
    
}
