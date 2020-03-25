/**
 * BoxTransform.java
 *
 * Created on 22. 11. 2016, 18:43:16 by burgetr
 */
package cz.vutbr.fit.layout.cssbox;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A transformation applied to a box according to the "transform" CSS property.
 * 
 * @author burgetr
 */
public class BoxTransform
{
    private Box box;
    private AffineTransform transform;
    
    /**
     * Creates a transformation for the given box based on its CSS properties. An empty transformation
     * is created for boxes that are not element boxes (they have no CSS style assigned). 
     * @param box the source box
     */
    public BoxTransform(Box box)
    {
        this.box = box;
        if (box instanceof ElementBox)
            transform = createTransform((ElementBox) box);
        else
            transform = null;
    }

    public BoxTransform(BoxTransform src)
    {
        box = src.box;
        transform = src.transform;
    }
    
    /**
     * Obtains the source box.
     * @return The box that this transformation belongs to.
     */
    public Box getBox()
    {
        return box;
    }

    /**
     * Obtains the transformation that should be applied.
     * @return The transformation object or {@code null} if no transformation should be applied.
     */
    public AffineTransform getTransform()
    {
        return transform;
    }
    
    /**
     * Checks if the transformation is empty (no transformation is applied). 
     * @return {@code true} when no transformation is applied.
     */
    public boolean isEmpty()
    {
        return (transform == null);
    }
    
    /**
     * Concatenates another transformation to this transformation.
     * @param src
     * @return A new concatenated transformation.
     */
    public BoxTransform concatenate(BoxTransform src)
    {
        if (src.isEmpty())
            return this;
        else if (this.isEmpty())
            return src;
        else
        {
            BoxTransform ret = new BoxTransform(this);
            ret.transform = new AffineTransform(transform);
            ret.transform.concatenate(src.transform);
            return ret;
        }
    }
    
    /**
     * Transforms a rectangle to other rectangle using the given transformation.
     * @param rect the source rectangle
     * @return the bounding box of the transformed rectangle.
     */
    public Rectangular transformRect(Rectangular rect)
    {
        if (transform != null)
        {
            Rectangle src = new Rectangle(rect.getX1(), rect.getY1(), rect.getWidth(), rect.getHeight());
            Shape dest = transform.createTransformedShape(src);
            Rectangle destr;
            if (dest instanceof Rectangle)
                destr = (Rectangle) dest;
            else
                destr = dest.getBounds();
            return Units.toRectangular(destr);
        }
        else
            return rect;
    }
    
    //=========================================================================================================================
    
    protected AffineTransform createTransform(ElementBox elem)
    {
        return org.fit.cssbox.render.Transform.createTransform(elem);
    }
    
}
