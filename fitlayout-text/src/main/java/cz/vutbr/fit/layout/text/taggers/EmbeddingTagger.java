/**
 * EmbeddingTagger.java
 *
 * Created on 6. 12. 2025, 15:17:05 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import cz.vutbr.fit.layout.api.AreaConcatenator;
import cz.vutbr.fit.layout.api.MultiTagger;
import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.model.Area;
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

    /** The concatenator used for converting areas to text */
    private AreaConcatenator concat; 
    
    private SSHClient sshClient;
    

    public EmbeddingTagger()
    {
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
                new ParameterInt("minLength", 0, 10000),
                new ParameterInt("maxLength", 0, 10000),
                new ParameterFloat("minScore", 0.0f, 1.0f));
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

    public AreaConcatenator getConcatenator()
    {
        return concat;
    }

    //==========================================================================================
    
    @Override
    public void startSubtree(Area root)
    {
        // TODO Auto-generated method stub
        super.startSubtree(root);
    }

    @Override
    public void finishSubtree(Area root)
    {
        // TODO Auto-generated method stub
        super.finishSubtree(root);
    }
    
    //==========================================================================================
    
    @Override
    public float belongsTo(Area node)
    {
        String text = getText(node);
        if (text.length() >= minLength && text.length() <= maxLength)
        {
            float score = getScore(text);
            if (score >= minScore)
            {
                return score;
            }
        }
        return 0.0f;
    }
    
    @Override
    public Map<String, Float> getRelevances(Area node)
    {
        String text = getText(node);
        if (text.length() >= minLength && text.length() <= maxLength)
        {
            return getScores(text, minScore);
        }
        return Collections.emptyMap();
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
     * Obtains the maximal score for the text.
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
                for (var entry : scores.getAsJsonObject().entrySet()) {
                    String group = entry.getKey();
                    float score = entry.getValue().getAsFloat();
                    if (score >= minScore)
                        ret.put(group, score);
                }
                return ret;
            } catch (JsonSyntaxException | IOException | InterruptedException e) {
                log.error("Failed to run embedder query: {}", e.getMessage());
                return Collections.emptyMap();
            }
        } else {
            return Collections.emptyMap();
        }
    }
    
    protected void addPredictQueriesForSubtree(Area root, List<SSHClient.PredictQuery> dest)
    {
        String text = getText(root);
        if (text.length() >= minLength && text.length() <= maxLength)
        {
            String id = String.valueOf(root.getId());
            dest.add(new SSHClient.PredictQuery(id, text));
        }
        
        for (Area child : root.getChildren())
            addPredictQueriesForSubtree(child, dest);
    }
    
    protected String getText(Area node)
    {
        String s = node.getText(getConcatenator()).trim();
        s = s.replaceAll("^[\\\"\\p{Pi}]+", ""); //remove starting quotes and other unicode "initial punctuation"
        s = s.replaceAll("[\\\"\\p{Pf}]+$", ""); //remove trailing quotes and other unicode "final punctuation"
        return s;
    }

}
