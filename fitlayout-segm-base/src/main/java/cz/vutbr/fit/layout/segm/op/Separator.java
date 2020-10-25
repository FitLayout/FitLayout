/**
 * 
 */

package cz.vutbr.fit.layout.segm.op;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * @author radek
 * A horizontal or vertical visual separator.
 */
public class Separator extends Rectangular implements Comparable<Separator>
{
    /* Separator types */
    //TODO convert to enum
	public static final short HORIZONTAL = 0;
    public static final short VERTICAL = 1;
    public static final short BOXH = 2;
    public static final short BOXV = 3;
    
    /** Separator type -- either HORIZONTAL, VERTICAL or BOX */
    protected short type;
    
    /** Left (top) separated area node (if any) */
    protected Area area1;
    
    /** Bottom (right) separated area node (if any) */
    protected Area area2;
    
    //======================================================================================
    
	public Separator(short type, int x1, int y1, int x2, int y2)
	{
        super(x1, y1, x2, y2);
        this.type = type;
        area1 = null;
        area2 = null;
	}

	public Separator(Separator orig)
	{
        super(orig);
        this.type = orig.type;
        area1 = orig.area1;
        area2 = orig.area2;
	}
	
    public Separator(short type, Rectangular rect)
    {
        super(rect);
        this.type = type;
        area1 = null;
        area2 = null;
    }

    public short getType()
    {
        return type;
    }
    
    public void setType(short type)
    {
    	this.type = type;
    }
    
    public boolean isBoxSep()
    {
        return type == BOXH || type == BOXV;
    }

    public Area getArea1()
    {
        return area1;
    }

    public void setArea1(Area area1)
    {
        this.area1 = area1;
    }

    public Area getArea2()
    {
        return area2;
    }

    public void setArea2(Area area2)
    {
        this.area2 = area2;
    }

    public String toString()
    {
        String t = "?";
        switch (type)
        {
            case HORIZONTAL: t = "HSep"; break;
            case VERTICAL:   t = "VSep"; break;
            case BOXH:       t = "BoxH"; break;
            case BOXV:       t = "BoxV"; break;
        }
        return t + " (" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")" + " w=" + getWeight() + " a1=" + area1 + " a2=" + area2;
    }

    public int compareTo(Separator other)
    {
        return other.getWeight() - getWeight();
    }

	//======================================================================================

    public boolean isHorizontal()
    {
        return getWidth() >= getHeight();
    }
    
    public boolean isVertical()
    {
        return getWidth() < getHeight();
    }
    
	public int getWeight()
	{
	    //TODO a very basic algorithm, add documentation
		int ww = Math.min(getWidth(), getHeight()) / 10;
		ww = isVertical() ? (ww * 2) : ww;
		return ww;
	}
	
    //======================================================================================
	
	public Separator hsplit(Separator other)
	{
	    Rectangular r = super.hsplit(other);
	    if (r == null)
	        return null;
	    else
	        return new Separator(type, r);
	}
	
    public Separator vsplit(Separator other)
    {
        Rectangular r = super.vsplit(other);
        if (r == null)
            return null;
        else
            return new Separator(type, r);
    }
    
}
