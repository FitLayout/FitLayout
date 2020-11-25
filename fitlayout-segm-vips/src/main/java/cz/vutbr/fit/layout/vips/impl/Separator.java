/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Separator.java
 */

package cz.vutbr.fit.layout.vips.impl;

/**
 * Class that represents a visual separator.
 * @author burgetr
 */
public class Separator implements Comparable<Separator> 
{
    public boolean vertical;
	public int startPoint = 0;
	public int endPoint = 0;
	public int weight = 3;

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

    @Override
	public int compareTo(Separator otherSeparator)
	{
		return this.weight - otherSeparator.weight;
	}

    @Override
    public String toString()
    {
        return "Separator [vertical=" + vertical + ", startPoint=" + startPoint
                + ", endPoint=" + endPoint + ", weight=" + weight + "]";
    }
    
}
