/**
 * VipsTreeBuilder.java
 *
 * Created on 19. 11. 2020, 13:05:15 by burgetr
 */
package cz.vutbr.fit.layout.vips.impl;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
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
    
    public Area buildAreaTree(AreaTree atree, VisualArea vs)
    {
        Area root = createSubtree(atree, vs);
        return root;
    }
    
    private Area createSubtree(AreaTree atree, VisualArea vs)
    {
        Area ret = createArea(atree, vs);
        if (vs.getChildren().size() == 0)
        {
            for (VisualBlock block : vs.getBlockRoots())
            {
                Box box = block.getBox();
                ret.addBox(box);
            }
        }
        else
        {
            if (vs.getDoC() <= pDoC)
            {
                for (VisualArea child : vs.getChildren())
                {
                        Area childArea = createSubtree(atree, child);
                        ret.appendChild(childArea);
                }
            }
        }
        return ret;
    }
    
    private Area createArea(AreaTree atree, VisualArea vs)
    {
        Area ret = atree.createArea(vs.getBounds());
        ret.setName("VB (" + vs.getDoC() + ")");
        return ret;
    }
    
}
