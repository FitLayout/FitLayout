/**
 * Tag.java
 *
 * Created on 21.11.2011, 13:47:31 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import cz.vutbr.fit.layout.api.Tagger;
import cz.vutbr.fit.layout.impl.DefaultTag;
import cz.vutbr.fit.layout.model.Area;

/**
 * A tag that can be assigned to the visual areas obtained using a text
 * classification by a particular tagger.
 * 
 * @author burgetr
 */
public class TextTag extends DefaultTag
{
    private Tagger source;
    
    public TextTag(String value, Tagger source)
    {
        super(value);
        this.source = source;
        setType("FitLayout.TextTag");
    }

    public Tagger getSource()
    {
        return source;
    }

    public boolean allowsJoining()
    {
        if (source != null)
            return source.allowsJoining();
        else
            return false;
    }
    
    public boolean allowsContinutation(Area node)
    {
        if (source != null)
            return source.allowsContinuation(node);
        else
            return false;
    }
    
}
