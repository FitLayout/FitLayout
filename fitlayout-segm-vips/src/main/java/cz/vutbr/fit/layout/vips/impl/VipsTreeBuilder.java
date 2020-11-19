/**
 * VipsTreeBuilder.java
 *
 * Created on 19. 11. 2020, 13:05:15 by burgetr
 */
package cz.vutbr.fit.layout.vips.impl;

import cz.vutbr.fit.layout.impl.DefaultArea;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;

/**
 * A resulting tree produced by VIPS.
 * 
 * @author burgetr
 */
public class VipsTreeBuilder
{
    private int pDoC;

    public VipsTreeBuilder(int pDoC)
    {
        this.pDoC = pDoC;
    }
    
    public Area buildAreaTree(VisualStructure vs)
    {
        Area root = createSubtree(vs);
        return root;
    }
    
    private Area createSubtree(VisualStructure vs)
    {
        Area ret = createArea(vs);
        if (pDoC >= vs.getDoC())
        {
            // continue segmenting
            if (vs.getChildrenVisualStructures().size() == 0)
            {
                for (VipsBlock block : vs.getNestedBlocks())
                {
                    Box box = block.getBox();
                    ret.addBox(box);
                }
            }

            for (VisualStructure child : vs.getChildrenVisualStructures())
            {
                Area childArea = createSubtree(child);
                ret.appendChild(childArea);
            }
        }
        else
        {
            // "stop" segmentation
            if (vs.getNestedBlocks().size() > 0)
            {
                for (VipsBlock block : vs.getNestedBlocks())
                {
                    Box box = block.getBox();
                    ret.addBox(box);
                }
            }
        }
        return ret;
    }
    
    private Area createArea(VisualStructure vs)
    {
        DefaultArea ret = new DefaultArea(vs.getX(), vs.getY(), vs.getX() + vs.getWidth() - 1, vs.getY() + vs.getHeight() - 1);
        
        return ret;
    }
    
}
