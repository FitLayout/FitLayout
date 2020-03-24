/**
 * RectangularZ.java
 *
 * Created on 27. 3. 2019, 15:19:58 by burgetr
 */
package cz.vutbr.fit.layout.cssbox;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Zoomed rectangular.
 * @author burgetr
 */
public class RectangularZ extends Rectangular
{
    
    public RectangularZ(java.awt.Rectangle src, float zoomFactor)
    {
        super(Units.toRectangular(src));
        zoom(zoomFactor);
    }
    
    public RectangularZ(int x1, int y1, float zoomFactor)
    {
        super(x1, y1);
        zoom(zoomFactor);
    }
    
    private void zoom(float zoom)
    {
        x1 = Math.round(x1 * zoom);
        y1 = Math.round(y1 * zoom);
        x2 = Math.round(x2 * zoom);
        y2 = Math.round(y2 * zoom);
    }

}
