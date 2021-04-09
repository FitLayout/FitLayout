/**
 * TextChunkArea.java
 *
 * Created on 26. 6. 2018, 13:52:54 by burgetr
 */
package cz.vutbr.fit.layout.model;

/**
 * An rectangular area representing a text chunk. The text of the chunk is not given by boxes;
 * it is specified explicitly.
 * 
 * @author burgetr
 */
public interface TextChunk extends ContentRect, Taggable
{
    
    /**
     * Gets the chunk set the chunk belongs to.
     * @return the chunk set or {@code null} when the chunk does not belong to any set.
     */
    public ChunkSet getChunkSet();
    
    /**
     * Gets the complete text of the chunk.
     * @return the text chunk.
     */
    public String getText();

    /**
     * Gets a readable name of the chunk for listing purposes.
     * @return the chunk name.
     */
    public String getName();
    
    /**
     * Sets a readable name of the chunk for listing purposes.
     * @param name the chunk name
     */
    public void setName(String name);
    
    /**
     * Gets the source area the chunk was extracted from.
     * @return the source area
     */
    public Area getSourceArea();

    /**
     * Gets the source box the chunk was extracted from.
     * @return the source box
     */
    public Box getSourceBox();
    
    /**
     * Gets the efficient background color of the chunk.
     * @return the background color or {@code nul} when transparent.
     */
    public Color getEffectiveBackgroundColor();
}
