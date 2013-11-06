package fixture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;

import static tests.Config.logger;

/**
 *
 */
public class Fixture {

    static final List<Fixture> data = new ArrayList<Fixture>();

    String url;
    DataSet[] datasets;
    String typeName;
    String destinationFile;

    private Fixture(String url, DataSet... datasets) {
        this.url = url;
        data.add(this);
        this.datasets = datasets;

        List<NameValuePair> kvp;
        String[] path;
        try {
            URL source = new URL(url);
            path = source.getPath().split("/");
            kvp = URLEncodedUtils.parse(new URI(url), "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        NameValuePair typeNameKVP = null;
        for (int i = 0; i < kvp.size(); i++) {
            if (kvp.get(i).getName().equals("typeName")) {
                typeNameKVP = kvp.get(i);
                break;
            }
        }
        if (typeNameKVP != null) {
            this.typeName = typeNameKVP.getValue().replace(':', '_');
            this.destinationFile = this.typeName + ".json";
        } else {
            this.typeName = path[path.length - 1];
            this.destinationFile = this.typeName;
        }
    }

    static Fixture url(String url, DataSet... datasets) {
        return new Fixture(url, datasets);
    }

    public static List<Fixture> fixtures() {
        return data;
    }

    public static Collection<DataSet> allDataSets() {
        List<DataSet> all = new ArrayList<DataSet>();
        for (Fixture f : data) {
            all.addAll(Arrays.asList(f.datasets));
        }
        return all;
    }

    public static Collection<DataSet> vectorDataSets() {
        List<DataSet> all = new ArrayList<DataSet>();
        for (Fixture f : data) {
            all.addAll(DataSet.filter(DataSet.DataType.VECTOR, f.datasets));
        }
        return all;
    }

    static {
        Fixture.url("http://data.opengeo.org/geodroid/Geodata.zip",
            DataSet.workspace("ne1",
                DataSet.tiles("tiles")
            ),
            DataSet.workspace("va",
                DataSet.vector("admin_0_countries")
                    .expectFeatureCount(1),
                DataSet.vector("admin_0_boundary_lines_land")
                    .expectFeatureCount(0),
                DataSet.vector("admin_1_states_provinces_lines_shp")
                    .expectFeatureCount(9),
                DataSet.vector("parks_and_protected_lands_scale_rank")
                    .expectFeatureCount(1),
                DataSet.vector("lakes_north_america")
                    .expectFeatureCount(2),
                DataSet.vector("urban_areas")
                    .expectFeatureCount(28),
                DataSet.vector("populated_places")
                    .expectFeatureCount(15),
                DataSet.vector("roads_north_america")
                    .expectFeatureCount(1401)
            ),
            DataSet.json("states")
                .expectFeatureCount(49),
            DataSet.mbtiles("geography")
        );

    }

    public void fetchData(File cache, File unzipped) throws Exception {
        HttpClient client = new DefaultHttpClient();
        boolean fetch = true;
        File target = new File(cache, destinationFile);

        if (target.exists()) {
            long size = target.length();
            HttpResponse response = client.execute(new HttpHead(url));
            String serverSize = response.getFirstHeader("Content-Length").getValue();
            // this is not so great but since it's a zip, hardly likely to match
            fetch = Long.valueOf(serverSize) != size;
        }
        if (fetch) {
            logger.info("downloading " + url);
            HttpResponse response = client.execute(new HttpGet(url));
            OutputStream out = new FileOutputStream(target);
            response.getEntity().writeTo(out);
            out.close();
            if (target.getName().endsWith(".zip")) {
                logger.info("unpacking " + target);
                ZipFile zf = new ZipFile(target);
                Enumeration<? extends ZipEntry> entries = zf.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry e = entries.nextElement();
                    File entryDestination = new File(unzipped, e.getName());
                    if (e.isDirectory()) {
                        entryDestination.mkdirs();
                    } else {
                        logger.info("extracting " + entryDestination);
                        entryDestination.getParentFile().mkdirs();
                        InputStream in = zf.getInputStream(e);
                        out = new FileOutputStream(entryDestination);
                        IOUtils.copy(in, out);
                        IOUtils.closeQuietly(in);
                        IOUtils.closeQuietly(out);
                    }
                }
            }
        } else {
            logger.info("local data matches size of remote, skipping download");
        }
    }
}
