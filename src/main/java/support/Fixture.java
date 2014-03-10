package support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import static support.Config.logger;

/**
 *
 */
public class Fixture {

    public static final DataSet NE_TILES;
    public static final DataSet VA_COUNTRIES;
    public static final DataSet VA_LAND_BOUNDARIES;
    public static final DataSet VA_STATES;
    public static final DataSet VA_PARKS;
    public static final DataSet VA_LAKES;
    public static final DataSet VA_URBAN;
    public static final DataSet VA_PLACES;
    public static final DataSet VA_ROADS;

    public static final DataSet NE1;
    public static final DataSet VA;
    public static final DataSet STATES_STATES;
    public static final DataSet GEOGRAPHY_GEOGRAPHY;
    public static final DataSet GUN_DEATHS_GUN_DEATHS;
    public static final DataSet STATES;
    public static final DataSet GEOGRAPHY;
    public static final DataSet GUN_DEATHS;
    public static final DataSet MEDFORD_PARKS_MEDFORD_PARKS;
    public static final DataSet MEDFORD_PARKS;

    static final Fixture V1;
    static final Fixture V2;

    private final String url;
    private final DataSet[] datasets;
    private final String destinationFile;
    private final File localTarget;

    private static final File cache = new File("cache");
    private static final File unzipped = new File(cache, "unzipped");

    private Fixture(String url, DataSet... datasets) {
        this.url = url;
        this.datasets = datasets;

        List<NameValuePair> kvp;
        String[] path;
        try {
            URL source = new URL(url);
            path = source.getPath().split("/");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        this.destinationFile = path[path.length - 1];
        this.localTarget = new File(cache, destinationFile);
    }

    private static Fixture url(String url, DataSet... datasets) {
        return new Fixture(url, datasets);
    }

    public Collection<DataSet> allDataSets() {
        return Arrays.asList(datasets);
    }

    public Collection<DataSet> vectorDataSets() {
        return DataSet.filter(DataSet.DataType.VECTOR, datasets);
    }

    static {
        V1 = Fixture.url("http://data.opengeo.org/geodroid/geodroid-test-data.zip",
            NE1 = DataSet.workspace("ne1",
                NE_TILES = DataSet.tiles("tiles")
            ),
            VA = DataSet.workspace("va",
                VA_COUNTRIES = DataSet.vector("admin_0_countries")
                    .expectFeatureCount(1),
                VA_LAND_BOUNDARIES = DataSet.vector("admin_0_boundary_lines_land")
                    .expectFeatureCount(0),
                VA_STATES = DataSet.vector("admin_1_states_provinces_lines_shp")
                    .expectFeatureCount(9),
                VA_PARKS = DataSet.vector("parks_and_protected_lands_scale_rank")
                    .expectFeatureCount(1)
                    .associatedStyles("parks"),
                VA_LAKES = DataSet.vector("lakes_north_america")
                    .expectFeatureCount(2),
                VA_URBAN = DataSet.vector("urban_areas")
                    .expectFeatureCount(28),
                VA_PLACES = DataSet.vector("populated_places")
                    .expectFeatureCount(15)
                    .associatedStyles("places"),
                VA_ROADS = DataSet.vector("roads_north_america")
                    .expectFeatureCount(1401)
                    .associatedStyles("roads")
            ),
            STATES = DataSet.workspace("states",
                STATES_STATES = DataSet.vector("states")
                    .expectFeatureCount(49)
            ),
            GEOGRAPHY = DataSet.workspace("geography",
                GEOGRAPHY_GEOGRAPHY = DataSet.mbtiles("geography")
            ),
            GUN_DEATHS = DataSet.workspace("gun_deaths",
                GUN_DEATHS_GUN_DEATHS = DataSet.vector("gun_deaths")
                    .expectFeatureCount(99)
            )
        );
        V2 = Fixture.url("http://data.boundlessgeo.com/mobile/geodroid-test-data-v2.zip",       
            NE1, VA, STATES, GEOGRAPHY, GUN_DEATHS,
            MEDFORD_PARKS = DataSet.workspace("medford_parks",
                    MEDFORD_PARKS_MEDFORD_PARKS = DataSet.vector("medford_parks").
                            expectFeatureCount(88)
            )
        );
    }

    private File getUnpackDirectory() {
        File dir = new File(unzipped, localTarget.getName().replace(".zip", ""));
        dir.mkdirs();
        return dir;
    }

    public void getData() throws Exception {
        fetchData();
    }

    public void fetchData() throws Exception {
        HttpClient client = new DefaultHttpClient();
        boolean fetch = true;

        if (localTarget.exists()) {
            long size = localTarget.length();
            HttpResponse response = client.execute(new HttpHead(url));
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println("invalid fixture URL " + url);
                System.out.println(response.getStatusLine());
                System.exit(1);
            }
            String serverSize = response.getFirstHeader("Content-Length").getValue();
            // this is not so great but since it's a zip, hardly likely to match
            fetch = Long.valueOf(serverSize) != size;
        }
        if (fetch) {
            logger.info("downloading " + url);
            HttpResponse response = client.execute(new HttpGet(url));
            localTarget.getParentFile().mkdirs();
            OutputStream out = new FileOutputStream(localTarget);
            response.getEntity().writeTo(out);
            out.close();
        } else {
            logger.info("local data matches size of remote, skipping download");
        }
        File unpackDirectory = getUnpackDirectory();
        if (unpackDirectory.listFiles().length == 0) {
            extractData(localTarget, unpackDirectory);
        }
    }

    private void extractData(File srcZip, File targetDir) throws Exception {
        logger.info("unpacking " + srcZip + " to " + targetDir);
        ZipFile zf = new ZipFile(srcZip);
        Enumeration<? extends ZipEntry> entries = zf.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            File entryDestination = new File(targetDir, e.getName());
            if (e.isDirectory()) {
                entryDestination.mkdirs();
            } else {
                logger.info("extracting " + entryDestination);
                entryDestination.getParentFile().mkdirs();
                InputStream in = zf.getInputStream(e);
                FileOutputStream out = new FileOutputStream(entryDestination);
                IOUtils.copy(in, out);
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    public void installData() throws Exception {
        if (Config.getAdbCommand() == null) {
            logger.warning("Skipping data installation. No ADB command specified");
            return;
        }
        List<File> toIntall = new ArrayList<File>();
        List<File> directories = new ArrayList<File>();
        directories.add(getUnpackDirectory());
        while (! directories.isEmpty()) {
            File current = directories.remove(0);
            File[] files = current.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()) {
                    directories.add(file);
                } else if (!file.getName().endsWith(".zip")) {
                    toIntall.add(file);
                }
            }
        }

        String dest = Config.getDeviceGeoDataLocation();

        // to speed things up, only deploy what's needed
        List<String> hashSums = ADB.getOutputLines("shell", "md5sum " + dest + "/*");
        Map<String, String> hashes = new HashMap<String,String>();
        for (String line: hashSums) {
            String[] hashAndFile = line.split("\\s+");
            File f = new File(hashAndFile[1]);
            hashes.put(f.getName(), hashAndFile[0]);
        }
        for (File f: toIntall) {
            String md5Hex = DigestUtils.md5Hex(new FileInputStream(f));
            String existing = hashes.get(f.getName());
            if (existing != null && existing.equals(md5Hex)) {
                logger.info("skipping " + f + " as it's already there");
                continue;
            }
            logger.info("installing " + f + " to device at " + dest);
            ADB.getOutputLines("push", f.getAbsolutePath(), dest);
        }
    }

    public static void main(String[] args) throws Exception {
        Config.getActiveFixture().getData();
        Config.getActiveFixture().installData();
    }
}
