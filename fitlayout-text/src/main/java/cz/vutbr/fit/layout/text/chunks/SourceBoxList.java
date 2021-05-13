/**
 * SourceBoxList.java
 *
 * Created on 4. 1. 2019, 10:05:59 by burgetr
 */
package cz.vutbr.fit.layout.text.chunks;

import java.util.ArrayList;
import java.util.Collection;

import cz.vutbr.fit.layout.model.Box;

/**
 * A list of source boxes with additional properties for creating the chunks.
 * 
 * @author burgetr
 */
public class SourceBoxList extends ArrayList<Box>
{
    private static final long serialVersionUID = 1L;
    
    private boolean blockLayout;

    /**
     * Creates a box list with the given layout.
     * @param boxes the source boxes
     * @param blockLayout does the list use the block layout? (boxes below each other?)
     */
    public SourceBoxList(Collection<Box> boxes, boolean blockLayout)
    {
        super(boxes);
        this.blockLayout = blockLayout;
    }

    /**
     * Checks whether the list of boxes uses a block layout (the boxes are below each other).
     * @return
     */
    public boolean isBlockLayout()
    {
        return blockLayout;
    }

    public void setBlockLayout(boolean blockLayout)
    {
        this.blockLayout = blockLayout;
    }

}
