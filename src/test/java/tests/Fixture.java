package tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

/**
 * These are marked as 'tests' to allow quickly testing them.
 * Because the tests are run through the suite, this will not get picked up
 * by surefire.
 */
public class Fixture {

    File cache = new File("cache");
    File unzipped = new File(cache, "unzipped");
    File geodata = new File(cache, "Geodata.zip");
    File unzippedGeodata = new File(unzipped, "Geodata");

    @Test
    public void getData() throws Exception {
        String url = "https://dl.dropboxusercontent.com/u/1663985/Geodata.zip";
        cache.mkdir();
        unzipped.mkdir();

        HttpClient client = new DefaultHttpClient();
        boolean fetch = true;
        if (geodata.exists()) {
            long size = geodata.length();
            HttpResponse response = client.execute(new HttpHead(url));
            String serverSize = response.getFirstHeader("Content-Length").getValue();
            // this is not so great but since it's a zip, hardly likely to match
            fetch = Long.valueOf(serverSize) != size;
        }
        if (fetch) {
            HttpResponse response = client.execute(new HttpGet(url));
            Header[] allHeaders = response.getAllHeaders();
            for (Header h: allHeaders) {
                System.out.println(h.getName() + " " + h.getValue());
            }
            OutputStream out = new FileOutputStream(geodata);
            response.getEntity().writeTo(out);
            out.close();
            ZipFile zf = new ZipFile(geodata);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                File entryDestination = new File(unzipped, e.getName());
                if (e.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zf.getInputStream(e);
                    out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
            }
        } else {
            Logger.getGlobal().info("local data matches size of remote, skipping download");
        }
    }

    @Test
    public void installData() throws IOException, InterruptedException {
        String adbCommand = Config.getAdbCommand();
        String dest = Config.getDeviceGeoDataLocation();
        File[] data = unzippedGeodata.listFiles();
        ProcessBuilder pb = new ProcessBuilder(adbCommand, "shell",
                "md5sum " + dest + "/*"
        );
        Process process = pb.start();
        process.waitFor();
        List<String> hashSums = IOUtils.readLines(process.getInputStream());
        Map<String, String> hashes = new HashMap<String,String>();
        for (String line: hashSums) {
            String[] hashAndFile = line.split(" ");
            File f = new File(hashAndFile[1]);
            hashes.put(f.getName(), hashAndFile[0]);
        }
        for (File f: data) {
            String md5Hex = DigestUtils.md5Hex(new FileInputStream(f));
            String existing = hashes.get(f.getName());
            if (existing != null && existing.equals(md5Hex)) {
                Logger.getGlobal().info("skipping " + f + " as it's already there");
                continue;
            }
            Logger.getGlobal().info("installing " + f + " to device at " + dest);
            pb = new ProcessBuilder(
                    adbCommand, "push",
                    f.getAbsolutePath(), dest);
            pb.inheritIO();
            Process proc = pb.start();
            int retval = proc.waitFor();
            if (retval != 0) {
                throw new IOException("Failed copying data to device");
            }
        }
    }
}
