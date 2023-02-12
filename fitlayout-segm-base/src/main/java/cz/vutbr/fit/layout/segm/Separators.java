/**
 * Separators.java
 *
 * Created on 24. 10. 2020, 21:48:34 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import java.util.Iterator;
import java.util.List;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.segm.op.Separator;
import cz.vutbr.fit.layout.segm.op.SeparatorSet;

/**
 * Implementation of the operations with separators.
 * 
 * @author burgetr
 */
public class Separators
{
    /** The name of the area node attribute that is used for storing the separator sets */
    public static final String ATTR_SEPARATORS = "segm.seps";
    
    /**
     * Creates a separators set of an area and stores it as an attribute of the area.
     * @param area the area to process
     * @return the created separator set that has been also attached to the area
     */
    public static SeparatorSet createSeparatorsForArea(Area area)
    {
        final SeparatorSet seps = Config.createSeparators(area);
        area.addUserAttribute(ATTR_SEPARATORS, seps);
        return seps;
    }

    public static SeparatorSet getSeparatorsForArea(Area area)
    {
        return area.getUserAttribute(ATTR_SEPARATORS, SeparatorSet.class);
    }

    /**
     * Removes simple separators from current separator set. A simple separator
     * has only one or zero visual areas at each side
     */
    public static void removeSimpleSeparators(Area area)
    {
        final SeparatorSet seps = getSeparatorsForArea(area);
        removeSimpleSeparators(area, seps.getHorizontal());
        removeSimpleSeparators(area, seps.getVertical());
        removeSimpleSeparators(area, seps.getBoxsep());
    }
    
    /**
     * Removes simple separators from a vector of separators. A simple separator
     * has only one or zero visual areas at each side.
     */
    private static void removeSimpleSeparators(Area area, List<Separator> v)
    {
        //System.out.println("Rem: this="+this);
        for (Iterator<Separator> it = v.iterator(); it.hasNext();)
        {
            Separator sep = it.next();
            if (sep.getType() == Separator.HORIZONTAL || sep.getType() == Separator.BOXH)
            {
                int a = countAreasAbove(area, sep);
                int b = countAreasBelow(area, sep);
                if (a <= 1 && b <= 1)
                    it.remove();
            }
            else
            {
                int a = countAreasLeft(area, sep);
                int b = countAreasRight(area, sep);
                if (a <= 1 && b <= 1)
                    it.remove();
            }
        }
    }

    /**
     * @return the number of the areas directly above the separator
     */
    private static int countAreasAbove(Area a, Separator sep)
    {
        int gx1 = a.getTopology().toTopologyX(sep.getX1());
        int gx2 = a.getTopology().toTopologyX(sep.getX2());
        int gy = a.getTopology().toTopologyY(sep.getY1() - 1);
        int ret = 0;
        if (gx1 >= 0 && gx2 >= 0 && gy >= 0)
        {
            int i = gx1;
            while (i <= gx2)
            {
                Area node = (Area) a.getTopology().findAreaAt(i, gy);
                //System.out.println("Search: " + i + ":" + gy + " = " + node);
                if (node != null)
                {
                    ret++;
                    i += a.getTopology().getPosition(node).getWidth();
                }
                else
                    i++;
            }
        }
        return ret;
    }
    
    /**
     * @return the number of the areas directly below the separator
     */
    private static int countAreasBelow(Area a, Separator sep)
    {
        int gx1 = a.getTopology().toTopologyX(sep.getX1());
        int gx2 = a.getTopology().toTopologyX(sep.getX2());
        int gy = a.getTopology().toTopologyY(sep.getY2() + 1);
        int ret = 0;
        if (gx1 >= 0 && gx2 >= 0 && gy >= 0)
        {
            int i = gx1;
            while (i <= gx2)
            {
                Area node = (Area) a.getTopology().findAreaAt(i, gy);
                //System.out.println("Search: " + i + ":" + gy + " = " + node);
                if (node != null)
                {
                    ret++;
                    i += a.getTopology().getPosition(node).getWidth();
                }
                else
                    i++;
            }
        }
        return ret;
    }

    /**
     * @return the number of the areas directly on the left of the separator
     */
    private static int countAreasLeft(Area a, Separator sep)
    {
        int gy1 = a.getTopology().toTopologyY(sep.getY1());
        int gy2 = a.getTopology().toTopologyY(sep.getY2());
        int gx = a.getTopology().toTopologyX(sep.getX1() - 1);
        int ret = 0;
        if (gy1 >= 0 && gy2 >= 0 && gx >= 0)
        {
            int i = gy1;
            while (i <= gy2)
            {
                Area node = (Area) a.getTopology().findAreaAt(gx, i);
                if (node != null)
                {
                    ret++;
                    i += a.getTopology().getPosition(node).getWidth();
                }
                else
                    i++;
            }
        }
        return ret;
    }
    
    /**
     * @return the number of the areas directly on the left of the separator
     */
    private static int countAreasRight(Area a, Separator sep)
    {
        int gy1 = a.getTopology().toTopologyY(sep.getY1());
        int gy2 = a.getTopology().toTopologyY(sep.getY2());
        int gx = a.getTopology().toTopologyX(sep.getX2() + 1);
        int ret = 0;
        if (gy1 >= 0 && gy2 >= 0 && gx >= 0)
        {
            int i = gy1;
            while (i <= gy2)
            {
                Area node = (Area) a.getTopology().findAreaAt(gx, i);
                if (node != null)
                {
                    ret++;
                    i += a.getTopology().getPosition(node).getWidth();
                }
                else
                    i++;
            }
        }
        return ret;
    }
    
}
