/**
 * Rect.java
 *
 * Created on 22. 10. 2014, 11:38:07 by burgetr
 */
package cz.vutbr.fit.layout.model;

/**
 * A generic rectangle with certain position, width and height.
 * 
 * @author burgetr
 */
public interface Rect
{

    public int getX1();
    
    public int getY1();
    
    public int getX2();
    
    public int getY2();
    
    public int getWidth();
    
    public int getHeight();
    
    public void move(int xofs, int yofs);
    
}
