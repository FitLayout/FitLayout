/**
 * Rectangular.java
 *
 * Created on 24.11.2006, 22:16:54 by radek
 */
package cz.vutbr.fit.layout.model;

/**
 * This class represents a general rectangular area.
 * 
 * @author radek
 */
public class Rectangular implements Rect
{
	protected int x1;
	protected int y1;
	protected int x2;
	protected int y2;
	
	public Rectangular()
	{
		x1 = 0;
		y1 = 0;
		x2 = -1;
		y2 = -1;
	}
	
	/**
	 * Creates a rectangle at the given coordinates.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public Rectangular(int x1, int y1, int x2, int y2)
	{
		this.x1 = Math.min(x1, x2);
		this.x2 = Math.max(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.y2 = Math.max(y1, y2);
	}
	
    /**
     * Creates an empty rectangle at the given coordinates.
     * @param x1
     * @param y1
     */
	public Rectangular(int x1, int y1)
    {
        this.x1 = x1;
        this.x2 = x1 - 1;
        this.y1 = y1;
        this.y2 = y1 - 1;
    }
    
	public Rectangular(Rectangular src)
	{
		this.x1 = src.x1;
		this.x2 = src.x2;
		this.y1 = src.y1;
		this.y2 = src.y2;
	}
	
    public void copy(Rectangular src)
    {
        this.x1 = src.x1;
        this.x2 = src.x2;
        this.y1 = src.y1;
        this.y2 = src.y2;
    }
    
	/**
	 * @return the x1
	 */
    @Override
	public int getX1()
	{
		return x1;
	}
	
	/**
	 * @param x1 the x1 to set
	 */
	public void setX1(int x1)
	{
		this.x1 = x1;
	}
	
	/**
	 * @return the x2
	 */
    @Override
	public int getX2()
	{
		return x2;
	}
	
	/**
	 * @param x2 the x2 to set
	 */
	public void setX2(int x2)
	{
		this.x2 = x2;
	}
	
	/**
	 * @return the y1
	 */
    @Override
	public int getY1()
	{
		return y1;
	}
	
	/**
	 * @param y1 the y1 to set
	 */
	public void setY1(int y1)
	{
		this.y1 = y1;
	}
	
	/**
	 * @return the y2
	 */
    @Override
	public int getY2()
	{
		return y2;
	}
	
	/**
	 * @param y2 the y2 to set
	 */
	public void setY2(int y2)
	{
		this.y2 = y2;
	}
	
    @Override
	public int getWidth()
	{
		return x2 - x1 + 1;
	}
	
    @Override
	public int getHeight()
	{
		return y2 - y1 + 1; 
	}
	
    public int midX()
    {
        return (x2 + x1) / 2;
    }
    
    public int midY()
    {
        return (y2 + y1) / 2;
    }
    
    /**
     * Changes the rectangle coordinates by adding the specified X and Y offsets
     * @param xofs the X offset
     * @param yofs the Y offset
     */
    @Override
    public void move(int xofs, int yofs)
    {
        x1 += xofs;
        y1 += yofs;
        x2 += xofs;
        y2 += yofs;
    }
    
    public int getArea()
    {
    	int a = getWidth() * getHeight();
    	return (a >= 0) ? a : 0;
    }
    
    public boolean isEmpty()
    {
    	return x2 < x1 || y2 < y1;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Rectangular)
        {
            Rectangular r = (Rectangular) obj;
            return r.x1 == x1 && r.y1 == y1 && r.x2 == x2 && r.y2 == y2;
        }
        else
            return false;
    }

    /**
     * Checks if this rectangle entirely contains another rectangle.
     * @param other the other rectangle
     * @return true when the other rectangle is completely contained in this one
     */
    public boolean encloses(Rectangular other)
    {
        return x1 <= other.x1 &&
               y1 <= other.y1 &&
               x2 >= other.x2 &&
               y2 >= other.y2;
    }
	
    /**
     * Checks if this rectangle entirely contains the X coordinates of another rectangle.
     * @param other the other rectangle
     * @return true when the X coordinates of the other rectangle are completely contained in this one
     */
    public boolean enclosesX(Rectangular other)
    {
        return x1 <= other.x1 &&
               x2 >= other.x2;
    }
    
    /**
     * Checks if this rectangle entirely contains the Y coordinates of another rectangle.
     * @param other the other rectangle
     * @return true when the Y coordinates of the other rectangle are completely contained in this one
     */
    public boolean enclosesY(Rectangular other)
    {
        return y1 <= other.y1 &&
               y2 >= other.y2;
    }
    
    /**
     * Checks if this rectangle contains a point.
     * @param x the point X coordinate
     * @param y the point Y coordinate
     * @return true when the point is contained in this one
     */
    public boolean contains(int x, int y)
    {
        return x1 <= x &&
               y1 <= y &&
               x2 >= x &&
               y2 >= y;
    }
    
	public boolean intersects(Rectangular other)
	{
	    if (this.isEmpty() || other.isEmpty())
	        return false;
	    else
    	    return !(other.x1 > x2 
        			|| other.x2 < x1 
        			|| other.y1 > y2 
        			|| other.y2 < y1);
	}
	
	public boolean intersectsX(Rectangular other)
	{
        if (this.isEmpty() || other.isEmpty())
            return false;
        else
            return !(other.x1 > x2 || other.x2 < x1); 
	}

    public boolean intersectsY(Rectangular other)
    {
        if (this.isEmpty() || other.isEmpty())
            return false;
        else
            return !(other.y1 > y2 || other.y2 < y1); 
    }

    /**
     * Computes the intersection of this rectangle with another one.
     * @param other the other rectangle
     * @return the resulting intersection or an empty rectangle when there is no intersection
     */
    public Rectangular intersection(Rectangular other)
    {
    	if (this.intersects(other))
	    {
    		return new Rectangular(Math.max(x1, other.x1),
    							   Math.max(y1, other.y1),
    							   Math.min(x2, other.x2),
    							   Math.min(y2, other.y2));
	    }
	    else
	    {
	        return new Rectangular(); //an empty rectangle
	    }    	
    }
    
    /**
     * Computes the union of this rectangle with another one.
     * @param other the other rectangle
     * @return the union rectangle
     */
    public Rectangular union(Rectangular other)
    {
        return new Rectangular(Math.min(x1, other.x1),
                               Math.min(y1, other.y1),
                               Math.max(x2, other.x2),
                               Math.max(y2, other.y2));
    }
    
    /**
     * Replaces the X coordinates of the rectangle with the X coordinates of another one. 
     * @param other the rectangle whose X coordinates will be used
     * @return the resulting rectangle
     */
    public Rectangular replaceX(Rectangular other)
    {
        Rectangular ret = new Rectangular(this);
        ret.x1 = other.x1;
        ret.x2 = other.x2;
        return ret;
    }
    
    /**
     * Replaces the Y coordinates of the rectangle with the Y coordinates of another one. 
     * @param other the rectangle whose Y coordinates will be used
     * @return the resulting rectangle
     */
    public Rectangular replaceY(Rectangular other)
    {
        Rectangular ret = new Rectangular(this);
        ret.y1 = other.y1;
        ret.y2 = other.y2;
        return ret;
    }
    
    /**
     * If this rectangle intersets with the other one, splits this rectangle horizontally so that it does not intersect with the other one anymore. 
     * @param other the rectangle used to split this one
     * @return if this rectangle had to be split in two parts, the second one is returned. Otherwise, null is returned.
     */
    public Rectangular hsplit(Rectangular other)
    {
        if (this.intersects(other))
        {
            Rectangular a = new Rectangular(this);
            Rectangular b = new Rectangular(this);
            if (a.x2 > other.x1 - 1) a.x2 = other.x1 - 1;
            if (b.x1 < other.x2 + 1) b.x1 = other.x2 + 1;
            if (a.isEmpty())
            {
                x1 = b.x1;
                return null;
            }
            else
            {
                x2 = a.x2;
                if (b.isEmpty())
                    return null;
                else
                    return b;
            }
        }
        else
            return null;
    }
    
    /**
     * If this rectangle intersets with the other one, splits this rectangle horizontally so that it does not intersect with the other one anymore. 
     * @param other the rectangle used to split this one
     * @return if this rectangle had to be split in two parts, the second one is returned. Otherwise, null is returned.
     */
    public Rectangular vsplit(Rectangular other)
    {
        if (this.intersects(other))
        {
            Rectangular a = new Rectangular(this);
            Rectangular b = new Rectangular(this);
            if (a.y2 > other.y1 - 1) a.y2 = other.y1 - 1;
            if (b.y1 < other.y2 + 1) b.y1 = other.y2 + 1;
            if (a.isEmpty())
            {
                y1 = b.y1;
                return null;
            }
            else
            {
                y2 = a.y2;
                if (b.isEmpty())
                    return null;
                else
                    return b;
            }
        }
        else
            return null;
    }
    
    public void expandToEnclose(Rectangular other)
    {
    	if (other.x1 < x1) x1 = other.x1;
    	if (other.y1 < y1) y1 = other.y1;
    	if (other.x2 > x2) x2 = other.x2;
    	if (other.y2 > y2) y2 = other.y2;
    }
    
	public String toString()
	{
		return "[" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + "]";
	}
}
