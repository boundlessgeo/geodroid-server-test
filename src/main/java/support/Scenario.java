package support;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Map;

/**
 * An means to uniquely identify results from different devices and software
 * versions. Currently an MD5 digest of device properties and the apk hashsum.
 *
 * @todo identify properties and an apk identifier to create a less opaque identifier
 */
public class Scenario {

    public static Scenario instance;
    private final Map<String, String> deviceBuildAndProductProps;
    private final String packageName;
    private final String packageHash;
    private final String scenario;
    
    public Scenario() throws Exception {
        deviceBuildAndProductProps = ADB.getDeviceBuildAndProductProps();
        String packageHashSum = ADB.getPackageHashSum();
        String[] parts = packageHashSum.split("\\s+");
        packageName = parts[0];
        packageHash = parts[1];
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bytes);
        for (Map.Entry<String, String> entry : deviceBuildAndProductProps.entrySet()) {
            ps.print(entry.getKey());
            ps.print(entry.getValue());
        }
        ps.print(packageHashSum);
        scenario = new BigInteger(1, md5.digest(bytes.toByteArray())).toString(16);
    }

    public Map<String, String> getDeviceBuildAndProductProps() {
        return deviceBuildAndProductProps;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getPackageHash() {
        return packageHash;
    }

    public String getScenario() {
        return scenario;
    }

    public static Scenario scenario() {
        if (instance == null) {
            try {
                instance = new Scenario();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return instance;
    }
    
}
