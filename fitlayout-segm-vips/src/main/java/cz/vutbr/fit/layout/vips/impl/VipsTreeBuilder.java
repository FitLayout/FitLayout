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
    
    public Area buildAreaTree(VisualArea vs)
    {
        Area root = createSubtree(vs);
        return root;
    }
    
    private Area createSubtree(VisualArea vs)
    {
        DefaultArea ret = createArea(vs);
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
                        Area childArea = createSubtree(child);
                        ret.appendChild(childArea);
                }
            }
        }
        return ret;
    }
    
    private DefaultArea createArea(VisualArea vs)
    {
        DefaultArea ret = new DefaultArea(vs.getBounds());
        ret.setName("VB (" + vs.getDoC() + ")");
        return ret;
    }
    
}
