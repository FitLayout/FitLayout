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
    public VipsTreeBuilder()
    {
    }
    
    public Area buildAreaTree(VisualStructure vs)
    {
        Area root = createSubtree(vs);
        return root;
    }
    
    private Area createSubtree(VisualStructure vs)
    {
        DefaultArea ret = createArea(vs);
        if (vs.getChildren().size() == 0)
        {
            for (VipsBlock block : vs.getBlockRoots())
            {
                Box box = block.getBox();
                ret.addBox(box);
            }
        }
        else
        {
            for (VisualStructure child : vs.getChildren())
            {
                Area childArea = createSubtree(child);
                ret.appendChild(childArea);
            }
        }
        return ret;
    }
    
    private DefaultArea createArea(VisualStructure vs)
    {
        DefaultArea ret = new DefaultArea(vs.getBounds());
        return ret;
    }
    
}
