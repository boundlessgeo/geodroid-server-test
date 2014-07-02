package tests;

import org.junit.Test;
import support.BaseTest;
import static support.Fixture.VA_PLACES;
import static support.Fixture.VA_ROADS;
import static support.Fixture.VA_STATES;

/**
 *
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class TestWMS extends BaseTest {

    @Test
    public void testWMSRoads() throws Exception {
        tests.getWMSImage(null, "EPSG:4326", null, "35.071847116597,-84.845603303943,42.108590280659,-69.437278108631", 1402, 640, VA_ROADS);
    }

    @Test
    public void testWMSRoadsWithStyle() throws Exception {
        tests.getWMSImage(null, "EPSG:4326", "roads", "35.071847116597,-84.845603303943,42.108590280659,-69.437278108631", 1402, 640, VA_ROADS);
    }

    @Test
    public void testWMSLayersStyles() throws Exception {
        tests.getWMSImage(null, "EPSG:4326", "roads,places", "35.071847116597,-84.845603303943,42.108590280659,-69.437278108631", 1402, 640, VA_ROADS, VA_PLACES);
    }

    @Test
    public void testWMSLayersFilters() throws Exception {
        tests.getWMSImage("STATE_NAME neq 'Virginia';;NAME eq 'Charlottesville'", "EPSG:4326", null, "35.071847116597,-84.845603303943,42.108590280659,-69.437278108631", 1402, 640, VA_STATES, VA_ROADS, VA_PLACES);
    }

    @Test
    public void testWMSLayersStylesFilters() throws Exception {
        tests.getWMSImage("STATE_NAME neq 'Virginia';;NAME eq 'Charlottesville'", "EPSG:4326", "roads,places", "35.071847116597,-84.845603303943,42.108590280659,-69.437278108631", 1402, 640, VA_STATES, VA_ROADS, VA_PLACES);
    }
}
