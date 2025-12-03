package cz.vutbr.fit.layout.text.wiki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.TimeUnit;

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
public class CLIClient implements Closeable {
    private static Logger log = LoggerFactory.getLogger(CLIClient.class);

    private Process process;
    private BufferedWriter stdin;
    private BufferedReader stdout;
    private Thread stderrReader;

    /**
     * Creates a new client and starts the external application.
     * 
     * @param command The command and its arguments to execute.
     * @throws IOException if the process fails to start.
     */
    public CLIClient(String... command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        process = pb.start();
        stdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // Asynchronously read stderr to prevent blocking and log any errors
        stderrReader = new Thread(() -> {
            try (BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = stderr.readLine()) != null) {
                    log.error("CLI stderr: {}", line);
                }
            } catch (IOException e) {
                // Stream closed, thread can exit.
            }
        });
        stderrReader.setDaemon(true);
        stderrReader.start();
    }

    /**
     * Sends a string to the application and waits for a JSON response.
     * 
     * @param input The string to send to the application's stdin. A newline is appended automatically.
     * @return The parsed JSON response as a {@link JsonElement}.
     * @throws IOException if there is an I/O error or the process has terminated.
     * @throws JsonSyntaxException if the response is not valid JSON.
     */
    public synchronized JsonElement sendAndReceive(String input) throws IOException, JsonSyntaxException {
        if (!process.isAlive()) {
            throw new IOException("Process has terminated unexpectedly.");
        }

        stdin.write(input);
        stdin.newLine();
        stdin.flush();

        String jsonLine = stdout.readLine();
        if (jsonLine == null) {
            throw new IOException("End of stream reached, process may have terminated.");
        }

        Gson gson = new Gson();
        return gson.fromJson(jsonLine, JsonElement.class);
    }

    @Override
    public void close() throws IOException {
        if (process != null) {
            try {
                if (stdin != null) stdin.close();
                if (stdout != null) stdout.close();

                // Wait for the process to terminate gracefully
                if (!process.waitFor(500, TimeUnit.MILLISECONDS)) {
                    log.warn("Process did not terminate gracefully, forcing shutdown.");
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for process to exit, forcing shutdown.");
                process.destroyForcibly();
                Thread.currentThread().interrupt();
            } finally {
                if (stderrReader != null && stderrReader.isAlive()) {
                    stderrReader.interrupt();
                }
                process = null;
            }
        }
    }
}
