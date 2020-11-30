/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructurejava
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.List;

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
    private Rectangular bounds;
	private int doC = 12;

	public VisualStructure()
	{
		blockRoots = new ArrayList<>();
		childStructures = new ArrayList<>();
		bounds = new Rectangular();
	}

    public VisualStructure(VisualStructure src)
    {
        this();
        bounds = new Rectangular(src.bounds);
        doC = src.doC;
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
	 * Checks whether the area is empty (it contains no visual blocks)
	 * @return {@code true} when the area is empty
	 */
	public boolean isEmpty()
	{
	    return blockRoots.isEmpty();
	}
	
    /**
     * Adds new child to visual structure children
     * @param child New child
     */
    public void addChild(VisualStructure child)
    {
        childStructures.add(child);
    }

	public void addChildren(List<VisualStructure> children)
	{
	    for (VisualStructure child : children)
	        addChild(child);
	}
	
    /**
     * Removes given child from structures children
     * @param child Child
     */
    public void removeChild(VisualStructure child)
    {
        this.childStructures.remove(child);
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

	public void setSeparators(List<Separator> separators)
    {
        this.separators = separators;
    }

    public List<Separator> getSeparators()
    {
        return separators;
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
	
    @Override
    public String toString()
    {
        return "[blockRoots=" + blockRoots + ", doC=" + doC + "]";
    }

}
