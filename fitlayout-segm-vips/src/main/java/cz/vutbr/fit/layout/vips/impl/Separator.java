/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Separator.java
 */

package cz.vutbr.fit.layout.vips.impl;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Class that represents a visual separator.
 * @author burgetr
 */
public class Separator extends Rectangular implements Comparable<Separator> 
{
	public int startPoint = 0;
	public int endPoint = 0;
	public int weight = 3;
	public int normalizedWeight = 0;

	public Separator(int start, int end) 
	{
		this.startPoint = start;
		this.endPoint = end;
	}

	public Separator(int start, int end, int weight) 
	{
		this.startPoint = start;
		this.endPoint = end;
		this.weight = weight;
	}

	public void setTopLeft(int x, int y)
	{
		setX1(x);
		setY1(y);
	}

	public void setBottomRight(int x, int y)
	{
        setX2(x);
        setY2(y);
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
}
