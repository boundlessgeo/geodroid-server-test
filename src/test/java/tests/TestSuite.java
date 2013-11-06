package tests;

import java.util.logging.Logger;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
//@SuiteClasses({TestRead.class, TestDataEndPoint.class, TestFeatureOutputPNG.class})
@SuiteClasses({TestRead.class, TestData.class, TestDataEndPoint.class, TestFeatureOutputPNG.class, TestFeatureOutputJSON.class})
public class TestSuite {

    static void error(Throwable ex) {
        System.out.println("");
        System.out.println("Make sure the service is running at the location specified");
        System.out.println("I tried " + Config.getBaseURI());
        System.out.println("The error message is : " + ex.getMessage());
        System.exit(1);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        Config.init();
        if (Config.installData()) {
            Fixtures fixture = new Fixtures();
            Logger logger = Logger.getLogger("");
            logger.info("downloading fixture data");
            fixture.getData();
            logger.info("installing fixture data");
            fixture.installData();
        }
        ADB.adbCommand("shell", "am startservice --user 0 org.geodroid.server/.GeodroidServerService");
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

}
