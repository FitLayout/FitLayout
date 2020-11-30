/**
 * VIPS - Visual Internet Page Segmentation for FitLayout
 * 
 * Tomas Popela, 2012
 * Radek Burget, 2020 
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * An extracted visual block in the page.
 * 
 * @author Tomas Popela
 * @author burgetr
 */
public class VisualBlock 
{
    //root of the whole tree
    private VisualBlock root;
	//rendered Box, that corresponds to DOM element
	private Box box = null;
	//children of this node
	private List<VisualBlock> children = null;
	//node's Degree Of Coherence
	private int doC = 0;

	//if node is visual block
	private boolean visualBlock = false;
	//if node was already divided
	private boolean alreadyDivided = false;
	//if node can be divided
	private boolean dividable = true;
	//background color of the block
	private String bgColor = null;


	public VisualBlock() 
	{
		this.children = new ArrayList<VisualBlock>();
	}

	/**
	 * Resets the block parametres to consider the block again.
	 */
	public void reset()
	{
	    visualBlock = false;
	    alreadyDivided = false;
	    dividable = true;
	    doC = 0;
	    children.clear();
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
	public void addChild(VisualBlock child)
	{
		children.add(child);
	}

	/**
	 * Gets all blocks children
	 * @return List of children
	 */
	public List<VisualBlock> getChildren()
	{
		return children;
	}

	public VisualBlock getRoot()
    {
        return root;
    }

    public void setRoot(VisualBlock root)
    {
        this.root = root;
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
	 * Returns block's degree of coherence DoC
	 * @return Degree of coherence
	 */
	public int getDoC()
	{
		return doC;
	}

	/**
	 * Sets block;s degree of coherence
	 * @param doC Degree of coherence
	 */
	public void setDoC(int doC)
	{
		this.doC = doC;
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
