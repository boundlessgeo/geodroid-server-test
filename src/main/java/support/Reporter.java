package support;

import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.filter.log.LogDetail;
import com.jayway.restassured.filter.log.ResponseLoggingFilter;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * Try to encapsulate report writing and file management.
 *
 * While a RunListener, the Reporter is often setup in a way that it won't
 * receive start/end of all tests message.
 * @author Ian Schneider <ischneider@boundlessgeo.com>
 */
public class Reporter extends RunListener {

    private static ByteArrayOutputStream currentReportFilterStream;
    private Filter doNothingFilter = new Filter() {
        @Override
        public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
            return ctx.next(requestSpec, responseSpec);
        }
    };
    private boolean recordResponses = false;
    private Pattern paramPattern = Pattern.compile("run\\[(.*)\\]");
    private File captureDir;
    private ReportWriter writer;
    private CSVReportWriter csvWriter;
    private XMLReportWriter xmlWriter;
    private String currentTestName;
    private long time;
    private final File baseDirectory;

    protected Reporter(File baseDirectory) {
        System.out.println("reporting to " + baseDirectory);
        this.baseDirectory = baseDirectory;
    }

    public static Reporter getProfileReporter() {
        return new Reporter(new File("profile-report"));
    }

    private File getCaptureDir() {
        if (captureDir == null) {
            captureDir = getDirectory("capture");
        }
        return captureDir;
    }

    private CSVReportWriter getCSVWriter() {
        if (csvWriter == null) {
            csvWriter = new CSVReportWriter(getDirectory("csv"));
        }
        return csvWriter;
    }

    private XMLReportWriter getXMLWriter() {
        if (xmlWriter == null) {
            xmlWriter = new XMLReportWriter(new File(getDirectory("report"), "report.xml"));
        }
        return xmlWriter;
    }

    private File getDirectory(String path) {
        File f = new File(baseDirectory, path);
        f.mkdirs();
        return f;
    }

    public File getFile(String path) {
        File f = new File(baseDirectory, path);
        f.getParentFile().mkdirs();
        return f;
    }

    public void toggleCSVReportingMode() {
        writer = getCSVWriter();
        recordResponses = false;
    }

    public Filter reportResponseFilter() {
        if (!recordResponses) {
            return doNothingFilter;
        }
        if (currentReportFilterStream != null) {
            throw new RuntimeException();
        }
        currentReportFilterStream = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(currentReportFilterStream, true);
        return new ResponseLoggingFilter(LogDetail.BODY, stream);
    }

    public Filter reportRequestFilter(final boolean logBody) {
        if (writer == null) {
            return doNothingFilter;
        }
        return new Filter() {

            @Override
            public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                String url = ctx.getCompleteRequestPath();
                if (!requestSpec.getQueryParams().isEmpty()) {
                    List<BasicNameValuePair> kvp = new ArrayList<BasicNameValuePair>();
                    for (String k : requestSpec.getQueryParams().keySet()) {
                        kvp.add(new BasicNameValuePair(k, requestSpec.getQueryParams().get(k).toString()));
                    }
                    url += "?" + URLEncodedUtils.format(kvp, "UTF-8");
                }
                String body = null;
                if (logBody) {
                    body = requestSpec.getBody().toString();
                }
                try {
                    writer.request(ctx.getRequestMethod().name(), url, requestSpec.getQueryParams(), body);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                Response resp = ctx.next(requestSpec, responseSpec);
                try {
                    writer.response(resp);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                return resp;
            }
        };
    }

    public void reportImage(String name, byte[] data) {
        if (writer == null) return;
        File f = new File(new File(baseDirectory, "report"), name);
        f.getAbsoluteFile().getParentFile().mkdirs();
        try {
            FileOutputStream fout = new FileOutputStream(f);
            fout.write(data);
            fout.close();
            writer.reportImage(f);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void writeScenario() {
        PrintWriter pw = null;
        Scenario s = Scenario.scenario();
        File file = getFile("scenario.txt");
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        pw.println(s.getPackageName() + "=" + s.getPackageHash());
        for (Map.Entry<String, String> k : s.getDeviceBuildAndProductProps().entrySet()) {
            pw.println(k.getKey() + "=" + k.getValue());
        }
        pw.close();
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        writer.failure(failure);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        long elapsed = System.currentTimeMillis() - time;
        if (currentReportFilterStream != null) {
            File output = new File(getCaptureDir(), currentTestName + ".txt");
            FileOutputStream fout = new FileOutputStream(output);
            currentReportFilterStream.writeTo(fout);
            fout.close();
            writer.responseBody(currentReportFilterStream.toString());
        }
        writer.finishTest(elapsed);
    }

    public void finish() {
        try {
            writer.endReport();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void testStarted(Description description) throws Exception {
        // a test has started, default to xmlWriter
        if (writer == null) {
            recordResponses = true;
            writer = getXMLWriter();
            writer.startReport();
        }
        // an individual test has started
        currentReportFilterStream = null;
        // ugh, unpack test name from the Description object
        Matcher m = paramPattern.matcher(description.getDisplayName());
        String testName = description.getMethodName();
        if (m.find()) {
            testName = m.group(1);
        }
        currentTestName = testName;
        writer.startTest(testName);
        time = System.currentTimeMillis();
    }

    public static interface ReportWriter {

        void startReport() throws Exception;

        void endReport() throws Exception;

        void startTest(String testName) throws Exception;

        void finishTest(long elapsed) throws Exception;

        void request(String method, String url, Map<String, ?> queryParams, String body) throws Exception;

        void response(Response response) throws Exception;

        void responseBody(String data) throws Exception;

        void failure(Failure f) throws Exception;

        void reportImage(File f) throws Exception;
    }

    static class CSVReportWriter implements ReportWriter {

        PrintWriter writer;
        private final File baseDir;
        String currentTest;
        Map<String, PrintWriter> writers;
        private String url;
        private long timestamp;

        public CSVReportWriter(File baseDir) {
            this.baseDir = baseDir;
            writers = new HashMap<String, PrintWriter>();
        }

        @Override
        public void startReport() {
        }

        @Override
        public void endReport() {
            for (PrintWriter w : writers.values()) {
                w.close();
            }
        }

        @Override
        public void startTest(String testName) throws Exception {
            System.out.println("startTest " + testName);
            writer = writers.get(testName);
            if (writer == null) {
                writers.put(testName, writer = new PrintWriter(new File(baseDir, testName + ".csv")));
                writer.write("timestamp,elapsed,responseCode,success,URL");
                writer.println();
            }
        }

        @Override
        public void finishTest(long elapsed) throws Exception {
            // we're tracking requests, so not concerned about total timing
        }

        @Override
        public void request(String method, String url, Map<String, ?> queryParams, String body) {
            timestamp = System.currentTimeMillis();
            this.url = url;
        }

        @Override
        public void responseBody(String data) throws Exception {
        }

        @Override
        public void response(Response response) throws Exception {
            StringBuilder sb = new StringBuilder();
            char sep = ',';
            sb.append(timestamp).append(sep);
            sb.append(System.currentTimeMillis() - timestamp).append(sep);
            sb.append(200).append(sep);
            sb.append("true").append(sep);
            sb.append(url);
            writer.write(sb.toString());
            writer.println();
        }

        @Override
        public void failure(Failure f) {
        }

        @Override
        public void reportImage(File f) throws Exception {
        }

    }

    static class XMLReportWriter implements ReportWriter {

        private final XMLStreamWriter writer;
        private final File dest;

        XMLReportWriter(File dest) {
            try {
                this.dest = dest;
                XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(new FileOutputStream(dest));
                this.writer = new IndentingXMLStreamWriter(writer);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private void start(String name, Object... attrs) {
            try {
                writer.writeStartElement(name);
                for (int i = 0; i < attrs.length; i += 2) {
                    writer.writeAttribute(attrs[i].toString(), attrs[i + 1].toString());
                }
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }

        private void end() {
            try {
                writer.writeEndElement();
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }

        private void element(String name, Object data, Object... attrs) {
            start(name, attrs);
            try {
                writer.writeCharacters(data.toString());
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
            end();
        }

        public void endReport() throws Exception {
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();

            File reportDir = dest.getParentFile();
            InputStream stream = getClass().getResourceAsStream("/report.css");
            IOUtils.copy(stream, new FileOutputStream(new File(reportDir,"report.css")));
        }

        public void failure(Failure failure) {
            element("failure", failure.getMessage());
        }

        public void finishTest(long l) throws Exception {
            element("elapsed", l);
            writer.writeEndElement();
        }

        public void reportImage(File f) throws XMLStreamException {
            writer.writeStartElement("img");
            String relPath = f.getAbsolutePath().replace(dest.getParentFile().getAbsolutePath(), "");
            writer.writeAttribute("src", relPath.substring(1));
            writer.writeEndElement();
        }

        public void request(String method, String url, Map<String, ?> queryParams, String body) {
            start("request");
            element("method", method);
            element("url", url);
            if (body != null) {
                element("body", formatJSON(body));
            }
            end();
        }

        private String formatJSON(String json) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Object readValue = mapper.readValue(json, Object.class);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectWriter writer = mapper.defaultPrettyPrintingWriter();
                DefaultPrettyPrinter pprinter = new DefaultPrettyPrinter();
                // make sure arrays get indented
                pprinter.indentArraysWith(new DefaultPrettyPrinter.Lf2SpacesIndenter());
                writer = writer.withPrettyPrinter(pprinter);
                writer.writeValue(baos, readValue);
                return baos.toString();
            } catch (org.codehaus.jackson.JsonParseException jpe) {
                return json;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void response(Response response) {
        }

        @Override
        public void responseBody(String response) {
            element("response", formatJSON(response));
        }

        @Override
        public void startReport() throws Exception {
            writer.writeStartDocument();
            writer.writeProcessingInstruction("xml-stylesheet type=\"text/css\" href=\"report.css\"");
            writer.writeStartElement("report");
            writer.writeAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        }

        @Override
        public void startTest(String testName) throws Exception {
            writer.writeStartElement("test");
            element("name", testName);
        }
    }

}
