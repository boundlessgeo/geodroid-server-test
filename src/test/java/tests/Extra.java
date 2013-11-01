package tests;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

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


//    public static <T> Matcher<T> isPNG() {
//        return new BaseMatcher<T>() {
//
//            public boolean matches(Object item) {
//
//            }
//
//            public void describeTo(Description description) {
//                description.appendText("expected PNG, got something else");
//            }
//        };
//    }
}
