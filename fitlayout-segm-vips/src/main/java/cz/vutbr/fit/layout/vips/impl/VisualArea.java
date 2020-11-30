/**
 * VIPS - Visual Internet Page Segmentation for FitLayout
 * 
 * Tomas Popela, 2012
 * Radek Burget, 2020 
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A class that represents a visual area in the resulting constructed visual structure.
 * 
 * @author Tomas Popela
 * @author burgetr
 */
public class VisualArea 
{
	private List<VisualBlock> blockRoots;
	private List<VisualArea> childStructures;
	private List<Separator> separators;
    private Rectangular bounds;
	private int doC = 12;

	public VisualArea()
	{
		blockRoots = new ArrayList<>();
		childStructures = new ArrayList<>();
		bounds = new Rectangular();
	}

    public VisualArea(VisualArea src)
    {
        this();
        bounds = new Rectangular(src.bounds);
        doC = src.doC;
    }
	
	/**
	 * Gets the visual blocks contained in this area.
	 *  
	 * @return A list of visual blocks.
	 */
	public List<VisualBlock> getBlockRoots()
	{
		return blockRoots;
	}

	/**
	 * Adds a block to contained blocks.
	 * 
	 * @param blockRoot New block
	 */
	public void addBlock(VisualBlock blockRoot)
	{
		blockRoots.add(blockRoot);
	}

	/**
	 * Sets the blocks contained in this area.
	 * 
	 * @param vipsBlocks a list of the block root nodes
	 */
	public void setBlockRoots(List<VisualBlock> vipsBlocks)
	{
		this.blockRoots = vipsBlocks;
	}

	/**
	 * Checks whether the area is empty (it contains no visual blocks).
	 * 
	 * @return {@code true} when the area is empty
	 */
	public boolean isEmpty()
	{
	    return blockRoots.isEmpty();
	}
	
    /**
     * Adds a new child area to this area.
     * 
     * @param child New child area
     */
    public void addChild(VisualArea child)
    {
        childStructures.add(child);
    }

	/**
	 * Adds a set of children to the area.
	 * 
	 * @param children
	 */
    public void addChildren(List<VisualArea> children)
	{
	    for (VisualArea child : children)
	        addChild(child);
	}
	
	/**
	 * Gets all child areas.
	 * 
	 * @return A list of child areas.
	 */
	public List<VisualArea> getChildren()
	{
		return childStructures;
	}

	/**
	 * Sets a list of separators contained in this area.
	 * 
	 * @param separators the new list of separators
	 */
	public void setSeparators(List<Separator> separators)
    {
        this.separators = separators;
    }

    /**
     * Gets a list of separators contained in this area.
     * 
     * @return a list of separators
     */
	public List<Separator> getSeparators()
    {
        return separators;
    }

	/**
	 * Sets the visual area bounds.
	 * 
	 * @param bounds the new bounds.
	 */
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
