/**
 * AfterFunction.java
 *
 * Created on 13. 2. 2023, 15:38:00 by burgetr
 */
package cz.vutbr.fit.layout.rdf.fn;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * This function decides whether a rectangle rect1 is after another rectangle rect2 roughly 
 * at the same line. 
 * Both rectangles must have either the {@code b:bounds} property set to a b:Bounds object or 
 * they must be the b:Bounds object directly. Returns a boolean literal. 
 * 
 * @author burgetr
 */
public class AfterFunction extends RectComparisonFunction
{
    public static final int MIN_Y_INTERSECTION = 3; // we require at least 3 pixels intersection
    public static final String fname = "isAfter"; 

    public AfterFunction()
    {
        super(fname);
        System.err.println("NEW!!!");
    }

    @Override
    protected boolean evaluateForBoxes(Rectangular b1, Rectangular b2)
    {
        //here b1 is the right area, b2 is the left area
        //we say that b1 is after b2
        boolean ret = false;
        if (b1 != null && b2 != null && !b1.intersects(b2))
        {
            final Rectangular inter = b1.intersection(new Rectangular(b1.getX1(), b2.getY1(), b1.getX2(), b2.getY2()));
            if (inter.getHeight() > MIN_Y_INTERSECTION)
            {
                if (b1.getX1() >= b2.getX2())
                    ret = true;
            }
        }
        return ret;
    }

}
