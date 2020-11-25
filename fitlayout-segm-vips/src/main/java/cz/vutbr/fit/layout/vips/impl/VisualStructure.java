/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructurejava
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A class that represents visual structure.
 * @author Tomas Popela
 *
 */
public class VisualStructure 
{
	private List<VipsBlock> blockRoots;
	private List<VisualStructure> childStructures;
	private List<Separator> separators;
	private Separator top;
    private Separator bottom;
    private Separator left;
    private Separator right;
    private Rectangular bounds;
	private int doC = 12;

	public VisualStructure()
	{
		blockRoots = new ArrayList<>();
		childStructures = new ArrayList<>();
		separators = new ArrayList<>();
		bounds = new Rectangular();
	}

    public VisualStructure(VisualStructure src)
    {
        this();
        top = src.top;
        bottom = src.bottom;
        left = src.left;
        right = src.right;
        bounds = new Rectangular(src.bounds);
        doC = src.doC;
    }
	
    /**
     * Creates a visual structure covering a pair of visual structures.
     * @param pair
     */
    public VisualStructure(SepPair pair)
    {
        this(pair.a);
        joinPair(pair, Set.of(pair.a));
    }
	
	/**
	 * @return Nested blocks in structure
	 */
	public List<VipsBlock> getBlockRoots()
	{
		return blockRoots;
	}

	/**
	 * Adds block to nested blocks
	 * @param blockRoot New block
	 */
	public void addBlock(VipsBlock blockRoot)
	{
		this.blockRoots.add(blockRoot);
	}

	/**
	 * Adds blocks to nested blocks
	 * @param blockRoots
	 */
	public void addBlocks(List<VipsBlock> blockRoots)
	{
		this.blockRoots.addAll(blockRoots);
	}

	/**
	 * Sets blocks as nested blocks
	 * @param vipsBlocks
	 */
	public void setBlockRoots(List<VipsBlock> vipsBlocks)
	{
		this.blockRoots = vipsBlocks;
	}

	/**
	 * Clears nested blocks list
	 */
	public void clearBlocks()
	{
		this.blockRoots.clear();
	}

    /**
     * Adds new child to visual structure children
     * @param visualStructure New child
     */
    public void addChild(VisualStructure visualStructure)
    {
        this.childStructures.add(visualStructure);
    }

	/**
	 * Adds new child to visual structure at given index
	 * @param visualStructure New child
	 * @param index Index
	 */
	public void addChildAt(VisualStructure visualStructure, int index)
	{
		this.childStructures.add(index, visualStructure);
	}

    /**
     * Removes given child from structures children
     * @param visualStructure Child
     */
    public void removeChild(VisualStructure visualStructure)
    {
        this.childStructures.remove(visualStructure);
    }

	/**
	 * Returns all children structures
	 * @return Children structures
	 */
	public List<VisualStructure> getChildren()
	{
		return childStructures;
	}

	/**
	 * Sets visual structures as children of visual structure
	 * @param childStructures List of visual structures
	 */
	public void setChildren(List<VisualStructure> childStructures)
	{
		this.childStructures = childStructures;
	}

	public List<Separator> getSeparators()
    {
        return separators;
    }

    public Separator getTop()
    {
        return top;
    }

    public void setTop(Separator top)
    {
        this.top = top;
    }

    public Separator getBottom()
    {
        return bottom;
    }

    public void setBottom(Separator bottom)
    {
        this.bottom = bottom;
    }

    public Separator getLeft()
    {
        return left;
    }

    public void setLeft(Separator left)
    {
        this.left = left;
    }

    public Separator getRight()
    {
        return right;
    }

    public void setRight(Separator right)
    {
        this.right = right;
    }

	public void setBounds(Rectangular bounds)
	{
	    this.bounds = new Rectangular(bounds);
	}
	
	public void setX1(int x)
	{
	    bounds.setX1(x);
	}
	
    public void setX2(int x)
    {
        bounds.setX2(x);
    }
    
    public void setY1(int y)
    {
        bounds.setY1(y);
    }
    
    public void setY2(int y)
    {
        bounds.setY2(y);
    }
    
    public int getX1()
    {
        return bounds.getX1();
    }
    
    public int getX2()
    {
        return bounds.getX2();
    }
    
    public int getY1()
    {
        return bounds.getY1();
    }
    
    public int getY2()
    {
        return bounds.getY2();
    }
    
	public Rectangular getBounds()
	{
	    return bounds;
	}

	/**
	 * Sets visual structure's degree of coherence DoC
	 * @param doC Degree of coherence - DoC
	 */
	public void setDoC(int doC)
	{
		this.doC = doC;
	}

	/**
	 * Returns structure's degree of coherence DoC
	 * @return Degree of coherence - DoC
	 */
	public int getDoC()
	{
		return doC;
	}
	
	/**
	 * Concatenates a pair with this structure. Extends the structure and adds the separator.
	 * @param pair
	 * @param knownChildren the existing already known children of this structure. The pair must contain some
	 * of them to be joined
	 * @return
	 */
    public boolean joinPair(SepPair pair, Set<VisualStructure> knownChildren)
    {
        if (knownChildren.contains(pair.a))
        {
            if (pair.separator.isVertical()) //we're on the left
            {
                setX2(pair.b.getX2());
                setRight(pair.b.getRight());
            }
            else //we're above
            {
                setY2(pair.b.getY2());
                setBottom(pair.b.bottom);
            }
            separators.add(pair.separator);
            return true;
        }
        else if (knownChildren.contains(pair.b))
        {
            if (pair.separator.isVertical()) //we're on the right
            {
                setX1(pair.a.getX1());
                setLeft(pair.a.getLeft());
            }
            else //we're below
            {
                setY1(pair.a.getY1());
                setTop(pair.a.getTop());
            }
            separators.add(pair.separator);
            return true;
        }
        else
            return false;
    }
    

    @Override
    public String toString()
    {
        return "[blockRoots=" + blockRoots + ", doC=" + doC + "]";
    }

}
