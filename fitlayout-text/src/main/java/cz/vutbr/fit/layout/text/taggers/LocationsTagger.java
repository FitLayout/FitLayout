/**
 * PersonsTagger.java
 *
 * Created on 11.11.2011, 14:20:49 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;
import edu.stanford.nlp.util.Triple;

/**
 * NER-based location name area tagger. It tags the areas that contain at least the specified number of location names. 
 * @author burgetr
 */
public class LocationsTagger extends NERTagger
{
    private static final float YES = 0.8f;
    private static final float NO = 0.0f;
    
    private int mincnt;
    
    public LocationsTagger()
    {
        mincnt = 1;
    }
    
    /**
     * Construct a new tagger.
     * @param mincnt the minimal count of the location names detected in the area necessary for tagging this area.
     */
    public LocationsTagger(int mincnt)
    {
        this.mincnt = mincnt;
    }

    @Override
    public String getId()
    {
        return "FITLayout.Tag.Location";
    }

    @Override
    public String getName()
    {
        return "Locations";
    }

    @Override
    public String getDescription()
    {
        return "NER-based location name area tagger. It tags the areas that contain at least the specified number of location names";
    }
    
    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
        ret.add(new ParameterInt("mincnt"));
        return ret;
    }
    
    public int getMincnt()
    {
        return mincnt;
    }

    public void setMincnt(int mincnt)
    {
        this.mincnt = mincnt;
    }

    @Override
    public float belongsTo(Area node)
    {
        if (isHomogeneous(node))
        {
            String text = node.getText();
            List<Triple<String,Integer,Integer>> list = getClassifier().classifyToCharacterOffsets(text);
            int cnt = 0;
            for (Triple<String,Integer,Integer> t : list)
            {
                if (t.first().equals("LOCATION"))
                    cnt++;
                if (cnt >= mincnt)
                    return YES;
            }
        }
        return NO;
    }
    
    @Override
    public boolean allowsContinuation(Area node)
    {
    	return false;
    }

    @Override
    public boolean allowsJoining()
    {
        return true;
    }

    @Override
    public boolean mayCoexistWith(Tag other)
    {
        return true;
    }
    
    @Override
    public List<TagOccurrence> extract(String src)
    {
        List<TagOccurrence> ret = new ArrayList<>();
        List<Triple<String,Integer,Integer>> list = getClassifier().classifyToCharacterOffsets(src);
        for (Triple<String,Integer,Integer> t : list)
        {
            if (t.first().equals("LOCATION"))
                ret.add(new TagOccurrence(src.substring(t.second(), t.third()), t.second(), YES));
        }
        return ret;
    }
    
}
