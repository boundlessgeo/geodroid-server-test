package support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.io.IOUtils;
import static support.Config.logger;

/**
 *
 */
public class ADB {

    public static void pull(String src, String dest) throws Exception {
        ADB.execute("pull", src, dest);
    }

    public static void startService(boolean restart) throws Exception {
        if (restart) {
            ADB.execute("shell", "am force-stop org.geodroid.server");
        }
        String result = ADB.getOutput("shell", "am startservice --user 0 org.geodroid.server/.GeodroidServerService");
        // shell returns 0 regardless
        // need to check output from the command to see
        if (!result.startsWith("Starting service")) {
            System.out.println("Failed to start the service, is it installed?");
            System.out.println("Error message is: ");
            System.out.println(result);
            System.exit(1);
        }
    }

    public static String getPackageHashSum() throws Exception {
        List<String> lines = getOutputLines("shell", "pm", "list", "packages", "-f", "geodroid.server");
        if (lines.size() != 1) {
            throw new RuntimeException("expected to find a single package, found " + lines);
        }
        String[] parts = lines.get(0).split(":|=");
        return getOutput("shell", "md5sum", parts[1]);
    }

    public static SortedMap<String,String> getDeviceBuildAndProductProps() throws Exception {
        Config.init();
        SortedMap<String,String> props = new TreeMap<String, String>();
        List<String> lines = getOutputLines("shell", "getprop");
        for (String s: lines) {
            if (s.startsWith("[ro.build") || s.startsWith("[ro.product")) {
                String[] parts = s.split(": ");
                parts[0] = parts[0].substring(1,parts[0].length() - 1);
                parts[1] = parts[1].substring(1,parts[1].length() - 1);
                props.put(parts[0], parts[1]);
            }
        }
        return props;
    }

    public static Process execute(String command, String... args) throws Exception {
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
        return pb.start();
    }

    private static Process waitFor(Process proc, String... args) throws Exception {
        int retval = proc.waitFor();
        if (retval != 0) {
            logger.severe("adb command failed");
            if (args.length > 0) {
                logger.severe("arguments : " + Arrays.toString(args));
            }
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

    public static String getOutput(String command, String... args) throws Exception {
        Process proc = waitFor(execute(command, args));
        return IOUtils.toString(proc.getInputStream());
    }

    public static List<String> getOutputLines(String command, String... args) throws Exception {
        Process proc = waitFor(execute(command, args));
        return IOUtils.readLines(proc.getInputStream());
    }
}
