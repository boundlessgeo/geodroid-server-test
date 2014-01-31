package support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static support.Config.logger;


/**
 *
 */
public class Device {

    @Test
    public void installPackage() throws IOException, InterruptedException {
        String adbCommand = Config.getAdbCommand();
        String adbDevice = Config.getAdbDevice();
        String geodroidPackage = Config.getGeoDroidPackage();
        File geodroidPackageFile;
        if (geodroidPackage == null) {
            logger.info("No geodroidPackage specified, not installing");
            return;
        } else {
            geodroidPackageFile = new File(geodroidPackage).getAbsoluteFile();
            if (! geodroidPackageFile.exists()) {
                throw new IOException("geodroidPackage '" + geodroidPackageFile.getAbsolutePath() + "' cannot be found");
            }
        }
        List<String> args = new ArrayList<String>();
        args.add(adbCommand);
        if (adbDevice != null) {
            args.add("-s " + adbDevice);
        }
        args.add("install");
        args.add("-r"); // reinstall
        args.add(geodroidPackageFile.getAbsolutePath());
        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start();
        // danger, this could hang if more than one device is attached
        // @todo detect multiple devices and barf if adbDevice not specified
        process.waitFor();
    }
}
