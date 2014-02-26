package support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.io.IOUtils;

public class Logcat {
    
    static ExecutorService executors = Executors.newCachedThreadPool();
    private static String lastGCLogTimestamp;
    private final Process command;
    private Future<?> future;
    private List<String> lines = new ArrayList<String>();
    private String lastTimeStamp;
    private File dest;
    private String filterToken;

    private Logcat(Process command) {
        this.command = command;
    }
    
    private void start() {
        this.future = executors.submit(new Runnable() {

            @Override
            public void run() {
                if (dest != null) {
                    doCopy();
                } else {
                    doReadLines();
                }
            }
        });
    }

    private void setLastTimeStamp(String lastTimeStamp) {
        this.lastTimeStamp = lastTimeStamp;
    }

    private void setDestination(File dest) {
        this.dest = dest;
    }

    private void setFilterToken(String contains) {
        this.filterToken = contains;
    }

    private void doReadLines() {
        try {
            // readLines will block until complete
            lines = IOUtils.readLines(command.getInputStream());
            if (filterToken != null) {
                ArrayList<String> filtered = new ArrayList<String>();
                for (int i = 0; i < lines.size(); i++) {
                    if (lines.get(i).contains(filterToken)) {
                        filtered.add(lines.get(i));
                    }
                }
                lines = filtered;
            }
        } catch (IOException ex) {
            // stream closed
        }
    }

    private void doCopy() {
        PrintWriter writer;
        try {
            writer = new PrintWriter(dest);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(command.getInputStream()));
            String line;
            // read and discard until the lastTimeStamp
            if (lastTimeStamp != null) {
                while ((line = br.readLine()) != null) {
                    if (line.startsWith(lastTimeStamp)) {
                        break;
                    }
                }
            }
            while ((line = br.readLine()) != null) {
                writer.println(line);
            }
        } catch (IOException ioe) {
            // stream closed
        } finally {
            writer.flush();
            writer.close();
        }
    }

    public List<String> readLines() {
        stopLogging();
        int i = 0;
        if (lastTimeStamp != null) {
            for (; i < lines.size(); i++) {
                if (lines.get(i).startsWith(lastTimeStamp)) {
                    break;
                }
            }
        }
        if (i >= lines.size() || lines.isEmpty() ) {
            return Collections.emptyList();
        }
        // stash this so the next run is faster
        lastGCLogTimestamp = lines.get(i);
        return lines.subList(i + 1, lines.size());
    }
    
    public void stopLogging() {
        // give a change to try to read remaining buffer?
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {

        }
        // this will cause any reading code to die w/ IOException
        command.destroy();
        // let any async process complete
        try {
            future.get();
        } catch (InterruptedException ex) {
            // okay
        } catch (ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String[] buildArgs(String[] extra, String... base) {
        List<String> args = new ArrayList(Arrays.asList(base));
        args.addAll(Arrays.asList(extra));
        return args.toArray(new String[0]);
    }

    private static String getTimeStamp(String logLine) {
        String[] parts = logLine.split(" ");
        return parts[0] + " " + parts[1];
    }

    /** Create a logcat to read from the device to the provided file.
     * This will first check the last log timestamp and then only read from
     * there on. Call `stopLogging` to finish.
     * @param dest
     * @return
     */
    public static Logcat beginLogging(File dest) {
        String[] filterSpec = {"GeodroidServer:D", "org.jeo.nano.NanoServer:D", "*:E"};
        try {
            String[] args = buildArgs(filterSpec,
                    "-v", "time", "-s"
            );
            // grab debug logs of our classes and any error level logs
            Process command = ADB.execute("logcat", args);
            Logcat cat = new Logcat(command);
            cat.setDestination(dest);
            cat.setLastTimeStamp(lastLogMessageTimestamp(filterSpec));
            cat.start();
            return cat;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String lastLogMessageTimestamp(String... filterSpec) {
        try {
            // `-d` argument reads until complete
            // `-v time` provides timestamp output
            String[] args = buildArgs(filterSpec,
                    "-d", "-v", "time", "-s"
            );
            Process proc = ADB.execute("logcat", args);
            List<String> lines = IOUtils.readLines(proc.getInputStream());
            return getTimeStamp(lines.get(lines.size() - 1));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Logcat collectGCStatistics() {
        try {
            String spec = "dalvikvm:D";
            Process command = ADB.execute("logcat", "-v", "time", "-s", spec);
            Logcat cat = new Logcat(command);
            if (lastGCLogTimestamp == null) {
                lastGCLogTimestamp = lastLogMessageTimestamp(spec);
            }
            cat.setFilterToken("GC_CONCURRENT");
            cat.setLastTimeStamp(lastGCLogTimestamp);
            cat.start();
            return cat;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
