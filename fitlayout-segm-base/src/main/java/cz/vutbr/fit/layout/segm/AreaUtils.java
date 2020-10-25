/**
 * AreaUtils.java
 *
 * Created on 13. 3. 2015, 17:01:42 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import java.util.Vector;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * General purpose area analysis functions.
 * 
 * @author burgetr
 */
public class AreaUtils
{

    /**
     * Checks if the given areas are in the same visual group (i.e. "are near each other"). 
     * @param a1
     * @param a2
     * @return
     */
    public static boolean isNeighbor(Area a1, Area a2)
    {
        if (isOnSameLine(a1, a2))
            return true; //on the same line
        else
        {
            //the Y difference is less than half the line height
            int dy = a2.getBounds().getY1() - a1.getBounds().getY2();
            if (dy < 0)
                dy = a1.getBounds().getY1() - a2.getBounds().getY2();
            return dy < a1.getBounds().getHeight() / 2;
        }
    }
    
    /**
     * Checks if the given areas are on the same line.
     * @param a1
     * @param a2
     * @return
     */
    public static boolean isOnSameLine(Area a1, Area a2)
    {
        return isOnSameLine(a1, a2, 1);
    }
    
    /**
     * Checks if the given areas are on the same line.
     * @param a1
     * @param a2
     * @return
     */
    public static boolean isOnSameLine(Area a1, Area a2, int threshold)
    {
        final Rectangular gp1 = a1.getBounds();
        final Rectangular gp2 = a2.getBounds();
        return (Math.abs(gp1.getY1() - gp2.getY1()) <= threshold 
                && Math.abs(gp1.getY2() - gp2.getY2()) <= threshold); 
    }
    
    public static boolean isOnSameLineRoughly(Area a1, Area a2)
    {
        final Rectangular gp1 = a1.getBounds();
        final Rectangular gp2 = a2.getBounds();
        return (gp2.getY1() >= gp1.getY1() && gp2.getY1() < gp1.getY2())
                || (gp2.getY2() > gp1.getY1() && gp2.getY2() <= gp1.getY2());
    }
    
    /**
     * Checks if the given area has a target URL assigned (it acts as a link)
     * @param a
     * @return
     */
    public static boolean isLink(Area a)
    {
        for (Box box : a.getBoxes())
        {
            if (box.getAttribute("href") != null)
                return true;
        }
        return false;
    }

    public static Area createSuperAreaFromVerticalRegion(Area root, Rectangular region)
    {
        //find the first and last area that belong to the region
        int first = -1;
        int last = -1;
        Rectangular bounds = null;
        Vector<Area> selected = new Vector<Area>();
        for (int i = 0; i < root.getChildCount(); i++)
        {
            final Rectangular pos = root.getChildAt(i).getBounds();
            if (region.enclosesY(pos))
            {
                //System.out.println("BELONGS " + root.getChildArea(i));
                if (first == -1)
                    first = i;
                last = i;
                selected.add(root.getChildAt(i));
                if (bounds == null)
                    bounds = new Rectangular(pos);
                else
                    bounds.expandToEnclose(pos);
            }
            else
            {
                //System.out.println("NOT BELONGS " + root.getChildArea(i));
                if (first != -1)
                    break; //region finished
            }
        }
        //System.out.println("first=" + first + " last=" + last);
        if (last > first)
        {
            Area ret = root.getAreaTree().createArea(bounds);
            root.insertChild(ret, first);
            for (Area a : selected)
                ret.appendChild(a);
            root.updateTopologies();
            ret.updateTopologies();
            return ret;
        }
        else
            return null;
    }
    
}
