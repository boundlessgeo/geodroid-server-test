package tests;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.specification.RequestSpecification;

public class Extra {
    
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

    public static RequestSpecification givenWithReport() {
        return given().filter(Reporter.reportResponseFilter()).filter(Reporter.reportRequestFilter(false));
    }

    public static RequestSpecification givenWithRequestReport() {
        return given().filter(Reporter.reportRequestFilter(false));
    }

    public static RequestSpecification givenWithRequestBodyReport() {
        return given().filter(Reporter.reportRequestFilter(true));
    }

    public static String dequote(String json) {
        return json.replaceAll("'", "\"");
    }

}
