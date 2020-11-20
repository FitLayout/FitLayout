/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsBlock.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.layout.model.Box;

/**
 * Class that represents block on page.
 * @author Tomas Popela
 *
 */
public class VipsBlock {

	//rendered Box, that corresponds to DOM element
	private Box _box = null;
	//children of this node
	private List<VipsBlock> _children = null;
	//node id
	private int _id = 0;
	//node's Degree Of Coherence
	private int _DoC = 0;

	//number of images in node
	private int _containImg = 0;
	//if node is image
	private boolean _isImg = false;
	//if node is visual block
	private boolean _isVisualBlock = false;
	//if node contains table
	private boolean _containTable = false;
	//number of paragraphs in node
	private int _containP = 0;
	//if node was already divided
	private boolean _alreadyDivided = false;
	//if node can be divided
	private boolean _isDividable = true;
	private boolean preventDivision = false;

	private String _bgColor = null;

	private int _frameSourceIndex = 0;
	private int _sourceIndex = 0;
	private int _order = 0;


	//length of text in node
	private int _textLen = 0;
	//length of text in links in node
	private int _linkTextLen = 0;

	public VipsBlock() {
		this._children = new ArrayList<VipsBlock>();
	}

	public VipsBlock(int id, VipsBlock node) {
		this._children = new ArrayList<VipsBlock>();
		setId(id);
		addChild(node);
	}

	/**
	 * Sets block as visual block
	 * @param isVisualBlock Value
	 */
	public void setIsVisualBlock(boolean isVisualBlock)
	{
		_isVisualBlock = isVisualBlock;
		checkProperties();
	}

	/**
	 * Checks if block is visual block
	 * @return True if block if visual block, otherwise false
	 */
	public boolean isVisualBlock()
	{
		return _isVisualBlock;
	}

	public boolean isPreventDivision()
    {
        return preventDivision;
    }

    public void setPreventDivision(boolean preventDivision)
    {
        this.preventDivision = preventDivision;
    }

    /**
	 * Checks the properties of visual block
	 */
	private void checkProperties()
	{
		checkIsImg();
		checkContainImg(this);
		checkContainTable(this);
		checkContainP(this);
		_linkTextLen = 0;
		_textLen = 0;
		countTextLength(this);
		countLinkTextLength(this);
		_sourceIndex = _box.getOrder();
	}

	/**
	 * Checks if visual block is an image.
	 */
	private void checkIsImg()
	{
        if ("img".equalsIgnoreCase(getBox().getTagName()))
			_isImg = true;
		else
			_isImg = false;
	}

	/**
	 * Checks if visual block contains image.
	 * @param vipsBlock Visual block
	 */
	private void checkContainImg(VipsBlock vipsBlock)
	{
        if ("img".equalsIgnoreCase(vipsBlock.getBox().getTagName()))
		{
			vipsBlock._isImg = true;
			this._containImg++;
		}

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			checkContainImg(childVipsBlock);
	}

	/**
	 * Checks if visual block contains table.
	 * @param vipsBlock Visual block
	 */
	private void checkContainTable(VipsBlock vipsBlock)
	{
        if ("table".equalsIgnoreCase(vipsBlock.getBox().getTagName()))
			this._containTable = true;

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			checkContainTable(childVipsBlock);
	}

	/**
	 * Checks if visual block contains paragraph.
	 * @param vipsBlock Visual block
	 */
	private void checkContainP(VipsBlock vipsBlock)
	{
		if ("p".equalsIgnoreCase(vipsBlock.getBox().getTagName()))
			this._containP++;

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			checkContainP(childVipsBlock);
	}

	/**
	 * Counts length of text in links in visual block
	 * @param vipsBlock Visual block
	 */
	private void countLinkTextLength(VipsBlock vipsBlock)
	{
        if ("a".equalsIgnoreCase(vipsBlock.getBox().getTagName()))
		{
            if (vipsBlock.getBox().getOwnText() != null)
                _linkTextLen += vipsBlock.getBox().getOwnText().length();
		}

		for (VipsBlock childVipsBlock : vipsBlock.getChildren())
			countLinkTextLength(childVipsBlock);
	}

	/**
	 * Count length of text in visual block
	 * @param vipsBlock Visual block
	 */
	private void countTextLength(VipsBlock vipsBlock)
	{
	    if (vipsBlock.getBox().getOwnText() != null)
	        _textLen = vipsBlock.getBox().getOwnText().replaceAll("\n", "").length();
	    else
	        _textLen = 0;
	}

	/**
	 * Adds new child to blocks children
	 * @param child New child
	 */
	public void addChild(VipsBlock child)
	{
		_children.add(child);
	}

	/**
	 * Gets all blocks children
	 * @return List of children
	 */
	public List<VipsBlock> getChildren()
	{
		return _children;
	}

	/**
	 * Sets block corresponding Box
	 * @param box Box
	 */
	public void setBox(Box box)
	{
		this._box = box;
	}

	/**
	 * Gets Box corresponding to the block
	 * @return Box
	 */
	public Box getBox()
	{
		return _box;
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
		return _isDividable;
	}

	/**
	 * Sets dividability of block
	 * @param isDividable True if is dividable otherwise false
	 */
	public void setIsDividable(boolean isDividable)
	{
		this._isDividable = isDividable;
	}

	/**
	 * Checks if node was already divided
	 * @return True if was divided, otherwise false
	 */
	public boolean isAlreadyDivided()
	{
		return _alreadyDivided;
	}

	/**
	 * Sets if block was divided
	 * @param alreadyDivided True if block was divided, otherwise false
	 */
	public void setAlreadyDivided(boolean alreadyDivided)
	{
		this._alreadyDivided = alreadyDivided;
	}

	/**
	 * Checks if block is image
	 * @return True if block is image, otherwise false
	 */
	public boolean isImg()
	{
		return _isImg;
	}

	/**
	 * Checks if block contain images
	 * @return Number of images
	 */
	public int containImg()
	{
		return _containImg;
	}

	/**
	 * Checks if block contains table
	 * @return True if block contains table, otherwise false
	 */
	public boolean containTable()
	{
		return _containTable;
	}

	/**
	 * Gets length of text in block
	 * @return Length of text
	 */
	public int getTextLength()
	{
		return _textLen;
	}

	/**
	 * Gets length of text in links in block
	 * @return Length of links text
	 */
	public int getLinkTextLength()
	{
		return _linkTextLen;
	}

	/**
	 * Gets number of paragraphs in block
	 * @return Number of paragraphs
	 */
	public int containP()
	{
		return _containP;
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
		if (_bgColor == null)
    		_bgColor = findBgColor(getBox());
		return _bgColor;
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
		return _frameSourceIndex;
	}

	/**
	 * Gets source index of block
	 * @return Block's source index
	 */
	public int getSourceIndex()
	{
		return _sourceIndex;
	}

	/**
	 * Gets order of block
	 * @return Block's order
	 */
	public int getOrder()
	{
		return _order;
	}

    @Override
    public String toString()
    {
        String ret = isVisualBlock() ? "[":"![";
        if (getBox() != null)
            ret += getBox().toString();
        ret += "]";
        return ret;
    }

}
