/**
 * PresentationHint.java
 *
 * Created on 29. 6. 2018, 15:33:48 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.TextChunk;
import cz.vutbr.fit.layout.text.tag.TagOccurrence;


/**
 * A hint that influences the chunk extraction in different points of the chunk extraction phases
 * as defined by the {@link PresentationBasedChunksSource}.
 * 
 * @author burgetr
 */
public interface PresentationHint
{
    
    /**
     * Does the hint imply that the area has block layout?
     * @return
     */
    public boolean impliesBlock();
    
    /**
     * Does the hint imply that the area has inline layout?
     * @return
     */
    public boolean impliesInline();
    
    /**
     * The probability of the hint based on the analyzed page.
     * @return the hint support 0.0 .. 1.0
     */
    public float getSupport();
    
    /**
     * Extracts boxes from the given area and/or modifies the already existing list of boxes (preprocessing).
     * If some additional areas were processed, they should be added to the 'processed' set.
     * @param a
     * @param current
     * @param processed
     * @return
     */
    public SourceBoxList extractBoxes(Area a, SourceBoxList current, Set<Area> processed);

    /**
     * Applies the hint to a list of occurences extracted from the given box text (postprocessing a list of
     * occurences)
     * @param boxText the source box text
     * @param occurrences the current list of occurrences
     * @return the new list of occurrences
     */
    public List<TagOccurrence> processOccurrences(BoxText boxText, List<TagOccurrence> occurrences);
    
    /**
     * Applies the hint to the list of chunks for the given area (postprocessing a list for a given area)
     * @param src
     * @param chunks
     * @return
     */
    public List<TextChunk> processChunks(Area src, List<TextChunk> chunks);
    
    /**
     * Applies the hint to the current list of chunks (postprocessing the whole list)
     * @param chunks the list of chunks (typically for a given tag)
     * @return
     */
    public List<TextChunk> postprocessChunks(List<TextChunk> chunks);

}
