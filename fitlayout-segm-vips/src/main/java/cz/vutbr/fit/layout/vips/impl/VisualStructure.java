/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructurejava
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents visual structure.
 * @author Tomas Popela
 *
 */
public class VisualStructure 
{
	private List<VipsBlock> blockRoots = null;
	private List<VisualStructure> childStructures = null;
	private List<Separator> horizontalSeparators = null;
	private List<Separator> verticalSeparators = null;
	private int width = 0;
	private int height = 0;
	private int x = 0;
	private int y = 0;
	private int doC = 12;
	private String _id = null;

	public VisualStructure()
	{
		blockRoots = new ArrayList<VipsBlock>();
		childStructures = new ArrayList<VisualStructure>();
		horizontalSeparators = new ArrayList<Separator>();
		verticalSeparators = new ArrayList<Separator>();
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

	/**
	 * Returns all horizontal separators form structure
	 * @return List of horizontal separators
	 */
	public List<Separator> getHorizontalSeparators()
	{
		return horizontalSeparators;
	}

	/**
	 * Sets list of separators as horizontal separators of structure
	 * @param horizontalSeparators List of separators
	 */
	public void setHorizontalSeparators(List<Separator> horizontalSeparators)
	{
		this.horizontalSeparators = horizontalSeparators;
	}

	/**
	 * Adds separator to horizontal separators of structure
	 * @param horizontalSeparator
	 */
	public void addHorizontalSeparator(Separator horizontalSeparator)
	{
		this.horizontalSeparators.add(horizontalSeparator);

	}

	/**
	 * Adds separators to horizontal separators of structure
	 * @param horizontalSeparators
	 */
	public void addHorizontalSeparators(List<Separator> horizontalSeparators)
	{
		this.horizontalSeparators.addAll(horizontalSeparators);

	}

    /**
     * Returns list of all vertical separators in visual structure
     * @return List of vertical separators
     */
    public List<Separator> getVerticalSeparators()
    {
        return verticalSeparators;
    }

    /**
     * Sets list of separators as vertical separators of structure
     * @param _verticalSeparators List of separators
     */
    public void setVerticalSeparators(List<Separator> _verticalSeparators)
    {
        this.verticalSeparators = _verticalSeparators;
    }

    /**
     * Adds separator to structure's vertical sepators
     * @param verticalSeparator
     */
    public void addVerticalSeparator(Separator verticalSeparator)
    {
        this.verticalSeparators.add(verticalSeparator);
    }
    
    /**
     * Adds list of separators to visual structure vertical separators list.
     * @param verticalSeparators
     */
    public void addVerticalSeparators(List<Separator> verticalSeparators)
    {
        this.verticalSeparators.addAll(verticalSeparators);
    }

	/**
	 * Returns X structure's coordinate
	 * @return X coordinate
	 */
	public int getX()
	{
		return this.x;
	}

	/**
	 * Returns structure's Y coordinate
	 * @return Y coordinate
	 */
	public int getY()
	{
		return this.y;
	}

	/**
	 * Sets X coordinate
	 * @param x X coordinate
	 */
	public void setX(int x)
	{
		this.x = x;
	}

	/**
	 * Sets Y coordinate
	 * @param y Y coordinate
	 */
	public void setY(int y)
	{
		this.y = y;
	}

	/**
	 * Sets width of visual structure
	 * @param width Width
	 */
	public void setWidth(int width)
	{
		this.width = width;
	}

	/**
	 * Sets height of visual structure
	 * @param height Height
	 */
	public void setHeight(int height)
	{
		this.height = height;
	}

	/**
	 * Returns width of visual structure
	 * @return Visual structure's width
	 */
	public int getWidth()
	{
		return this.width;
	}

	/**
	 * Returns height of visual structure
	 * @return Visual structure's height
	 */
	public int getHeight()
	{
		return this.height;
	}

	/**
	 * Sets if of visual structure
	 * @param id Id
	 */
	public void setId(String id)
	{
		this._id = id;
	}

	/**
	 * Returns id of visual structure
	 * @return Visual structure's id
	 */
	public String getId()
	{
		return this._id;
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
