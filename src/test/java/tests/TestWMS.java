package tests;

import org.junit.Test;
import support.BaseTest;
import static support.Fixture.VA_PLACES;
import static support.Fixture.VA_ROADS;

/**
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class TestWMS extends BaseTest {

    @Test
    public void testWMSRoads() throws Exception {
        tests.getWMSImage("EPSG:4326", null, "35.071847116597,-84.845603303943,42.108590280659,-69.437278108631", 1402, 640, VA_ROADS);
    }

    @Test
    public void testWMSRoadsWithStyle() throws Exception {
        tests.getWMSImage("EPSG:4326", "roads", "35.071847116597,-84.845603303943,42.108590280659,-69.437278108631", 1402, 640, VA_ROADS);
    }

    @Test
    public void testWMSLayersStyles() throws Exception {
        tests.getWMSImage("EPSG:4326", "roads,places", "35.071847116597,-84.845603303943,42.108590280659,-69.437278108631", 1402, 640, VA_ROADS, VA_PLACES);
    }
}
