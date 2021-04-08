/**
 * ChunkSet.java
 *
 * Created on 8. 4. 2021, 13:28:54 by burgetr
 */
package cz.vutbr.fit.layout.model;

import java.util.Set;

/**
 * A set of text chunks extracted from a page.
 *  
 * @author burgetr
 */
public interface ChunkSet extends Artifact
{

    public Set<TextChunk> getTextChunks();
    
    public AreaTopology getTopology();
    
}
