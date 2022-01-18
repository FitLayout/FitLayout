/**
 * PersonsTagger.java
 *
 * Created on 11.11.2011, 14:20:49 by burgetr
 */
package cz.vutbr.fit.layout.text.taggers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;
import cz.vutbr.fit.layout.model.TagOccurrence;
import edu.stanford.nlp.util.Triple;

/**
 * NER-based personal name area tagger. It tags the areas that contain at least the specified number of personal names. 
 * @author burgetr
 */
public class PersonsTagger extends NERTagger
{
    private static final float YES = 0.8f;
    private static final float COULDBE = 0.1f;
    private static final float NO = 0.0f;
    
    /** The expression describing the string that _could_ be a name */
    protected Pattern couldexpr = Pattern.compile("\\p{Lu}[\\p{L}&&[^\\p{Lu}]]+\\s+\\p{Lu}[\\p{L}&&[^\\p{Lu}]]+");
    /** The expression describing the allowed format of the title continuation */
    protected Pattern contexpr = Pattern.compile("[A-Z][A-Za-z]"); 

    private int mincnt;
    
    public PersonsTagger()
    {
        mincnt = 1;
    }
    
    /**
     * Construct a new tagger.
     * @param mincnt the minimal count of the personal names detected in the area necessary for tagging this area.
     */
    public PersonsTagger(int mincnt)
    {
        this.mincnt = mincnt;
    }

    @Override
    public String getId()
    {
        return "FITLayout.Tag.Person";
    }

    @Override
    public String getName()
    {
        return "Persons";
    }

    @Override
    public String getDescription()
    {
        return "NER-based personal name area tagger. It tags the areas that contain at least the specified number of personal names.";
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
        if (node.isLeaf())
        {
            String text = node.getText();
            List<Triple<String,Integer,Integer>> list = getClassifier().classifyToCharacterOffsets(text);
            int cnt = 0;
            for (Triple<String,Integer,Integer> t : list)
            {
                if (t.first().equals("PERSON"))
                    cnt++;
                if (cnt >= mincnt)
                    return YES;
            }
            //no name matched, try matching at least the format
            if (checkAllowedFormat(text))
                return COULDBE;
        }
        return NO;
    }
    
    private boolean checkAllowedFormat(String text)
    {
        int cnt = 0;
        while (couldexpr.matcher(text).find())
        {
            cnt++;
            if (cnt >= mincnt)
                return true;
        }
        return false;
    }

    public boolean allowsContinuation(Area node)
    {
        if (node.isLeaf())
        {
            String text = node.getText().trim();
            if (contexpr.matcher(text).lookingAt()) //must start with something that looks as a name
                return true;
        }
        return false;
    }

    public boolean mayCoexistWith(Tag other)
    {
        return true;
    }
    
    public boolean allowsJoining()
    {
        return true;
    }
    
    public List<TagOccurrence> extract(String src)
    {
        List<TagOccurrence> ret = new ArrayList<>();
        List<Triple<String,Integer,Integer>> list = getClassifier().classifyToCharacterOffsets(src);
        for (Triple<String,Integer,Integer> t : list)
        {
            if (t.first().equals("PERSON"))
                ret.add(new TagOccurrence(src.substring(t.second(), t.third()), t.second(), YES));
        }
        return ret;
    }
    
}
