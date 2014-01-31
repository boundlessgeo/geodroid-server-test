package support;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static support.Config.logger;

/**
 * These are marked as 'tests' to allow quickly testing them.
 * Because the tests are run through the suite, this will not get picked up
 * by surefire.
 *
 * maven/ant getfile was looked at but suffered problems
 */
public class Fixtures {

    static final File cache = new File("cache");
    static final File unzipped = new File(cache, "unzipped");


    @Test
    public void getData() throws Exception {
        cache.mkdir();
        unzipped.mkdir();

        for (Fixture fixture: Fixture.fixtures()) {
            fixture.fetchData(cache, unzipped);
        }
    }

    @Test
    public void installData() throws Exception {
        if (Config.getAdbCommand() == null) {
            logger.warning("Skipping data installation. No ADB command specified");
            return;
        }
        List<File> toIntall = new ArrayList<File>();
        List<File> directories = new ArrayList<File>();
        directories.add(cache);
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
        Process proc = ADB.adbCommand("shell", "md5sum " + dest + "/*");
        List<String> hashSums = IOUtils.readLines(proc.getInputStream());
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
            ADB.adbCommand("push", f.getAbsolutePath(), dest);
        }
    }
}
