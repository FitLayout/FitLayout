/**
 * EmbeddingService.java
 *
 * Created on 2. 12. 2025, 21:07:20 by burgetr
 */

package cz.vutbr.fit.layout.text.wiki;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import java.nio.file.Paths;

public class EmbeddingService implements AutoCloseable
{

    private final ZooModel<String, float[]> model;
    private final Predictor<String, float[]> predictor;

    public EmbeddingService(String onnxModelPath) throws Exception
    {
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelPath(Paths.get(onnxModelPath)).build();

        model = ModelZoo.loadModel(criteria);
        predictor = model.newPredictor();
    }

    public float[] embed(String text) throws TranslateException
    {
        return predictor.predict(text);
    }

    public static float[] normalize(float[] vector)
    {
        double sum = 0.0;
        for (float v : vector)
            sum += v * v;
        double norm = Math.sqrt(sum);
        float[] out = new float[vector.length];
        for (int i = 0; i < vector.length; i++)
            out[i] = (float) (vector[i] / norm);
        return out;
    }

    @Override
    public void close() throws Exception
    {
        predictor.close();
        model.close();
    }
}
