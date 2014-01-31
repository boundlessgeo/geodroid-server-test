package support;

import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.junit.BeforeClass;

public class Preflight {

    static boolean ran = false;

    static void error(Throwable ex) {
        System.out.println("");
        System.out.println("Make sure the service is running at the location specified");
        System.out.println("I tried " + Config.getBaseURI());
        System.out.println("The error message is : " + ex.getMessage());
        System.exit(1);
    }

    public static void preflight() throws Exception {
        if (ran) {
            return;
        }
        ran = true;
        installData();
        Config.init();
        if (Config.getAdbCommand() != null) {
            Process start = ADB.adbCommand("shell", "am startservice --user 0 org.geodroid.server/.GeodroidServerService");
            start.waitFor();
            // waitFor will return 0 regardless
            // need to check output from the command to see
            String result = IOUtils.toString(start.getInputStream());
            if (! result.startsWith("Starting service")) {
                System.out.println("Failed to start the service, is it installed?");
                System.out.println("Error message is: ");
                System.out.println(result);
                System.exit(1);
            }
        }
        // pre-flight verify things are running
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 2000);
        HttpConnectionParams.setSoTimeout(client.getParams(), 2000);
        try {
            client.execute(new HttpGet(Config.getBaseURI()));
        } catch (HttpHostConnectException hhce) {
            error(hhce);
        } catch (ConnectTimeoutException cte) {
            error(cte);
        }
    }

    public static void installData() throws Exception {
        if (!Config.installData()) {
            System.out.println("Skipping data installation as per configuration");
            System.out.println("If any tests fail, try installing data and run again");
            return;
        }
        Fixtures fixture = new Fixtures();
        Logger logger = Logger.getLogger("");
        logger.info("downloading fixture data");
        fixture.getData();
        logger.info("installing fixture data");
        fixture.installData();
    }

}
