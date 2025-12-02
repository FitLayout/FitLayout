/**
 * TestApp.java
 *
 * Created on 2. 12. 2025, 21:15:44 by burgetr
 */

package cz.vutbr.fit.layout.text.wiki;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author burgetr
 */
public class TestApp
{
    public static void main(String[] args) throws Exception
    {

        String modelPath = "/onnx/model.onnx";

        // Example training samples
        Set<String> movieNames = Set.of("Inception", "Matrix", "Interstellar",
                "Avatar");
        Set<String> actorNames = Set.of("Leonardo DiCaprio", "Keanu Reeves",
                "Tom Hanks");

        String collectionMovies = "movies";
        String collectionActors = "actors";

        try (EmbeddingService emb = new EmbeddingService(modelPath))
        {
            QdrantHttpClient q = new QdrantHttpClient("http://localhost:6333");

            // Create movie collection
            float[] v = emb.embed(movieNames.iterator().next());
            float[] exampleVec = EmbeddingService.normalize(v);

            q.createCollection(collectionMovies, exampleVec.length);
            q.createCollection(collectionActors, exampleVec.length);

            // Insert movies
            List<Map<String, Object>> moviePoints = new ArrayList<>();
            int id = 1;
            for (String m : movieNames)
            {
                float[] vec = EmbeddingService.normalize(emb.embed(m));
                moviePoints.add(
                        Map.of("id", "movie-" + id++, "vector", toList(vec),
                                "payload", Map.of("type", "movie", "name", m)));
            }
            q.upsert(collectionMovies, moviePoints);

            // Insert actors
            List<Map<String, Object>> actorPoints = new ArrayList<>();
            id = 1;
            for (String a : actorNames)
            {
                float[] vec = EmbeddingService.normalize(emb.embed(a));
                actorPoints.add(
                        Map.of("id", "actor-" + id++, "vector", toList(vec),
                                "payload", Map.of("type", "actor", "name", a)));
            }
            q.upsert(collectionActors, actorPoints);

            // Now classify a new name
            String name = "Tom Cruise";
            float[] query = EmbeddingService.normalize(emb.embed(name));

            String resultMovies = q.search(collectionMovies, query, 3);
            String resultActors = q.search(collectionActors, query, 3);

            System.out.println("Movies score: " + score(resultMovies));
            System.out.println("Actors score: " + score(resultActors));
        }
    }

    private static List<Float> toList(float[] a)
    {
        List<Float> list = new ArrayList<>(a.length);
        for (float f : a)
            list.add(f);
        return list;
    }

    // very naive probability from search response
    private static double score(String jsonSearch)
    {
        if (jsonSearch.contains("score"))
        {
            // extract first score number
            String[] p = jsonSearch.split("score");
            if (p.length > 1)
            {
                String s = p[1].replaceAll("[^0-9.]+", "");
                try
                {
                    return Double.parseDouble(s);
                } catch (Exception ignored)
                {
                }
            }
        }
        return 0.0;
    }
}
