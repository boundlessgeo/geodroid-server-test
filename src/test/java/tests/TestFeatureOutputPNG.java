package tests;

import com.jayway.restassured.response.Response;
import fixture.DataSet;
import fixture.Fixture;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static tests.Extra.givenWithRequestReport;

/**
 *
 */
@RunWith(value = Parameterized.class)
public class TestFeatureOutputPNG {

    private final DataSet dataSet;

    @BeforeClass
    public static void init() {
        Config.init();
    }

    public TestFeatureOutputPNG(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Test
    public void run() throws Exception {
        String route = dataSet.getParent() == null ?
                "/features/{name}.png" : "/features/{parent}/{name}.png";
        List<String> params = new ArrayList<String>();
        if (dataSet.getParent() != null) {
            params.add(dataSet.getParent().name);
        }
        params.add(dataSet.name);
        Response resp = givenWithRequestReport().get(route, params.toArray());
        byte[] data = IOUtils.toByteArray(resp.asInputStream());
        assertTrue("Expected a PNG",
                Extra.isPNG(new ByteArrayInputStream(data)));
        File f = new File(String.format("target/images/TestFeatureOutputPNG-%s.png", dataSet.name));
        Reporter.instance.writer.reportImage(f);
        f.getAbsoluteFile().getParentFile().mkdirs();
        FileOutputStream fout = new FileOutputStream(f);
        fout.write(data);
        fout.close();
    }

    @Parameterized.Parameters(name = "TestFeatureOutputPNG-{0}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: Fixture.vectorDataSets()) {
            data.add(new Object[] {ds});
        }
        return data;
    }
}
