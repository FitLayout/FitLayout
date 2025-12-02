package cz.vutbr.fit.layout.text.wiki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A source of examples for a given Wikidata class. It downloads all the surface
 * forms (text representations) of its instances (e.g. "George Bush",
 * "Douglas Adams" for Q5 "human").
 * 
 * @author burgetr
 */
public class WikiExampleSource {

    private static final Logger log = LoggerFactory.getLogger(WikiExampleSource.class);
    private static final String WIKIDATA_SPARQL_ENDPOINT = "https://query.wikidata.org/sparql";
    private static final int MAX_RESULTS = 2000;

    private final String classId;
    private Set<String> surfaceForms;

    /**
     * Creates a new source for a given Wikidata class ID.
     * 
     * @param classId The Wikidata class ID, e.g., "Q5" for "human".
     */
    public WikiExampleSource(String classId) {
        this.classId = classId;
        this.surfaceForms = null;
    }

    /**
     * Retrieves the surface forms for the instances of the configured Wikidata class.
     * The forms are downloaded on the first call and cached for subsequent calls.
     * 
     * @return A set of surface forms, or an empty set if an error occurs.
     */
    public Set<String> getSurfaceForms() {
        if (surfaceForms == null) {
            try {
                downloadSurfaceForms();
            } catch (IOException e) {
                log.error("Failed to download surface forms for class {}: {}", classId, e.getMessage());
                surfaceForms = new HashSet<>(); // return empty set on failure
            }
        }
        return surfaceForms;
    }

    private void downloadSurfaceForms() throws IOException {
        log.info("Downloading surface forms for Wikidata class: {}", classId);
        surfaceForms = new HashSet<>();
        String sparqlQuery = "SELECT ?item ?label WHERE {\n"
                + "  ?item wdt:P31 wd:" + classId + " ."
                + "  OPTIONAL { ?item rdfs:label ?lab_cs  FILTER (lang(?lab_cs)=\"cs\") } "
                + "  OPTIONAL { ?item rdfs:label ?lab_en  FILTER (lang(?lab_en)=\"en\") } "
                + "  OPTIONAL { ?item rdfs:label ?lab_mul FILTER (lang(?lab_mul)=\"mul\") } "
                + "  BIND( "
                + "    COALESCE(?lab_cs, ?lab_en, ?lab_mul) AS ?label\n"
                + "  ) "
                + "} LIMIT " + MAX_RESULTS;

        try
        {
            URL url = new URI(WIKIDATA_SPARQL_ENDPOINT + "?query=" + URLEncoder.encode(sparqlQuery, StandardCharsets.UTF_8.name())).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/sparql-results+json");

            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP request failed with response code: " + conn.getResponseCode());
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                JsonObject result = JsonParser.parseReader(in).getAsJsonObject();
                System.err.println("Found " + result.getAsJsonObject("results").get("bindings").getAsJsonArray().size() + " bindings" + " for class " + classId);
                JsonArray bindings = result.getAsJsonObject("results").getAsJsonArray("bindings");

                for (JsonElement binding : bindings) {
                    JsonObject b = binding.getAsJsonObject();
                    if (b.has("label")) {
                        surfaceForms.add(b.getAsJsonObject("label").get("value").getAsString());
                    }
                }
            }
            log.info("Found {} unique surface forms for class {}", surfaceForms.size(), classId);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Main method for demonstration and testing purposes.
     * 
     * Q5: "human"
     * Q7725634: "literally work"
     * 
     * @param args Command line arguments. Expects one argument: the Wikidata class ID.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java cz.vutbr.fit.extract.WikiExampleSource <WikiDataClassID>");
            System.out.println("Example: java cz.vutbr.fit.extract.WikiExampleSource Q5");
            return;
        }
        String classId = args[0];
        WikiExampleSource exampleSource = new WikiExampleSource(classId);
        Set<String> forms = exampleSource.getSurfaceForms();
        System.out.println("Surface forms for " + classId + ":");
        forms.stream().limit(20).forEach(System.out::println);
        System.out.println("... and " + (forms.size() - 20) + " more.");
    }
}
