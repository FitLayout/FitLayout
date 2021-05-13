/**
 * AreaListSource.java
 *
 * Created on 9. 3. 2018, 23:27:32 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.List;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.TextChunk;

/**
 * An abstract base of a source of text chunks.
 * 
 * @author burgetr
 */
public abstract class ChunksSource
{
    private Area root;
    
    public ChunksSource(Area root)
    {
        this.root = root;
    }
    
    public Area getRoot()
    {
        return root;
    }
    
    /**
     * Extracts a list of chunks from the source area tree.
     * 
     * @return
     */
    public abstract List<TextChunk> getTextChunks();

}
