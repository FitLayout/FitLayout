/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsBlock.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A class that represents a block on the page.
 * @author Tomas Popela
 * @author burgetr
 */
public class VipsBlock 
{
	//rendered Box, that corresponds to DOM element
	private Box box = null;
	//children of this node
	private List<VipsBlock> children = null;
	//node id
	private int _id = 0;
	//node's Degree Of Coherence
	private int _DoC = 0;

	//if node is visual block
	private boolean visualBlock = false;
	//if node was already divided
	private boolean alreadyDivided = false;
	//if node can be divided
	private boolean dividable = true;

	private String bgColor = null;

	private int frameSourceIndex = 0;


	public VipsBlock() 
	{
		this.children = new ArrayList<VipsBlock>();
	}

	public VipsBlock(int id, VipsBlock node) 
	{
		this.children = new ArrayList<VipsBlock>();
		setId(id);
		addChild(node);
	}

	/**
	 * Sets block as visual block
	 * @param isVisualBlock Value
	 */
	public void setIsVisualBlock(boolean isVisualBlock)
	{
		visualBlock = isVisualBlock;
	}

	/**
	 * Checks if block is visual block
	 * @return True if block if visual block, otherwise false
	 */
	public boolean isVisualBlock()
	{
		return visualBlock;
	}

	/**
	 * Adds new child to blocks children
	 * @param child New child
	 */
	public void addChild(VipsBlock child)
	{
		children.add(child);
	}

	/**
	 * Gets all blocks children
	 * @return List of children
	 */
	public List<VipsBlock> getChildren()
	{
		return children;
	}

	/**
	 * Sets block corresponding Box
	 * @param box Box
	 */
	public void setBox(Box box)
	{
		this.box = box;
	}

	/**
	 * Gets Box corresponding to the block
	 * @return Box
	 */
	public Box getBox()
	{
		return box;
	}

	/**
	 * Sets block's id
	 * @param id Id
	 */
	public void setId(int id)
	{
		this._id = id;
	}

	/**
	 * Gets blocks id
	 * @return Id
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * Returns block's degree of coherence DoC
	 * @return Degree of coherence
	 */
	public int getDoC()
	{
		return _DoC;
	}

	/**
	 * Sets block;s degree of coherence
	 * @param doC Degree of coherence
	 */
	public void setDoC(int doC)
	{
		this._DoC = doC;
	}

	/**
	 * Checks if block is dividable
	 * @return True if is dividable, otherwise false
	 */
	public boolean isDividable()
	{
		return dividable;
	}

	/**
	 * Sets dividability of block
	 * @param isDividable True if is dividable otherwise false
	 */
	public void setIsDividable(boolean isDividable)
	{
		this.dividable = isDividable;
	}

	/**
	 * Checks if node was already divided
	 * @return True if was divided, otherwise false
	 */
	public boolean isAlreadyDivided()
	{
		return alreadyDivided;
	}

	/**
	 * Sets if block was divided
	 * @param alreadyDivided True if block was divided, otherwise false
	 */
	public void setAlreadyDivided(boolean alreadyDivided)
	{
		this.alreadyDivided = alreadyDivided;
	}

	/**
	 * Gets the visual bounds of the block.
	 * @return the visual bounds
	 */
	public Rectangular getBounds()
	{
	    return getBox().getVisualBounds();
	}
	
	/**
	 * Finds background color of element
	 * @param element Element
	 */
	private String findBgColor(Box element)
	{
		String backgroundColor = Utils.colorString(element.getBackgroundColor());
		if (backgroundColor.isEmpty())
		{
			if (element.getParent() != null && !element.getTagName().equals("body"))
			{
				return findBgColor(element.getParent());
			}
			else
			{
				return "#ffffff";
			}
		}
		else
		{
			return backgroundColor;
		}
	}

	/**
	 * Gets background color of element
	 * @return Background color
	 */
	public String getBgColor()
	{
		if (bgColor == null)
    		bgColor = findBgColor(getBox());
		return bgColor;
	}

	/**
	 * Gets block's font size
	 * @return Font size
	 */
	public int getFontSize()
	{
		return Math.round(this.getBox().getTextStyle().getFontSize());
	}

	/**
	 * Gets block's font weight
	 * @return Font weight
	 */
	public String getFontWeight()
	{
	    return Utils.fontWeight(getBox());
	}

	/**
	 * Gets frame source index of block
	 * @return Frame source index
	 */
	public int getFrameSourceIndex()
	{
		return frameSourceIndex;
	}

    @Override
    public String toString()
    {
        String ret = isVisualBlock() ? "*[":"[";
        if (getBox() != null)
            ret += getBox().toString();
        ret += "]";
        ret += "(DoC=" + getDoC() + ")";
        return ret;
    }

}
