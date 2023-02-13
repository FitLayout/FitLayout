/**
 * AboveFunction.java
 *
 * Created on 13. 2. 2023, 15:22:48 by burgetr
 */
package cz.vutbr.fit.layout.rdf.fn;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * This function decides whether a rectangle rect1 is above another rectangle rect2. Both 
 * rectangles must have either the {@code b:bounds} property set to a b:Bounds object or 
 * they must be the b:Bounds object directly. Returns a boolean literal. 
 * 
 * @author burgetr
 */
public class AboveFunction extends RectComparisonFunction
{
    public static final int MIN_X_INTERSECTION = 3; // we require at least 3 pixels intersection
    public static final String fname = "isAbove"; 

    public AboveFunction()
    {
        super(fname);
    }

    @Override
    protected boolean evaluateForBoxes(Rectangular b1, Rectangular b2)
    {
        //here b1 is the top area, b2 is the bottom area
        //we say that b1 is above b2
        boolean ret = false;
        if (b1 != null && b2 != null && !b1.intersects(b2))
        {
            final Rectangular inter = b1.intersection(new Rectangular(b2.getX1(), b1.getY1(), b2.getX2(), b1.getY2()));
            if (inter.getWidth() > MIN_X_INTERSECTION)
            {
                if (b1.getY2() <= b2.getY1())
                    ret = true;
            }
        }
        return ret;
    }

}
