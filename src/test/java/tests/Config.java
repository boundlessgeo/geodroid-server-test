package tests;

import java.io.File;
import com.jayway.restassured.RestAssured;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

/**
 *
 */
public class Config {

    static final String PROP_BASE_URI = "baseURI";
    static final String PROP_PORT = "port";
    static final String PROP_ADB_COMMAND = "adbCommand";
    static final String PROP_ADB_DEVICE = "adbDevice";
    static final String PROP_DEVICE_GEODATA = "deviceGeoData";
    static final String PROP_GEODROID_PACKAGE = "geodroidPackage";
    static final String PROP_INSTALL_DATA = "installData";

    static Properties props;
    public static final Logger logger = Logger.getLogger("");

    static File getConfigFile() {
        File props = null;
        String env = System.getenv("TEST_PROPERTIES");
        if (env != null) {
            props = new File(env);
        }
        if (props == null) {
            props = new File("test.properties");
        }
        return props;
    }

    static Properties initProperties() {
        Properties defaults = new Properties();
        defaults.setProperty(PROP_PORT, "8000");
        defaults.setProperty(PROP_DEVICE_GEODATA, "/sdcard/GeoData/");
        defaults.setProperty(PROP_INSTALL_DATA, "true");
        defaults.setProperty("devicePath", "");

        File config = getConfigFile();
        if (!config.exists()) {
            System.err.println("Unable to locate test properties file at " + config.getAbsolutePath());
        }
        Properties user = new Properties(defaults);
        try {
            user.load(new FileReader(config));
        } catch (IOException ex) {
            System.err.println("Invalid properties format");
            ex.printStackTrace();
            System.exit(1);
        }

        File adb = null;
        if (user.getProperty(PROP_ADB_COMMAND) == null) {
            String androidHome = System.getenv("ANDROID_HOME");
            if (androidHome != null) {
                File dir = new File(androidHome);
                if (!dir.exists()) {
                    System.out.println("ANDROID_HOME doesn't exist: " + dir.getAbsolutePath());
                    System.exit(1);
                }
                adb = new File(dir, new File("platform-tools", "adb").getPath());
            } else {
                System.out.println("cannot locate adb command");
                System.out.println("please provide either the ANDROID_HOME environment variable");
                System.out.println("or the full path to the adb command in the properties file");
                System.out.println("as " + PROP_ADB_COMMAND + "=<fullpath>");
                System.exit(1);
            }
        } else {
            adb = new File(user.getProperty(PROP_ADB_COMMAND));
        }
        if (!adb.exists()) {
            System.out.println("cannot find adb executable at : " + adb.getAbsolutePath());
            System.exit(1);
        }
        defaults.put(PROP_ADB_COMMAND, adb.getAbsolutePath());

        return user;
    }

    static boolean detectDeviceIP() {
        if (props.getProperty(Config.PROP_BASE_URI) != null) {
            return true;
        }
        String listing = null;
        try {
            Process netcfg = ADB.adbCommand("shell", "netcfg");
            listing = IOUtils.toString(netcfg.getInputStream());
        } catch (Exception ex) {
            System.out.println("Unable to run netcfg command to determine IP address of device.");
            System.out.println("Error is : " + ex.getMessage());
        }
        // gather any ifaces that are UP along with their IP
        Pattern p = Pattern.compile("(\\w+)\\s+UP\\s+([^/]+)");
        Matcher m = p.matcher(listing);
        List<String[]> interfaceIPs = new ArrayList<String[]>();
        String match = null;
        while (m.find()) {
            String iface = m.group(1);
            String ip = m.group(2);
            // look for a non-loopback device
            if (!iface.equals("lo")) {
                interfaceIPs.add(new String[] {iface, ip});
            }
        }
        if (interfaceIPs.size() == 1) {
            match = interfaceIPs.get(0)[1];
        } else {
            // androvm vbox devices probably have 2 interfaces - host-only and NAT
            // prefer the 192 network for host-only assuming a standard config
            for (int i = 0; i < interfaceIPs.size(); i++) {
                if (interfaceIPs.get(i)[1].startsWith("192")) {
                    match = interfaceIPs.get(i)[1];
                    break;
                }
            }
        }
        if (match != null) {
            System.out.println("Determined device IP as " + match);
            props.setProperty(Config.PROP_BASE_URI, "http://" + match);
        }
        return match != null;
    }

    static {
        init();
    }

    static void init() {
        if (props == null) {
            props = initProperties();
            if (!detectDeviceIP()) {
                System.out.println("Cannot detect the IP of the device, please provide the " + Config.PROP_BASE_URI + " property");
                System.out.println("Or ensure the device is connected to the network.");
                System.exit(1);
            }
        }
        RestAssured.baseURI = props.getProperty(PROP_BASE_URI);
        RestAssured.port = Integer.parseInt(props.getProperty(PROP_PORT));
    }

    static String getAdbCommand() {
        return props.getProperty(PROP_ADB_COMMAND);
    }

    static String getAdbDevice() {
        return props.getProperty(PROP_ADB_DEVICE);
    }

    static String getDeviceGeoDataLocation() {
        return props.getProperty(PROP_DEVICE_GEODATA);
    }

    static String getGeoDroidPackage() {
        return props.getProperty(PROP_GEODROID_PACKAGE);
    }

    static boolean installData() {
        return "true".equals(props.getProperty(PROP_INSTALL_DATA));
    }

    static String getBaseURI() {
        return props.getProperty(PROP_BASE_URI) + ":" + props.getProperty(PROP_PORT);
    }
}
