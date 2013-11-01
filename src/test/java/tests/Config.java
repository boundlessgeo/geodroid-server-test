package tests;

import java.io.File;
import com.jayway.restassured.RestAssured;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class Config {

    static final String PROP_BASE_URI = "baseURI";
    static final String PROP_PORT = "port";
    static final String PROP_ADB_COMMAND = "adbCommand";
    static final String PROP_DEVICE_GEODATA = "deviceGeoData";

    static Properties props;

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
        defaults.setProperty(PROP_DEVICE_GEODATA, "/sdcard/GeoData");
        defaults.setProperty("fixtureData", "https://dl.dropboxusercontent.com/u/1663985/Geodata.zip");
        defaults.setProperty("devicePath", "");

        File config = getConfigFile();
        if (!config.exists()) {
            System.err.println("Unable to locate test properties file at " + config.getAbsolutePath());
            System.exit(1);
        }
        Properties user = new Properties(defaults);
        try {
            user.load(new FileReader(config));
        } catch (IOException ex) {
            System.err.println("Invalid properties format");
            ex.printStackTrace();
            System.exit(1);
        }
        verify(user);
        return user;
    }

    static void verify(Properties props) {
        String[][] expected = new String[][]{
            {PROP_BASE_URI, "The base URI where the server can be accessed"},
            {PROP_ADB_COMMAND, "The system path to the adb command"},
        };
        List<String[]> messages = new ArrayList<String[]>();
        for (String[] prop : expected) {
            if (props.getProperty(prop[0]) == null) {
                messages.add(prop);
            }
        }
        if (!messages.isEmpty()) {
            System.out.println("Please provide the following properties:");
            for (String[] args : messages) {
                System.out.println(String.format("\t%s : %s", args));
            }
            System.exit(1);
        }
    }

    static {
        init();
    }

    static void init() {
        if (props == null) {
            props = initProperties();
        }
        RestAssured.baseURI = props.getProperty(PROP_BASE_URI);
        RestAssured.port = Integer.parseInt(props.getProperty(PROP_PORT));
    }

    static String getAdbCommand() {
        return props.getProperty(PROP_ADB_COMMAND);
    }

    static String getDeviceGeoDataLocation() {
        return props.getProperty(PROP_DEVICE_GEODATA);
    }

}
