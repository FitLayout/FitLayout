/**
 * AreaLayout.java
 *
 * Created on 12. 2. 2023, 18:28:02 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.vutbr.fit.layout.model.Area;

/**
 * Area layout analysis utilities.
 * 
 * @author burgetr
 */
public class AreaLayout
{

    public static boolean hasInFlowLayout(Area root)
    {
        float em = root.getTextStyle().getFontSize();
        // find leaf or separated areas
        final List<Area> leaves = root.findNodesPreOrder(a -> (a.isLeaf() || a.isBackgroundSeparated()));
        // sort by position
        Collections.sort(leaves, new Comparator<Area>() {
            public int compare(Area a1, Area a2)
            {
                return a1.getY1() == a2.getY1() ? a1.getX1() - a2.getX1() : a1.getY1() - a2.getY1();
            }
        });
        // detect different twists and turns in the layout
        boolean atStart = true;
        int lastX = 0;
        int lastY = 0;
        for (Area a : leaves)
        {
            final var bounds = a.getBounds();
            if (!atStart)
            {
                int difY = bounds.getY1() - lastY;
                if (difY > 0.8f * em) // a very big vertical skip 
                    return false;
                int difX = bounds.getX1() - lastX;
                if (difX > 1.0f * em) // a big horizontal skip
                    return false;
            }
            lastX = bounds.getX2();
            lastY = bounds.getY2();
            atStart = false;
        }
        // no twists detected
        return true;
    }
    
}
