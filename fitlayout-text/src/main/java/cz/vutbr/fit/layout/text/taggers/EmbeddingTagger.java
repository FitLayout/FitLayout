/**
 * EmbeddingTagger.java
 *
 * Created on 6. 12. 2025, 15:17:05 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import cz.vutbr.fit.layout.api.AreaConcatenator;
import cz.vutbr.fit.layout.api.MultiTagger;
import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;
import cz.vutbr.fit.layout.text.TextFlowConcatenator;
import cz.vutbr.fit.layout.text.wiki.SSHClient;

/**
 * 
 * @author burgetr
 */
public class EmbeddingTagger extends BaseTagger implements MultiTagger
{
    private static Logger log = LoggerFactory.getLogger(EmbeddingTagger.class);
    
    public static final String SSH_HOST_PROPERTY = "fitlayout.embeddings.sshHost";
    public static final String SSH_SCRIPT_PATH_PROPERTY = "fitlayout.embeddings.sshScriptPath";
            
    private int minLength = 3;
    private int maxLength = 50;
    private float minScore = 0.4f;
    private boolean useMax = false; // use only the maximum score

    /** The concatenator used for converting areas to text */
    private AreaConcatenator concat; 
    private SSHClient sshClient;
    /** A cache of relevances for each area tree and area ID */
    private Map<AreaTree, Map<Integer, Map<String, Float>>> relevances;
    

    public EmbeddingTagger()
    {
        relevances = new HashMap<>();
        concat = new TextFlowConcatenator();
        String sshHost = System.getProperty(SSH_HOST_PROPERTY);
        String sshScriptPath = System.getProperty(SSH_SCRIPT_PATH_PROPERTY);
        if (sshHost != null && sshScriptPath != null)
            sshClient = new SSHClient(sshHost, sshScriptPath);
        else
            log.error("The {} and {} properties are not set, embedding tagger will not assign any tags", SSH_HOST_PROPERTY, SSH_SCRIPT_PATH_PROPERTY);
    }
    
    public EmbeddingTagger(int minLength, int maxLength, float minScore)
    {
        this();
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.minScore = minScore;
    }
    
    @Override
    public String getId()
    {
        return "FITLayout.Tag.Embedding";
    }

    @Override
    public String getName()
    {
        return "Use embeddings for tag detection";
    }

    @Override
    public String getDescription()
    {
        return "Matches area text with existing embeddings";
    }
    
    //==========================================================================================
    
    @Override
    public List<Parameter> defineParams()
    {
        return List.of(
                new ParameterInt("minLength", "Minimal text length to process", 0, 10000),
                new ParameterInt("maxLength", "Maximal text length to process", 0, 10000),
                new ParameterFloat("minScore", "Minimal similarity score", 0.0f, 1.0f),
                new ParameterBoolean("useMax", "Use only the maximum similarity score"));
    }

    public int getMinLength()
    {
        return minLength;
    }

    public void setMinLength(int minLength)
    {
        this.minLength = minLength;
    }

    public int getMaxLength()
    {
        return maxLength;
    }

    public void setMaxLength(int maxLength)
    {
        this.maxLength = maxLength;
    }
    
    public float getMinScore()
    {
        return minScore;
    }

    public void setMinScore(float minScore)
    {
        this.minScore = minScore;
    }

    public boolean getUseMax()
    {
        return useMax;
    }

    public void setUseMax(boolean useMax)
    {
        this.useMax = useMax;
    }

    public AreaConcatenator getConcatenator()
    {
        return concat;
    }

    //==========================================================================================
    
    @Override
    public void startSubtree(Area root)
    {
        if (root.getAreaTree() != null)
        {
            var subtreeRelevances = getRelevancesForSubtree(root);
            relevances.put(root.getAreaTree(), subtreeRelevances);
        }
        else
            log.error("Cannot assign tags to area without area tree: {}", root);
    }

    @Override
    public void finishSubtree(Area root)
    {
        if (root.getAreaTree() != null)
        {
            relevances.remove(root.getAreaTree());
        }
    }
    
    //==========================================================================================
    
    @Override
    public float belongsTo(Area node)
    {
        String text = getText(node);
        if (text.length() >= minLength && text.length() <= maxLength)
        {
            if (node.getAreaTree() == null)
            {
                // no area tree -- classify the node separately (inefficient)
                float score = getScore(text);
                if (score >= minScore)
                    return score;
            }
            else
            {
                var subtreeRelevances = relevances.get(node.getAreaTree());
                if (subtreeRelevances != null)
                {
                    var areaRelevances = subtreeRelevances.get(node.getId());
                    if (areaRelevances != null)
                    {
                        float max = 0.0f;
                        for (var entry : areaRelevances.entrySet())
                            max = Math.max(max, entry.getValue());
                        if (max >= minScore)
                            return max;
                    }
                }
                
            }
        }
        return 0.0f;
    }
    
    @Override
    public Map<String, Float> getRelevances(Area node)
    {
        final String text = getText(node);
        if (text.length() >= minLength && text.length() <= maxLength)
        {
            if (node.getAreaTree() == null)
            {
                // no area tree -- classify the node separately (inefficient)
                return getScores(text, minScore);
            }
            else
            {
                var subtreeRelevances = relevances.get(node.getAreaTree());
                if (subtreeRelevances != null)
                {
                    var areaRelevances = subtreeRelevances.get(node.getId());
                    if (areaRelevances != null)
                    {
                        if (useMax)
                        {
                            // return the highest score only
                            return areaRelevances.entrySet().stream()
                                    .max(Map.Entry.comparingByValue())
                                    .filter(entry -> entry.getValue() >= minScore)
                                    .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                                    .orElse(Collections.emptyMap());
                        }
                        else
                        {
                            // filter out results below the minimum score
                            return areaRelevances.entrySet()
                                   .stream()
                                   .filter(entry -> entry.getValue() >= minScore)
                                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        }
                    }
                }
                return Collections.emptyMap();
            }
        }
        else
        {
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean allowsContinuation(Area node)
    {
        return false;
    }

    @Override
    public boolean allowsJoining()
    {
        return false;
    }

    @Override
    public boolean mayCoexistWith(Tag other)
    {
        return true;
    }

    @Override
    public List<TagOccurrence> extract(String src)
    {
        return List.of(new TagOccurrence(src, 0, 1.0f));
    }

    /**
     * Obtains the maximal score for the text using the SSH client.
     * @param text
     * @return
     */
    protected float getScore(String text)
    {
        if (sshClient != null)
        {
            try
            {
                var embedData = sshClient.runQuery(text);
                var scores = embedData.getAsJsonObject().get("scores");
                float max = 0.0f;
                for (var entry : scores.getAsJsonObject().entrySet()) {
                    float score = entry.getValue().getAsFloat();
                    if (score > max) max = score;
                }
                return max;
            } catch (JsonSyntaxException | IOException | InterruptedException e) {
                log.error("Failed to run embedder query: {}", e.getMessage());
                return 0.0f;
            }
        } else {
            return 0.0f;
        }
    }
    
    /**
     * Obtains the scores for the text.
     * @param text
     * @param minScore
     * @return
     */
    protected Map<String, Float> getScores(String text, float minScore)
    {
        if (sshClient != null)
        {
            try
            {
                var embedData = sshClient.runQuery(text);
                var scores = embedData.getAsJsonObject().get("scores");
                Map<String, Float> ret = new HashMap<>();
                String maxGroup = null;
                float maxScore = 0.0f;
                for (var entry : scores.getAsJsonObject().entrySet()) {
                    String group = entry.getKey();
                    float score = entry.getValue().getAsFloat();
                    if (score >= minScore)
                    {
                        ret.put(group, score);
                        if (score > maxScore) 
                        {
                            maxGroup = group;
                            maxScore = score;
                        }
                    }
                }
                if (useMax && maxGroup != null)
                    return Map.of(maxGroup, maxScore);
                else
                    return ret;
            } catch (JsonSyntaxException | IOException | InterruptedException e) {
                log.error("Failed to run embedder query: {}", e.getMessage());
                return Collections.emptyMap();
            }
        } else {
            return Collections.emptyMap();
        }
    }
    
    protected Map<Integer, Map<String, Float>> getRelevancesForSubtree(Area root)
    {
        if (sshClient != null)
        {
            List<SSHClient.PredictQuery> queries = new ArrayList<>();
            createPredictQueriesForSubtree(root, queries);
            try {
                var predictResult = sshClient.runMultiQuery(queries);
                if (predictResult.getResults() != null) 
                {
                    // Convert result IDs back to integers
                    Map<Integer, Map<String, Float>> ret = new HashMap<>();
                    for (var result : predictResult.getResults()) 
                        ret.put(Integer.parseInt(result.getId()), result.getScores());
                    return ret;
                }
                else
                {
                    log.error("Empty embedder results");
                    return Collections.emptyMap();
                }
            } catch (JsonSyntaxException | IOException | InterruptedException e) {
                log.error("Failed to run embedder query: {}", e.getMessage());
                return Collections.emptyMap();
            }
        }
        else
        {
            return Collections.emptyMap();
        }
    }
    
    protected void createPredictQueriesForSubtree(Area root, List<SSHClient.PredictQuery> dest)
    {
        String text = getText(root);
        if (text.length() >= minLength && text.length() <= maxLength)
        {
            String id = String.valueOf(root.getId());
            dest.add(new SSHClient.PredictQuery(id, text));
        }
        
        for (Area child : root.getChildren())
            createPredictQueriesForSubtree(child, dest);
    }
    
    protected String getText(Area node)
    {
        String s = node.getText(getConcatenator()).trim();
        s = s.replaceAll("^[\\\"\\p{Pi}]+", ""); //remove starting quotes and other unicode "initial punctuation"
        s = s.replaceAll("[\\\"\\p{Pf}]+$", ""); //remove trailing quotes and other unicode "final punctuation"
        return s;
    }

}
