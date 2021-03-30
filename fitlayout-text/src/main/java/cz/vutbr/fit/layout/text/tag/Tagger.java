/**
 * Tagger.java
 *
 * Created on 11.11.2011, 11:22:29 by burgetr
 */
package cz.vutbr.fit.layout.text.tag;

import java.util.List;

import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.Service;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Tag;


/**
 * A generic tagger that is able to assign tags to areas.
 * 
 * @author burgetr
 */
public interface Tagger extends Service, ParametrizedOperation
{

    /**
     * Obtains the tag that this tagger assigns to the areas.
     * @return the tag string
     */
    public TextTag getTag();

    /**
     * Checks whether the area may be tagged with the tag. This method does not actually assign the tag to the area.
     * @param node The examined area node.
     * @return the relevance of the assignment (0.0 = not assigned, 0.1 = hopefully possible, >0.5 quite possible, 1.0 absolutely sure)
     */
    public float belongsTo(Area node);
    
    /**
     * Checks whether the area may be a continuation of a previously started area tagged with this tag.
     * @param node The examined area node.
     * @return <code>true</code> if the area may be a continuation of a tagged area
     */
    public boolean allowsContinuation(Area node);
    
    /**
     * Checks whether the tag may be used for joining the areas in the visual area tree.
     * @return <code>true</code> if the tag may be used for joining.
     */
    public boolean allowsJoining();
    
    /**
     * Check if the area tagged with this tag may be tagged with another tag. If not, this tag won't be used
     * for the areas already tagged with another tag.
     */
    public boolean mayCoexistWith(Tag other);
    
    /**
     * Extracts the parts of a source string that correspond to this tag.
     * @param src The source string. 
     * @return A list of extracted strings.
     */
    public List<TagOccurrence> extract(String src);
    
}
