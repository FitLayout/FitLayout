package cz.vutbr.fit.layout.text.wiki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * A client for an external command-line application that communicates over standard I/O.
 * The client sends string requests to the application's standard input and expects
 * JSON responses on its standard output. Each request and response should be on a single line.
 * 
 * @author burgetr
 */
public class SSHClient {
    private static Logger log = LoggerFactory.getLogger(SSHClient.class);

    private String host;
    private String scriptPath;
    
    /**
     * Creates a new client for running remote commands.
     * 
     * @param host The SSH host.
     * @param scriptPath The path to the script to execute on the remote host.
     */
    public SSHClient(String host, String scriptPath) {
        this.host = host;
        this.scriptPath = scriptPath;
    }
    
    /**
     * Executes a query on the remote host and parses the JSON response.
     * 
     * @param query The query string to pass as an argument to the remote script.
     * @return The parsed JSON response as a {@link JsonElement}.
     * @throws IOException if there is an I/O error or the process fails.
     * @throws InterruptedException if the thread is interrupted while waiting for the process.
     * @throws JsonSyntaxException if the response is not valid JSON.
     */
    public JsonElement runQuery(String query) throws IOException, InterruptedException, JsonSyntaxException {
        String cmd = scriptPath + " \"" + query + "\"";
        String[] command = new String[]{"ssh", "-q", host, cmd};
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        String jsonLine;
        try (BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            jsonLine = stdout.readLine();
        }

        try (BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String errorOutput = stderr.lines().collect(Collectors.joining("\n"));
            if (!errorOutput.isEmpty()) {
                log.error("SSH stderr: {}", errorOutput);
            }
        }

        boolean finished = process.waitFor(15, TimeUnit.SECONDS);
        if (!finished) {
            log.warn("Process did not terminate in time, forcing shutdown.");
            process.destroyForcibly();
        }

        if (jsonLine == null) {
            throw new IOException("No output received from process.");
        }

        Gson gson = new Gson();
        return gson.fromJson(jsonLine, JsonElement.class);
    }

}
