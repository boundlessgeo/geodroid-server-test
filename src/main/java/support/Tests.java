package support;

import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import static org.junit.Assert.assertTrue;
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

    public Response getFeatures(DataSet dataSet, boolean report) {
        RequestSpecification request = report ? givenWithRequestReport() : given();
        request.pathParam("dataset", dataSet.name);
        String route = "/features/{dataset}";
        if (dataSet.getParent() != null) {
            route = "/features/{workspace}/{dataset}";
            request.pathParam("workspace", dataSet.getParent().name);
        }
        return request.expect()
            .body("features", hasSize(dataSet.getExpectedFeatureCount()))
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

    public void getTiles(DataSet dataSet, int z, int x, int y) {
        String route = "/tiles/{workspace}/{dataset}/{z}/{x}/{y}?origin=top_left";
        Response resp = givenWithRequestReport()
                .pathParam("workspace", dataSet.getParent().name)
                .pathParam("dataset", dataSet.name)
                .pathParam("z", z)
                .pathParam("x", x)
                .pathParam("y", y)
        .expect()
                //.contentType("image/png") currently responds with no content-type
                .get(route);
        // hmm, hard to tell what we get back
        assertTrue(isJPEG(resp.asInputStream()));
    }

    public static boolean isPNG(InputStream stream) {
        return isImage(stream, "png");
    }

    public static boolean isJPEG(InputStream stream) {
        return isImage(stream, "jpg");
    }

    public static boolean isImage(InputStream stream, String type) {
        BufferedImage read = null;
        ImageReader reader = ImageIO.getImageReadersBySuffix(type).next();
        try {
            reader.setInput(ImageIO.createImageInputStream(stream));
            read = reader.read(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            // not an image
        }
        return read != null;
    }

}
