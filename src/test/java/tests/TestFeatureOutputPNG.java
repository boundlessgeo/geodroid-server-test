package tests;

import support.Reporter;
import com.jayway.restassured.response.Response;
import java.awt.image.BufferedImage;
import support.DataSet;
import support.Fixture;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import support.BaseTest;

/**
 *
 */
@RunWith(value = Parameterized.class)
public class TestFeatureOutputPNG extends BaseTest {

    private final DataSet dataSet;
    private final String style;

    public TestFeatureOutputPNG(DataSet dataSet, String style) {
        this.dataSet = dataSet;
        this.style = style;
    }

    @Test
    public void run() throws Exception {
        String route = style.length() == 0 ?
                "/features/{parent}/{name}.png":
                "/features/{parent}/{name}.png?style={style}";
        List<String> params = new ArrayList<String>();
        params.add(dataSet.getParent().name);
        params.add(dataSet.name);
        if (style.length() != 0) {
            params.add(style);
        }
        Response resp = tests.givenWithRequestReport().get(route, params.toArray());
        assertEquals(200, resp.getStatusCode());
        byte[] data = IOUtils.toByteArray(resp.asInputStream());
        assertTrue("Expected a PNG", isPNG(new ByteArrayInputStream(data)));
        if (reporter != null) {
            reporter.reportImage(String.format("TestFeatureOutputPNG-%s-%s.png", dataSet.name, style), data);
        }
    }

    public static boolean isPNG(InputStream stream) {
        BufferedImage read = null;
        ImageReader reader = ImageIO.getImageReadersBySuffix("png").next();
        try {
            reader.setInput(ImageIO.createImageInputStream(stream));
            read = reader.read(0);
        } catch (IOException ex) {
            ex.printStackTrace();
            // not an image
        }
        return read != null;
    }

    @Parameterized.Parameters(name = "TestFeatureOutputPNG-{0}-{1}")
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();
        for (DataSet ds: activeFixture().vectorDataSets()) {
            data.add(new Object[] {ds, ""});
            for (String style: ds.getAssociatedStyles()) {
                data.add(new Object[]{ds, style});
            }
        }
        return data;
    }
}
