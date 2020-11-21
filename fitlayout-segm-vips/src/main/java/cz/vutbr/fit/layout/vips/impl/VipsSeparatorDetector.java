/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorGraphicsDetector.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.vutbr.fit.layout.model.Box.Type;

/**
 * Separator detector with possibility of generating graphics output.
 * @author Tomas Popela
 *
 */
public class VipsSeparatorDetector 
{
	private List<VipsBlock> _visualBlocks = null;
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	
	private int width;
	private int height;

	private int _cleanSeparatorsTreshold = 0;

	/**
	 * Defaults constructor.
	 * @param width Pools width
	 * @param height Pools height
	 */
	public VipsSeparatorDetector(int width, int height) {
	    this.width = width;
	    this.height = height;
		this._horizontalSeparators = new ArrayList<Separator>();
		this._verticalSeparators = new ArrayList<Separator>();
		this._visualBlocks = new ArrayList<VipsBlock>();
	}

	public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

	/**
	 * Creates pool
	 */
	protected void createPool()
	{
	}

	/**
	 * Sets VIPS block, that will be used for separators computing.
	 * @param visualBlocks List of visual blocks
	 */
	public void setVisualBlocks(List<VipsBlock> visualBlocks)
	{
		this._visualBlocks.clear();
		for (VipsBlock block : visualBlocks)
		{
		    //addVisualBlock(block); //TODO? does this have effect?
		    _visualBlocks.add(block);
		}
	}

	/**
	 * Gets VIPS block that is used for separators computing.
	 * @return Visual structure
	 */
	public List<VipsBlock> getVisualBlocks()
	{
		return _visualBlocks;
	}

	/**
	 * Computes vertical visual separators
	 */
	private void findVerticalSeparators()
	{
		for (VipsBlock vipsBlock : _visualBlocks)
		{
			// add new visual block to pool
			addVisualBlock(vipsBlock);

			// block vertical coordinates
			final int blockStart = vipsBlock.getBox().getContentBounds().getX1();
			final int blockEnd = blockStart + vipsBlock.getBox().getContentBounds().getWidth();

			// for each separator that we have in pool
			for (Separator separator : _verticalSeparators)
			{
				// find separator, that intersects with our visual block
				if (blockStart < separator.endPoint)
				{
					// next there are six relations that the separator and visual block can have

					// if separator is inside visual block
					if (blockStart < separator.startPoint && blockEnd >= separator.endPoint)
					{
						List<Separator> tempSeparators = new ArrayList<Separator>();
						tempSeparators.addAll(_verticalSeparators);

						//remove all separators, that are included in block
						for (Separator other : tempSeparators)
						{
							if (blockStart < other.startPoint && blockEnd > other.endPoint)
								_verticalSeparators.remove(other);
						}

						//find separator, that is on end of this block (if exists)
						for (Separator other : _verticalSeparators)
						{
							// and if it's necessary change it's start point
							if (blockEnd > other.startPoint && blockEnd < other.endPoint)
							{
								other.startPoint = blockEnd + 1;
								break;
							}
						}
						break;
					}
					// if block is inside another block -> skip it
					if (blockEnd < separator.startPoint)
						break;
					// if separator starts in the middle of block
					if (blockStart < separator.startPoint && blockEnd >= separator.startPoint)
					{
						// change separator start's point coordinate
						separator.startPoint = blockEnd+1;
						break;
					}
					// if block is inside the separator
					if (blockStart >= separator.startPoint && blockEnd <= separator.endPoint)
					{
						if (blockStart == separator.startPoint)
						{
							separator.startPoint = blockEnd+1;
							break;
						}
						if (blockEnd == separator.endPoint)
						{
							separator.endPoint = blockStart - 1;
							break;
						}
						// add new separator that starts behind the block
						_verticalSeparators.add(_verticalSeparators.indexOf(separator) + 1, new Separator(blockEnd + 1, separator.endPoint));
						// change end point coordinates of separator, that's before block
						separator.endPoint = blockStart - 1;
						break;
					}
					// if in one block is one separator ending and another one starting
					if (blockStart > separator.startPoint && blockStart < separator.endPoint)
					{
						// find the next one
						int nextSeparatorIndex =_verticalSeparators.indexOf(separator);

						// if it's not the last separator
						if (nextSeparatorIndex + 1 < _verticalSeparators.size())
						{
							Separator nextSeparator = _verticalSeparators.get(_verticalSeparators.indexOf(separator) + 1);

							// next separator is really starting before the block ends
							if (blockEnd > nextSeparator.startPoint && blockEnd < nextSeparator.endPoint)
							{
								// change separator start point coordinate
								separator.endPoint = blockStart - 1;
								nextSeparator.startPoint = blockEnd + 1;
								break;
							}
							else
							{
								List<Separator> tempSeparators = new ArrayList<Separator>();
								tempSeparators.addAll(_verticalSeparators);

								//remove all separators, that are included in block
								for (Separator other : tempSeparators)
								{
									if (blockStart < other.startPoint && other.endPoint < blockEnd)
									{
										_verticalSeparators.remove(other);
										continue;
									}
									if (blockEnd > other.startPoint && blockEnd < other.endPoint)
									{
										// change separator start's point coordinate
										other.startPoint = blockEnd+1;
										break;
									}
									if (blockStart > other.startPoint && blockStart < other.endPoint)
									{
										other.endPoint = blockStart-1;
										continue;
									}
								}
								break;
							}
						}
					}
					// if separator ends in the middle of block
					// change it's end point coordinate
					separator.endPoint = blockStart-1;
					break;
				}
			}
		}
	}

	/**
	 * Computes horizontal visual separators
	 */
	private void findHorizontalSeparators()
	{
		for (VipsBlock vipsBlock : _visualBlocks)
		{
			// add new visual block to pool
			addVisualBlock(vipsBlock);

			// block vertical coordinates
			final int blockStart = vipsBlock.getBox().getContentBounds().getY1();
			final int blockEnd = blockStart + vipsBlock.getBox().getContentBounds().getHeight();

			// for each separator that we have in pool
			for (Separator separator : _horizontalSeparators)
			{
				// find separator, that intersects with our visual block
				if (blockStart < separator.endPoint)
				{
					// next there are six relations that the separator and visual block can have

					// if separator is inside visual block
					if (blockStart < separator.startPoint && blockEnd >= separator.endPoint)
					{
						List<Separator> tempSeparators = new ArrayList<Separator>();
						tempSeparators.addAll(_horizontalSeparators);

						//remove all separators, that are included in block
						for (Separator other : tempSeparators)
						{
							if (blockStart < other.startPoint && blockEnd > other.endPoint)
								_horizontalSeparators.remove(other);
						}

						//find separator, that is on end of this block (if exists)
						for (Separator other : _horizontalSeparators)
						{
							// and if it's necessary change it's start point
							if (blockEnd > other.startPoint && blockEnd < other.endPoint)
							{
								other.startPoint = blockEnd + 1;
								break;
							}
						}
						break;
					}
					// if block is inside another block -> skip it
					if (blockEnd < separator.startPoint)
						break;
					// if separator starts in the middle of block
					if (blockStart <= separator.startPoint && blockEnd >= separator.startPoint)
					{
						// change separator start's point coordinate
						separator.startPoint = blockEnd+1;
						break;
					}
					// if block is inside the separator
					if (blockStart >= separator.startPoint && blockEnd < separator.endPoint)
					{
						if (blockStart == separator.startPoint)
						{
							separator.startPoint = blockEnd+1;
							break;
						}
						if (blockEnd == separator.endPoint)
						{
							separator.endPoint = blockStart - 1;
							break;
						}
						// add new separator that starts behind the block
						_horizontalSeparators.add(_horizontalSeparators.indexOf(separator) + 1, new Separator(blockEnd + 1, separator.endPoint));
						// change end point coordinates of separator, that's before block
						separator.endPoint = blockStart - 1;
						break;
					}
					// if in one block is one separator ending and another one starting
					if (blockStart > separator.startPoint && blockStart < separator.endPoint)
					{
						// find the next one
						int nextSeparatorIndex =_horizontalSeparators.indexOf(separator);

						// if it's not the last separator
						if (nextSeparatorIndex + 1 < _horizontalSeparators.size())
						{
							Separator nextSeparator = _horizontalSeparators.get(_horizontalSeparators.indexOf(separator) + 1);

							// next separator is really starting before the block ends
							if (blockEnd > nextSeparator.startPoint && blockEnd < nextSeparator.endPoint)
							{
								// change separator start point coordinate
								separator.endPoint = blockStart - 1;
								nextSeparator.startPoint = blockEnd + 1;
								break;
							}
							else
							{
								List<Separator> tempSeparators = new ArrayList<Separator>();
								tempSeparators.addAll(_horizontalSeparators);

								//remove all separators, that are included in block
								for (Separator other : tempSeparators)
								{
									if (blockStart < other.startPoint && other.endPoint < blockEnd)
									{
										_horizontalSeparators.remove(other);
										continue;
									}
									if (blockEnd > other.startPoint && blockEnd < other.endPoint)
									{
										// change separator start's point coordinate
										other.startPoint = blockEnd+1;
										break;
									}
									if (blockStart > other.startPoint && blockStart < other.endPoint)
									{
										other.endPoint = blockStart-1;
										continue;
									}
								}
								break;
							}
						}
					}
					// if separator ends in the middle of block
					// change it's end point coordinate
					separator.endPoint = blockStart-1;
					break;
				}
			}
		}
	}

	/**
	 * Detects horizontal visual separators from Vips blocks.
	 */
	public void detectHorizontalSeparators()
	{
		if (_visualBlocks.size() == 0)
		{
			System.err.println("I don't have any visual blocks!");
			return;
		}

		createPool();
		_horizontalSeparators.clear();
		_horizontalSeparators.add(new Separator(0, getHeight()));

		findHorizontalSeparators();

		//remove pool borders
		List<Separator> tempSeparators = new ArrayList<Separator>();
		tempSeparators.addAll(_horizontalSeparators);

		for (Separator separator : tempSeparators)
		{
			if (separator.startPoint == 0)
				_horizontalSeparators.remove(separator);
			if (separator.endPoint == getHeight())
				_horizontalSeparators.remove(separator);
		}

		if (_cleanSeparatorsTreshold != 0)
			cleanUpSeparators(_horizontalSeparators);

		computeHorizontalWeights();
		sortSeparatorsByWeight(_horizontalSeparators);
	}

	/**
	 * Detects vertical visual separators from Vips blocks.
	 */
	public void detectVerticalSeparators()
	{
		if (_visualBlocks.size() == 0)
		{
			System.err.println("I don't have any visual blocks!");
			return;
		}

		createPool();
		_verticalSeparators.clear();
		_verticalSeparators.add(new Separator(0, getWidth()));

		findVerticalSeparators();

		//remove pool borders
		List<Separator> tempSeparators = new ArrayList<Separator>();
		tempSeparators.addAll(_verticalSeparators);

		for (Separator separator : tempSeparators)
		{
			if (separator.startPoint == 0)
				_verticalSeparators.remove(separator);
			if (separator.endPoint == getWidth())
				_verticalSeparators.remove(separator);
		}

		if (_cleanSeparatorsTreshold != 0)
			cleanUpSeparators(_verticalSeparators);
		computeVerticalWeights();
		sortSeparatorsByWeight(_verticalSeparators);
	}

	private void cleanUpSeparators(List<Separator> separators)
	{
		List<Separator> tempList = new ArrayList<Separator>();
		tempList.addAll(separators);

		for (Separator separator : tempList)
		{
			int width = separator.endPoint - separator.startPoint + 1;

			if (width < _cleanSeparatorsTreshold)
				separators.remove(separator);
		}
	}

	/**
	 * Sorts given separators by it's weight.
	 * @param separators Separators
	 */
	private void sortSeparatorsByWeight(List<Separator> separators)
	{
		Collections.sort(separators);
	}

	/**
	 * Computes weights for vertical separators.
	 */
	private void computeVerticalWeights()
	{
		for (Separator separator : _verticalSeparators)
		{
			ruleOne(separator);
			ruleTwo(separator, false);
			ruleThree(separator, false);
		}
	}

	/**
	 * Computes weights for horizontal separators.
	 */
	private void computeHorizontalWeights()
	{
		for (Separator separator : _horizontalSeparators)
		{
			ruleOne(separator);
			ruleTwo(separator, true);
			ruleThree(separator,true);
			ruleFour(separator);
			ruleFive(separator);
		}
	}

	/**
	 * The greater the distance between blocks on different
	 * side of the separator, the higher the weight. <p>
	 * For every 10 points of width we increase weight by 1 points.
	 * @param separator Separator
	 */
	private void ruleOne(Separator separator)
	{
		int width = separator.endPoint - separator.startPoint + 1;

		//separator.weight += width;

		if (width > 55 )
			separator.weight += 12;
		if (width > 45 && width <= 55)
			separator.weight += 10;
		if (width > 35 && width <= 45)
			separator.weight += 8;
		if (width > 25 && width <= 35)
			separator.weight += 6;
		else if (width > 15 && width <= 25)
			separator.weight += 4;
		else if (width > 8 && width <= 15)
			separator.weight += 2;
		else
			separator.weight += 1;

	}

	/**
	 * If a visual separator is overlapped with some certain HTML
	 * tags (e.g., the &lt;HR&gt; HTML tag), its weight is set to be higher.
	 * @param separator Separator
	 */
	private void ruleTwo(Separator separator, boolean horizontal)
	{
		List<VipsBlock> overlappedElements = new ArrayList<VipsBlock>();
		if (horizontal)
			findHorizontalOverlappedElements(separator, overlappedElements);
		else
			findVerticalOverlappedElements(separator, overlappedElements);

		if (overlappedElements.size() == 0)
			return;

		for (VipsBlock vipsBlock : overlappedElements)
		{
			if ("hr".equalsIgnoreCase(vipsBlock.getBox().getTagName()))
			{
				separator.weight += 2;
				break;
			}
		}
	}

	/**
	 * Finds elements that are overlapped with horizontal separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param result Elements, that we found
	 */
	private void findHorizontalOverlappedElements(Separator separator, List<VipsBlock> result)
	{
		for (VipsBlock vipsBlock : _visualBlocks)
		{
			int topEdge = vipsBlock.getBox().getContentBounds().getY1();
			int bottomEdge = topEdge + vipsBlock.getBox().getContentBounds().getHeight();

			// two upper edges of element are overlapped with separator
			if (topEdge > separator.startPoint && topEdge < separator.endPoint && bottomEdge > separator.endPoint)
			{
				result.add(vipsBlock);
			}

			// two bottom edges of element are overlapped with separator
			if (topEdge < separator.startPoint && bottomEdge > separator.startPoint && bottomEdge < separator.endPoint)
			{
				result.add(vipsBlock);
			}

			// all edges of element are overlapped with separator
			if (topEdge >= separator.startPoint && bottomEdge <= separator.endPoint)
			{
				result.add(vipsBlock);
			}

		}
	}

	/**
	 * Finds elements that are overlapped with vertical separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param result Elements, that we found
	 */
	private void findVerticalOverlappedElements(Separator separator, List<VipsBlock> result)
	{
		for (VipsBlock vipsBlock : _visualBlocks)
		{
			final int leftEdge = vipsBlock.getBox().getContentBounds().getX1();
			final int rightEdge = leftEdge + vipsBlock.getBox().getContentBounds().getWidth();

			// two left edges of element are overlapped with separator
			if (leftEdge > separator.startPoint && leftEdge < separator.endPoint && rightEdge > separator.endPoint)
			{
				result.add(vipsBlock);
			}

			// two right edges of element are overlapped with separator
			if (leftEdge < separator.startPoint && rightEdge > separator.startPoint && rightEdge < separator.endPoint)
			{
				result.add(vipsBlock);
			}

			// all edges of element are overlapped with separator
			if (leftEdge >= separator.startPoint && rightEdge <= separator.endPoint)
			{
				result.add(vipsBlock);
			}
		}
	}

	/**
	 * If background colors of the blocks on two sides of the separator
	 * are different, the weight will be increased.
	 * @param separator Separator
	 */
	private void ruleThree(Separator separator, boolean horizontal)
	{
		// for vertical is represents elements on left side
		List<VipsBlock> topAdjacentElements = new ArrayList<VipsBlock>();
		// for vertical is represents elements on right side
		List<VipsBlock> bottomAdjacentElements = new ArrayList<VipsBlock>();
		if (horizontal)
			findHorizontalAdjacentBlocks(separator, topAdjacentElements, bottomAdjacentElements);
		else
			findVerticalAdjacentBlocks(separator, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		boolean weightIncreased = false;

		for (VipsBlock top : topAdjacentElements)
		{
			for (VipsBlock bottom : bottomAdjacentElements)
			{
				if (!top.getBgColor().equals(bottom.getBgColor()))
				{
					separator.weight += 2;
					weightIncreased = true;
					break;
				}
			}
			if (weightIncreased)
				break;
		}
	}

	/**
	 * Finds elements that are adjacent to horizontal separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param resultTop Elements, that we found on top side of separator
	 * @param resultBottom Elements, that we found on bottom side side of separator
	 */
	private void findHorizontalAdjacentBlocks(Separator separator, List<VipsBlock> resultTop, List<VipsBlock> resultBottom)
	{
		for (VipsBlock vipsBlock : _visualBlocks)
		{
			int topEdge = vipsBlock.getBox().getContentBounds().getY1();
			int bottomEdge = topEdge + vipsBlock.getBox().getContentBounds().getHeight();

			// if box is adjancent to separator from bottom
			if (topEdge == separator.endPoint + 1 && bottomEdge > separator.endPoint + 1)
			{
				resultBottom.add(vipsBlock);
			}

			// if box is adjancent to separator from top
			if (bottomEdge == separator.startPoint - 1 && topEdge < separator.startPoint - 1)
			{
				resultTop.add(0, vipsBlock);
			}
		}
	}

	/**
	 * Finds elements that are adjacent to vertical separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param resultLeft Elements, that we found on left side of separator
	 * @param resultRight Elements, that we found on right side side of separator
	 */
	private void findVerticalAdjacentBlocks(Separator separator, List<VipsBlock> resultLeft, List<VipsBlock> resultRight)
	{
		for (VipsBlock vipsBlock : _visualBlocks)
		{
			final int leftEdge = vipsBlock.getBox().getContentBounds().getX1() + 1;
			final int rightEdge = leftEdge + vipsBlock.getBox().getContentBounds().getWidth();

			// if box is adjancent to separator from right
			if (leftEdge == separator.endPoint + 1 && rightEdge > separator.endPoint + 1)
			{
				resultRight.add(vipsBlock);
			}

			// if box is adjancent to separator from left
			if (rightEdge == separator.startPoint - 1 && leftEdge < separator.startPoint - 1)
			{
				resultLeft.add(0, vipsBlock);
			}
		}
	}

	/**
	 * For horizontal separators, if the differences of font properties
	 * such as font size and font weight are bigger on two
	 * sides of the separator, the weight will be increased.
	 * Moreover, the weight will be increased if the font size of the block
	 * above the separator is smaller than the font size of the block
	 * below the separator.
	 * @param separator Separator
	 */
	private void ruleFour(Separator separator)
	{
		List<VipsBlock> topAdjacentElements = new ArrayList<VipsBlock>();
		List<VipsBlock> bottomAdjacentElements = new ArrayList<VipsBlock>();

		findHorizontalAdjacentBlocks(separator, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		boolean weightIncreased = false;

		for (VipsBlock top : topAdjacentElements)
		{
			for (VipsBlock bottom : bottomAdjacentElements)
			{
				int diff = Math.abs(top.getFontSize() - bottom.getFontSize());
				if (diff != 0)
				{
					separator.weight += 2;
					weightIncreased = true;
					break;
				}
				else
				{
					if (!top.getFontWeight().equals(bottom.getFontWeight()))
					{
						separator.weight += 2;
					}
				}
			}
			if (weightIncreased)
				break;
		}

		weightIncreased = false;

		for (VipsBlock top : topAdjacentElements)
		{
			for (VipsBlock bottom : bottomAdjacentElements)
			{
				if (top.getFontSize() < bottom.getFontSize())
				{
					separator.weight += 2;
					weightIncreased = true;
					break;
				}
			}
			if (weightIncreased)
				break;
		}
	}

	/**
	 * For horizontal separators, when the structures of the blocks on the two
	 * sides of the separator are very similar (e.g. both are text),
	 * the weight of the separator will be decreased.
	 * @param separator Separator
	 */
	private void ruleFive(Separator separator)
	{
		List<VipsBlock> topAdjacentElements = new ArrayList<VipsBlock>();
		List<VipsBlock> bottomAdjacentElements = new ArrayList<VipsBlock>();

		findHorizontalAdjacentBlocks(separator, topAdjacentElements, bottomAdjacentElements);

		if (topAdjacentElements.size() < 1 || bottomAdjacentElements.size() < 1)
			return;

		boolean weightDecreased = false;

		for (VipsBlock top : topAdjacentElements)
		{
			for (VipsBlock bottom : bottomAdjacentElements)
			{
				if (top.getBox().getType() == Type.TEXT_CONTENT && bottom.getBox().getType() == Type.TEXT_CONTENT)
				{
					separator.weight -= 2;
					weightDecreased = true;
					break;
				}
			}
			if (weightDecreased)
				break;
		}
	}

	/**
	 * @return the _horizontalSeparators
	 */
	public List<Separator> getHorizontalSeparators()
	{
		return _horizontalSeparators;
	}

	public void setHorizontalSeparators(List<Separator> separators)
	{
		_horizontalSeparators.clear();
		_horizontalSeparators.addAll(separators);
	}

	public void setVerticalSeparators(List<Separator> separators)
	{
		_verticalSeparators.clear();
		_verticalSeparators.addAll(separators);
	}

	/**
	 * @return the _verticalSeparators
	 */
	public List<Separator> getVerticalSeparators()
	{
		return _verticalSeparators;
	}

	public void setCleanUpSeparators(int treshold)
	{
		this._cleanSeparatorsTreshold = treshold;
	}

	public boolean isCleanUpEnabled()
	{
		if (_cleanSeparatorsTreshold == 0)
			return true;

		return false;
	}

    public void addVisualBlock(VipsBlock vipsBlock)
    {
        //used only for graphics
    }


}
