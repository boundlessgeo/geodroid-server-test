package tests;

import com.jayway.restassured.filter.Filter;
import com.jayway.restassured.filter.FilterContext;
import com.jayway.restassured.filter.log.LogDetail;
import com.jayway.restassured.filter.log.ResponseLoggingFilter;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.FilterableRequestSpecification;
import com.jayway.restassured.specification.FilterableResponseSpecification;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 *
 */
public class Reporter extends RunListener {

    static ByteArrayOutputStream currentReportFilterStream;
    static Reporter instance;
    static String reportOutput = "target/report.xml";
    Pattern paramPattern = Pattern.compile("run\\[(.*)\\]");
    File outDir = new File("target/capture/");
    ReportWriter writer = new ReportWriter(reportOutput);
    String currentTestName;
    long time;
    Failure failure;

    static {
        // for running via command line
        new File("target").mkdirs();
    }

    public Reporter() {
        instance = this;
        outDir.mkdirs();
    }

    public static Filter reportResponseFilter() {
        if (currentReportFilterStream != null) {
            throw new RuntimeException();
        }
        currentReportFilterStream = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(currentReportFilterStream, true);
        return new ResponseLoggingFilter(LogDetail.BODY, stream);
    }

    public static Filter reportRequestFilter() {
        return new Filter() {

            @Override
            public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec, FilterContext ctx) {
                String url = ctx.getCompleteRequestPath();
                if (!requestSpec.getQueryParams().isEmpty()) {
                    List<BasicNameValuePair> kvp = new ArrayList<BasicNameValuePair>();
                    for (String k: requestSpec.getQueryParams().keySet()) {
                        kvp.add(new BasicNameValuePair(k, requestSpec.getQueryParams().get(k).toString()));
                    }
                    url += "?" + URLEncodedUtils.format(kvp, "UTF-8");
                }

                instance.writer.request(url, requestSpec.getQueryParams());

                return ctx.next(requestSpec, responseSpec);
            }
        };
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        writer.failure(failure);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        long elapsed = System.currentTimeMillis() - time;
        if (currentReportFilterStream != null) {
            File output = new File(outDir, currentTestName + ".txt");
            FileOutputStream fout = new FileOutputStream(output);
            currentReportFilterStream.writeTo(fout);
            fout.close();
            writer.response(currentReportFilterStream.toString());
        }
        writer.finishTest(elapsed);
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        writer.endReport();
    }

    @Override
    public void testRunStarted(Description description) throws Exception {
        writer.startReport();
    }

    @Override
    public void testStarted(Description description) throws Exception {
        currentReportFilterStream = null;
        // bunk
        Matcher m = paramPattern.matcher(description.getDisplayName());
        String testName = description.getMethodName();
        if (m.find()) {
            testName = m.group(1);
        }
        currentTestName = testName;
        writer.startTest(testName);
        time = System.currentTimeMillis();
    }

    static class ReportWriter {

        XMLStreamWriter writer;

        ReportWriter(String path) {
            try {
                XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(new FileOutputStream(path));
                this.writer = new IndentingXMLStreamWriter(writer);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private void start(String name, Object... attrs) {
            try {
                writer.writeStartElement(name);
                for (int i = 0; i < attrs.length; i+=2) {
                    writer.writeAttribute(attrs[i].toString(), attrs[i+1].toString());
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

        private void endReport() throws Exception {
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        }

        private void failure(Failure failure) {
            element("failure", failure.getMessage());
        }

        private void finishTest(long l) throws Exception {
            element("elapsed", l);
            writer.writeEndElement();
        }

        void reportImage(File f) throws XMLStreamException {
            writer.writeStartElement("img");
            writer.writeAttribute("src", f.getPath().replace("target/", ""));
            writer.writeEndElement();
        }

        private void request(String url, Map<String, ?> queryParams) {
            element("request", url);
        }

        private void response(String response) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                Object readValue = mapper.readValue(response, Object.class);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mapper.defaultPrettyPrintingWriter().writeValue(baos, readValue);
                element("response", baos.toString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        private void startReport() throws Exception {
            writer.writeStartDocument();
            writer.writeProcessingInstruction("xml-stylesheet type=\"text/css\" href=\"report.css\"");
            writer.writeStartElement("report");
            writer.writeAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        }

        private void startTest(String testName) throws Exception {
            writer.writeStartElement("test");
            element("name", testName);
        }
    }

}
