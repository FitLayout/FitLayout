/**
 * QdrantHttpClient.java
 *
 * Created on 2. 12. 2025, 21:13:52 by burgetr
 */

package cz.vutbr.fit.layout.text.wiki;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 *
 * @author burgetr
 */
public class QdrantHttpClient
{
    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String baseUrl;

    public QdrantHttpClient(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    public void createCollection(String name, int vectorSize) throws Exception
    {
        Map<String, Object> payload = Map.of("vectors",
                Map.of("size", vectorSize, "distance", "Cosine"));

        String json = gson.toJson(payload);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collections/" + name))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString());
        System.out.println(resp.body());
    }

    public void upsert(String collection, List<Map<String, Object>> points)
            throws Exception
    {
        Map<String, Object> payload = Map.of("points", points);
        String json = gson.toJson(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collections/" + collection
                        + "/points?wait=true"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString());
        System.out.println(resp.body());
    }

    public String search(String collection, float[] vector, int top)
            throws Exception
    {
        Map<String, Object> payload = Map.of("vector", vector, "top", top,
                "with_payload", true);
        String json = gson.toJson(payload);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/collections/" + collection
                        + "/points/search"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }
}
