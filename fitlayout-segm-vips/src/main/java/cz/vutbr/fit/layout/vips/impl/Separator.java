/**
 * VIPS - Visual Internet Page Segmentation for FitLayout
 * 
 * Tomas Popela, 2012
 * Radek Burget, 2020 
 */

package cz.vutbr.fit.layout.vips.impl;

/**
 * A detected visual separator.
 * @author burgetr
 */
public class Separator implements Comparable<Separator> 
{
    public boolean vertical;
	public int startPoint = 0;
	public int endPoint = 0;
	public int weight = 3;
	public int normalizedWeight;
	

	public Separator(int start, int end, boolean vertical) 
	{
	    this.vertical = vertical;
		this.startPoint = start;
		this.endPoint = end;
	}

	public Separator(int start, int end, boolean vertical, int weight) 
	{
        this.vertical = vertical;
		this.startPoint = start;
		this.endPoint = end;
		this.weight = weight;
	}

    public Separator(Separator src) 
    {
        this.vertical = src.vertical;
        this.startPoint = src.startPoint;
        this.endPoint = src.endPoint;
    }

	public boolean isVertical()
    {
        return vertical;
    }

    public void setVertical(boolean vertical)
    {
        this.vertical = vertical;
    }

    public int getStartPoint()
    {
        return startPoint;
    }

    public void setStartPoint(int startPoint)
    {
        this.startPoint = startPoint;
    }

    public int getEndPoint()
    {
        return endPoint;
    }

    public void setEndPoint(int endPoint)
    {
        this.endPoint = endPoint;
    }

    public int getWeight()
    {
        return weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    public int getNormalizedWeight()
    {
        return normalizedWeight;
    }

    public void setNormalizedWeight(int normalizedWeight)
    {
        this.normalizedWeight = normalizedWeight;
    }

    /**
     * Check whether this separator is inside a visual area (it splits the area in two parts)
     * @param area the visual area to check
     * @return {@code true} if the separator splits the area in two parts
     */
    public boolean isInside(VisualArea area)
    {
        if (vertical)
        {
            return startPoint > area.getX1() && endPoint < area.getX2();
        }
        else
        {
            return startPoint > area.getY1() && endPoint < area.getY2();
        }
    }
    
    @Override
	public int compareTo(Separator otherSeparator)
	{
        final int dif = otherSeparator.weight - this.weight;
        if (dif == 0)
        {
            //if the weight is equal, prefer horizontal separators over vertical ones
            return (otherSeparator.vertical ? 0 : 1) - (this.vertical ? 0 : 1);
        }
        else
            return dif;
	}

    @Override
    public String toString()
    {
        return "Separator [vertical=" + vertical + ", startPoint=" + startPoint
                + ", endPoint=" + endPoint + ", weight=" + weight + "]";
    }
    
}
