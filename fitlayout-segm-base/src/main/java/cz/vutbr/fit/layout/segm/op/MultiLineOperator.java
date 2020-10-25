/**
 * MultiLineOperator.java
 *
 * Created on 28. 2. 2015, 23:10:59 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTopology;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.segm.AreaImpl;
import cz.vutbr.fit.layout.segm.AreaStyle;
import cz.vutbr.fit.layout.segm.TreeOp;

/**
 * Detects sequences of aligned lines and joins them to a single area.
 * 
 * @author burgetr
 */
public class MultiLineOperator extends BaseOperator
{
    private static Logger log = LoggerFactory.getLogger(MultiLineOperator.class);

    /** Should the lines have a consistent visual style? */
    protected boolean useConsistentStyle;
    
    /** The maximal distance of two areas allowed within a single line (in 'em' units) */
    protected float maxLineEmSpace;
    
    
    public MultiLineOperator()
    {
        useConsistentStyle = false;
        maxLineEmSpace = 1.5f;
    }
    
    public MultiLineOperator(boolean useConsistentStyle, float maxLineEmSpace)
    {
        this.useConsistentStyle = useConsistentStyle;
        this.maxLineEmSpace = maxLineEmSpace;
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.MultiLine";
    }
    
    @Override
    public String getName()
    {
        return "Group aligned lines";
    }

    @Override
    public String getDescription()
    {
        return "Detects sequences of aligned lines and joins them to a single area";
    }

    @Override
    public String getCategory()
    {
        return "lines";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>();
        ret.add(new ParameterBoolean("useConsistentStyle"));
        ret.add(new ParameterFloat("maxLineEmSpace"));
        return ret;
    }

    public boolean getUseConsistentStyle()
    {
        return useConsistentStyle;
    }

    public void setUseConsistentStyle(boolean useConsistentStyle)
    {
        this.useConsistentStyle = useConsistentStyle;
    }

    public float getMaxLineEmSpace()
    {
        return maxLineEmSpace;
    }

    public void setMaxLineEmSpace(float maxLineEmSpace)
    {
        this.maxLineEmSpace = maxLineEmSpace;
    }

    //==============================================================================

    @Override
    public void apply(AreaTree atree)
    {
        recursiveJoinAreas((AreaImpl) atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursiveJoinAreas((AreaImpl) root);
    }
    
    //==============================================================================
    
    
    /**
     * Goes through all the areas in the tree and tries to join their sub-areas into single
     * areas.
     */
    protected void recursiveJoinAreas(AreaImpl root)
    {
        joinAreas(root);
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveJoinAreas((AreaImpl) root.getChildAt(i));
    }
    
    /**
     * Goes through the grid of areas and joins the adjacent visual areas that are not
     * separated by anything
     */
    protected void joinAreas(AreaImpl a)
    {
        AreaTopology t = a.getTopology();
        
        boolean change = true;
        while (change)
        {
            change = false;
            for (int i = 0; i < a.getChildCount(); i++)
            {
                AreaImpl node = (AreaImpl) a.getChildAt(i);
                Rectangular pos = t.getPosition(node);
                int nx1 = pos.getX1();
                int nx2 = pos.getX2();
                int ny2 = pos.getY2();

                //try to expand down - find a neighbor
                AreaImpl neigh = null;
                int dist = 1;
                while (neigh == null && ny2 + dist < t.getTopologyHeight())
                {
                    //try to find some node below in the given distance
                    for (int x = nx1; neigh == null && x <= nx2; x++)
                    {
                        neigh = (AreaImpl) t.findAreaAt(x, ny2 + dist);
                        if (neigh != null) //something found
                        {
                            if ((!useConsistentStyle || AreaStyle.hasSameStyle(node, neigh))
                                    && neigh.getGridPosition().getX1() == nx1)
                            {
                                if (verticalJoin(a, node, neigh, true)) //try to join
                                {
                                    node.updateTopologies();
                                    change = true;
                                }
                            }
                        }
                    }
                    dist++;
                }
                if (change) break; //something changed, repeat
            }
        }
    }

    /**
     * Joins two boxes vertically into one area if the node widths are equal or they 
     * can be aligned to a rectangle using free spaces.
     * @param n1 left node to be aligned
     * @param n2 right node to be aligned
     * @param affect when set to <code>true</code>, the two nodes are joined and n2 is removed from the tree.
     *        When set to <code>false</code>, no changes are performed (only checking)
     * @return <code>true</code> when succeeded
     */
    private boolean verticalJoin(AreaImpl parent, AreaImpl n1, AreaImpl n2, boolean affect)
    {
        //System.out.println("VJoin: " + n1.toString() + " + " + n2.toString());
        //check the maximal distance between the nodes
        int dist = Math.min(Math.abs(n2.getY1() - n1.getY2()), Math.abs(n1.getY1() - n2.getY2()));
        if (dist > n1.getTextStyle().getFontSize() * maxLineEmSpace)
            return false;
        //check if there is no separating border or background
        if (n1.hasBottomBorder() || 
            n2.hasTopBorder() ||
            !AreaStyle.hasEqualBackground(n1, n2))
            return false; //separated, give up
        //align the start
        int sx1 = n1.getGridPosition().getX1();
        int sx2 = n2.getGridPosition().getX1();
        while (sx1 != sx2)
        {
            if (sx1 < sx2) //n1 starts earlier, try to expand n2 to the left
            {
                if (sx2 > 0 && canExpandX(parent, n2, sx2-1, n1))
                    sx2--;
                else
                    return false; //cannot align - give up
            }
            else if (sx1 > sx2) //n2 starts earlier, try to expand n1 to the left
            {
                if (sx1 > 0 && canExpandX(parent, n1, sx1-1, n2))
                    sx1--;
                else
                    return false; //cannot align - give up
            }
        }
        //System.out.println("sy1="+sy1);
        //align the end
        int ex1 = n1.getGridPosition().getX2(); //last
        int ex2 = n2.getGridPosition().getX2();
        while (ex1 != ex2)
        {
            if (ex1 < ex2) //n1 ends earlier, try to expand n1 to the right
            {
                if (ex1 < parent.getTopology().getTopologyHeight()-1 && canExpandX(parent, n1, ex1+1, n2))
                    ex1++;
                else
                    return false; //cannot align - give up
            }
            else if (ex1 > ex2) //n2 ends earlier, try to expand n2 to the right
            {
                if (ex2 < parent.getTopology().getTopologyHeight()-1 && canExpandX(parent, n2, ex2+1, n1))
                    ex2++;
                else
                    return false; //cannot align - give up
            }
        }
        //System.out.println("ey1="+ey1);
        //align succeeded, join the areas
        if (affect)
        {
            log.debug("VJoin: {} + {}", n1, n2);
            Rectangular newpos = new Rectangular(sx1, n1.getGridPosition().getY1(),
                                                 ex1, n2.getGridPosition().getY2());
            TreeOp.joinArea(n1, n2, newpos, true);
            parent.removeChild(n2);
        }
        return true;
    }
    
    
    /**
     * Checks if the area can be horizontally expanded to the given 
     * X coordinate, i.e. there is a free space in the space on this X coordinate
     * for the whole width of the area.
     * @param node the area node that should be expanded
     * @param x the X coordinate to that the area should be expanded
     * @param except an area that shouldn't be considered for conflicts (e.g. an overlaping area)
     * @return <code>true</code> if the area can be expanded
     */
    private boolean canExpandX(AreaImpl parent, AreaImpl node, int x, AreaImpl except)
    {
        AreaTopology t = parent.getTopology();
        int gy = t.getPosition(node).getY1();
        int gh = t.getTopologyHeight();
        for (int y = gy; y < gy + gh; y++)
        {
            AreaImpl cand = (AreaImpl) t.findAreaAt(x, y);
            if (cand != null && cand != except)
                return false; //something found - cannot expand
        }
        return true;
    }

}
