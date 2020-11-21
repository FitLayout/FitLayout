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
	private List<VipsBlock> _nestedBlocks = null;
	private List<VisualStructure> _childrenVisualStructures = null;
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	private int _width = 0;
	private int _height = 0;
	private int _x = 0;
	private int _y = 0;
	private int _doC = 12;
	private String _id = null;
	private int _minimalDoC = 0;

	public VisualStructure()
	{
		_nestedBlocks = new ArrayList<VipsBlock>();
		_childrenVisualStructures = new ArrayList<VisualStructure>();
		_horizontalSeparators = new ArrayList<Separator>();
		_verticalSeparators = new ArrayList<Separator>();
	}

	/**
	 * @return Nested blocks in structure
	 */
	public List<VipsBlock> getNestedBlocks()
	{
		return _nestedBlocks;
	}

	/**
	 * Adds block to nested blocks
	 * @param nestedBlock New block
	 */
	public void addNestedBlock(VipsBlock nestedBlock)
	{
		this._nestedBlocks.add(nestedBlock);
	}

	/**
	 * Adds blocks to nested blocks
	 * @param nestedBlocks
	 */
	public void addNestedBlocks(List<VipsBlock> nestedBlocks)
	{
		this._nestedBlocks.addAll(nestedBlocks);
	}

	/**
	 * Sets blocks as nested blocks
	 * @param vipsBlocks
	 */
	public void setNestedBlocks(List<VipsBlock> vipsBlocks)
	{
		this._nestedBlocks = vipsBlocks;
	}

	/**
	 * Clears nested blocks list
	 */
	public void clearNestedBlocks()
	{
		this._nestedBlocks.clear();
	}

	/**
	 * Removes nested block at given index
	 * @param index Index of block
	 */
	public void removeNestedBlockAt(int index)
	{
		this._nestedBlocks.remove(index);
	}

	/**
	 * Removes given child from structures children
	 * @param visualStructure Child
	 */
	public void removeChild(VisualStructure visualStructure)
	{
		this._childrenVisualStructures.remove(visualStructure);
	}

	/**
	 * Adds new child to visual structure children
	 * @param visualStructure New child
	 */
	public void addChild(VisualStructure visualStructure)
	{
		this._childrenVisualStructures.add(visualStructure);
	}

	/**
	 * Adds new child to visual structure at given index
	 * @param visualStructure New child
	 * @param index Index
	 */
	public void addChildAt(VisualStructure visualStructure, int index)
	{
		this._childrenVisualStructures.add(index, visualStructure);
	}

	/**
	 * Returns all children structures
	 * @return Children structures
	 */
	public List<VisualStructure> getChildrenVisualStructures()
	{
		return _childrenVisualStructures;
	}

	/**
	 * Sets visual structures as children of visual structure
	 * @param childrenVisualStructures List of visual structures
	 */
	public void setChildrenVisualStructures(List<VisualStructure> childrenVisualStructures)
	{
		this._childrenVisualStructures = childrenVisualStructures;
	}

	/**
	 * Returns all horizontal separators form structure
	 * @return List of horizontal separators
	 */
	public List<Separator> getHorizontalSeparators()
	{
		return _horizontalSeparators;
	}

	/**
	 * Sets list of separators as horizontal separators of structure
	 * @param horizontalSeparators List of separators
	 */
	public void setHorizontalSeparators(List<Separator> horizontalSeparators)
	{
		this._horizontalSeparators = horizontalSeparators;
	}

	/**
	 * Adds separator to horizontal separators of structure
	 * @param horizontalSeparator
	 */
	public void addHorizontalSeparator(Separator horizontalSeparator)
	{
		this._horizontalSeparators.add(horizontalSeparator);

	}

	/**
	 * Adds separators to horizontal separators of structure
	 * @param horizontalSeparators
	 */
	public void addHorizontalSeparators(List<Separator> horizontalSeparators)
	{
		this._horizontalSeparators.addAll(horizontalSeparators);

	}

	/**
	 * Returns X structure's coordinate
	 * @return X coordinate
	 */
	public int getX()
	{
		return this._x;
	}

	/**
	 * Returns structure's Y coordinate
	 * @return Y coordinate
	 */
	public int getY()
	{
		return this._y;
	}

	/**
	 * Sets X coordinate
	 * @param x X coordinate
	 */
	public void setX(int x)
	{
		this._x = x;
	}

	/**
	 * Sets Y coordinate
	 * @param y Y coordinate
	 */
	public void setY(int y)
	{
		this._y = y;
	}

	/**
	 * Sets width of visual structure
	 * @param width Width
	 */
	public void setWidth(int width)
	{
		this._width = width;
	}

	/**
	 * Sets height of visual structure
	 * @param height Height
	 */
	public void setHeight(int height)
	{
		this._height = height;
	}

	/**
	 * Returns width of visual structure
	 * @return Visual structure's width
	 */
	public int getWidth()
	{
		return this._width;
	}

	/**
	 * Returns height of visual structure
	 * @return Visual structure's height
	 */
	public int getHeight()
	{
		return this._height;
	}

	/**
	 * Returns list of all vertical separators in visual structure
	 * @return List of vertical separators
	 */
	public List<Separator> getVerticalSeparators()
	{
		return _verticalSeparators;
	}

	/**
	 * Sets list of separators as vertical separators of structure
	 * @param _verticalSeparators List of separators
	 */
	public void setVerticalSeparators(List<Separator> _verticalSeparators)
	{
		this._verticalSeparators = _verticalSeparators;
	}

	/**
	 * Adds separator to structure's vertical sepators
	 * @param verticalSeparator
	 */
	public void addVerticalSeparator(Separator verticalSeparator)
	{
		this._verticalSeparators.add(verticalSeparator);
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
		this._doC = doC;
	}

	/**
	 * Returns structure's degree of coherence DoC
	 * @return Degree of coherence - DoC
	 */
	public int getDoC()
	{
		return _doC;
	}

	/**
	 * Finds minimal DoC in all children visual structures
	 * @param visualStructure Given visual structure
	 */
	private void findMinimalDoC(VisualStructure visualStructure)
	{
		if (!visualStructure.getId().equals("1"))
		{
			if (visualStructure.getDoC() < _minimalDoC)
				_minimalDoC = visualStructure.getDoC();
		}

		for (VisualStructure child : visualStructure.getChildrenVisualStructures())
		{
			findMinimalDoC(child);
		}
	}

	/**
	 * Updates DoC to normalized DoC
	 */
	public void updateToNormalizedDoC()
	{
		_doC = 12;

		for (Separator separator : _horizontalSeparators)
		{
			if (separator.normalizedWeight < _doC)
				_doC = separator.normalizedWeight;
		}

		for (Separator separator : _verticalSeparators)
		{
			if (separator.normalizedWeight < _doC)
				_doC = separator.normalizedWeight;
		}

		if (_doC == 12)
		{
			for (VipsBlock nestedBlock : _nestedBlocks)
			{
				if (nestedBlock.getDoC() < _doC)
					_doC = nestedBlock.getDoC();
			}
		}

		_minimalDoC = 12;

		findMinimalDoC(this);

		if (_minimalDoC < _doC)
			_doC = _minimalDoC;
	}

	/**
	 * Adds list of separators to visual structure vertical separators list.
	 * @param verticalSeparators
	 */
	public void addVerticalSeparators(List<Separator> verticalSeparators)
	{
		this._verticalSeparators.addAll(verticalSeparators);
	}
}
