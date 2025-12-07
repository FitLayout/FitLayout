/**
 * MultiTagger.java
 *
 * Created on 6. 12. 2025, 20:50:45 by burgetr
 */
package cz.vutbr.fit.layout.api;

import java.util.Map;

import cz.vutbr.fit.layout.model.Area;

/**
 * A tagger that can assign multiple tags to an area. The tags are discriminated by string result
 * that can be later mapped to a specific tag depending on the tagging configuration.
 * 
 * @author burgetr
 */
public interface MultiTagger extends Tagger
{

    /**
     * Provides a map of tag discriminators to their relevance for the given area.
     * @param node The area to which the tags will be assigned.
     * @return A map of tag discriminators to their relevance.
     */
    public Map<String, Float> getRelevances(Area node);
    
}
