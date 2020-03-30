/**
 * RectangularZ.java
 *
 * Created on 27. 3. 2019, 15:19:58 by burgetr
 */
package cz.vutbr.fit.layout.cssbox.impl;

import org.fit.cssbox.layout.Rectangle;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Zoomed rectangular.
 * @author burgetr
 */
public class RectangularZ extends Rectangular
{
    
    public RectangularZ(Rectangle src, float zoomFactor)
    {
        x1 = Math.round(src.getX() * zoomFactor);
        y1 = Math.round(src.getY() * zoomFactor);
        x2 = Math.round((src.getX() + src.getWidth() - 1) * zoomFactor);
        y2 = Math.round((src.getY() + src.getHeight() - 1) * zoomFactor);
    }
    
    public RectangularZ(float x1, float y1, float zoomFactor)
    {
        this.x1 = Math.round(x1 * zoomFactor);
        this.y1 = Math.round(y1 * zoomFactor);
        this.x2 = this.x1 - 1;
        this.y2 = this.y1 - 1;
    }
    
}
