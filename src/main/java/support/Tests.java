package support;

import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Implementation of tests.
 */
public class Tests {
    private Reporter reporter;

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public RequestSpecification givenWithReport() {
        return reporter != null ?
                given().filter(reporter.reportResponseFilter()).filter(reporter.reportRequestFilter(false)) :
                given();
    }

    public RequestSpecification givenWithRequestReport() {
        return reporter != null ? given().filter(reporter.reportRequestFilter(false)) :
                given();
    }

    public RequestSpecification givenWithRequestBodyReport() {
        return reporter != null ? given().filter(reporter.reportRequestFilter(true)) :
                given();
    }

    public Response getFeatureById(DataSet dataSet, int id) {
        String route = "/features/{workspace}/{dataset}/{id}";
        RequestSpecification request = givenWithRequestReport().pathParams(
                "workspace", dataSet.getParent().name,
                "dataset", dataSet.name,
                "id", Integer.toString(id));
        return request.expect()
                .statusCode(200)
                .body("features", hasSize(1))
                .body("features.id", hasItems(Integer.toString(id)))
                .get(route);
    }

    public Response getDataEndPoint(DataSet parent, DataSet child) {
        RequestSpecification request = givenWithReport().pathParam("dataset", parent.name);
        Response response;
        if (child == null) {
            ResponseSpecification resp;
            if (parent.type == DataSet.TopLevelType.WORKSPACE) {
                resp = request.expect()
                        .body("datasets", containsInAnyOrder(parent.childrenNames()));
            } else {
                resp = request.expect()
                        .body("name", is(parent.name));
                if (parent.getExpectedFeatureCount() != null) {
                    resp.body("count", is(parent.getExpectedFeatureCount()));
                }
            }
            response = resp.get("/data/{dataset}");
        } else {
            ResponseSpecification resp = request.pathParam("child", child.name).expect()
                    .body("name", is(child.name));
            if (child.getExpectedFeatureCount() != null) {
                    resp.body("count", is(child.getExpectedFeatureCount()));
            }
            response = resp.get("/data/{dataset}/{child}");
        }
        return response;
    }

}
