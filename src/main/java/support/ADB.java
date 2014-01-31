package support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import static support.Config.logger;

/**
 *
 */
public class ADB {

    public static Process adbCommand(String command, String... args) throws Exception {
        String adbCommand = Config.getAdbCommand();
        String adbDevice = Config.getAdbDevice();
        List<String> argList = new ArrayList<String>();
        argList.add(adbCommand);
        if (adbDevice != null) {
            argList.add("-s");
            argList.add(adbDevice);
        }
        argList.add(command);
        argList.addAll(Arrays.asList(args));
        ProcessBuilder pb = new ProcessBuilder(argList);
        Process proc = pb.start();
        int retval = proc.waitFor();
        if (retval != 0) {
            logger.severe("adb command failed");
            logger.severe(argList.toString());
            List<String> output = IOUtils.readLines(proc.getInputStream());
            for (int i = 0; i < output.size(); i++) {
                logger.severe(output.get(i));
            }
            output = IOUtils.readLines(proc.getErrorStream());
            for (int i = 0; i < output.size(); i++) {
                logger.severe(output.get(i));
            }
            throw new IOException("command failed");
        }
        return proc;
    }
}
