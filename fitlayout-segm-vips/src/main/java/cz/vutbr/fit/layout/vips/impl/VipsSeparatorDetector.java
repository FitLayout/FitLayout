/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorGraphicsDetector.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cz.vutbr.fit.layout.model.Box.Type;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A base separator detector implementation.
 * 
 * @author Tomas Popela
 * @author burgetr
 */
public class VipsSeparatorDetector 
{
	private List<VipsBlock> visualBlocks;
	private List<Separator> horizontalSeparators;
	private List<Separator> verticalSeparators;
	private Rectangular pageBounds;

	/**
	 * Creates the separator detector from a list of visual blocks.
	 * 
	 * @param visualBlocks the visual blocks to consider
	 * @param pageBounds the sub-page bounds
	 */
	public VipsSeparatorDetector(List<VipsBlock> visualBlocks, Rectangular pageBounds) 
	{
	    this.pageBounds = pageBounds;
        this.visualBlocks = visualBlocks;
		this.horizontalSeparators = new ArrayList<>();
		this.verticalSeparators = new ArrayList<>();
	}

	/**
	 * Sets VIPS block, that will be used for separators computing.
	 * @param visualBlocks List of visual blocks
	 */
	public void setVisualBlocks(List<VipsBlock> visualBlocks)
	{
		this.visualBlocks.clear();
		for (VipsBlock block : visualBlocks)
		{
		    this.visualBlocks.add(block);
		}
	}

	/**
	 * Gets VIPS block that is used for separators computing.
	 * @return Visual structure
	 */
	public List<VipsBlock> getVisualBlocks()
	{
		return visualBlocks;
	}

    /**
     * Detects horizontal visual separators from Vips blocks.
     * @return a list of detected separators
     */
    public List<Separator> detectHorizontalSeparators()
    {
        horizontalSeparators.clear();
        if (visualBlocks.size() > 0)
        {
            horizontalSeparators.add(new Separator(pageBounds.getY1(), pageBounds.getY2(), false));
            findHorizontalSeparators();
            removeBorderSeparators(horizontalSeparators, pageBounds.getY1(), pageBounds.getY2());
            computeHorizontalWeights();
            sortSeparatorsByWeight(horizontalSeparators);
        }       
        return horizontalSeparators;
    }

    /**
     * Detects vertical visual separators from Vips blocks.
     * @return 
     */
    public List<Separator> detectVerticalSeparators()
    {
        verticalSeparators.clear();
        if (visualBlocks.size() > 0)
        {
            verticalSeparators.add(new Separator(pageBounds.getX1(), pageBounds.getX2(), true));
            findVerticalSeparators();
            removeBorderSeparators(verticalSeparators, pageBounds.getX1(), pageBounds.getX2());
            computeVerticalWeights();
            sortSeparatorsByWeight(verticalSeparators);
        }
        return verticalSeparators;
    }

	/**
	 * Computes the vertical visual separators.
	 */
	private void findVerticalSeparators()
	{
		for (VipsBlock vipsBlock : visualBlocks)
		{
			final int blockStart = vipsBlock.getBox().getContentBounds().getX1();
			final int blockEnd = vipsBlock.getBox().getContentBounds().getX2();
			updateSeparatorsForBlock(verticalSeparators, blockStart, blockEnd);
		}
	}

    /**
     * Computes the horizontal visual separators.
     */
    private void findHorizontalSeparators()
    {
        for (VipsBlock vipsBlock : visualBlocks)
        {
            final int blockStart = vipsBlock.getBox().getContentBounds().getY1();
            final int blockEnd = vipsBlock.getBox().getContentBounds().getY2();
            updateSeparatorsForBlock(horizontalSeparators, blockStart, blockEnd);
        }
    }

    private void updateSeparatorsForBlock(List<Separator> separators, final int blockStart, final int blockEnd)
    {
        List<Separator> toAdd = new ArrayList<>();
        for (Iterator<Separator> it = separators.iterator(); it.hasNext(); )
        {
            final Separator sep = it.next();
            // c. If the block covers the separator, remove the separator
            if (blockStart <= sep.startPoint && blockEnd >= sep.endPoint)
            {
                it.remove();
            }
            // a. If the block is contained in the separator, split the separator
            else if (blockStart > sep.startPoint && blockEnd < sep.endPoint)
            {
                final Separator newsep = new Separator(sep);
                sep.endPoint = blockStart - 1;
                newsep.startPoint = blockEnd + 1;
                toAdd.add(newsep);
            }
            // b. If the block crosses with the separator, update the separator’s parameters
            else if (blockStart < sep.startPoint && blockEnd >= sep.startPoint && blockEnd < sep.endPoint)
            {
                sep.startPoint = blockEnd + 1;
            }
            else if (blockEnd > sep.endPoint && blockStart > sep.startPoint && blockStart <= sep.endPoint)
            {
                sep.endPoint = blockStart - 1;
            }
        }
        separators.addAll(toAdd);
    }

    /**
     * Removes the border separators from the list.
     * @param list the list of separators
     * @param min the minimum coordinate value for recognizing the left/top border
     * @param max the maximum coordinate value for recognizing the right/bottom border
     */
    private void removeBorderSeparators(List<Separator> list, int min, int max)
    {
        for (Iterator<Separator> it = list.iterator(); it.hasNext(); )
        {
            final Separator separator = it.next();
            if (separator.startPoint == min || separator.endPoint == max)
                it.remove();
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
		for (Separator separator : verticalSeparators)
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
		for (Separator separator : horizontalSeparators)
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
		for (VipsBlock vipsBlock : visualBlocks)
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
		for (VipsBlock vipsBlock : visualBlocks)
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
		for (VipsBlock vipsBlock : visualBlocks)
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
		for (VipsBlock vipsBlock : visualBlocks)
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
		return horizontalSeparators;
	}

	/**
	 * @return the _verticalSeparators
	 */
	public List<Separator> getVerticalSeparators()
	{
		return verticalSeparators;
	}

    public List<Separator> getAllSeparators()
    {
        List<Separator> ret = new ArrayList<>(horizontalSeparators.size() + verticalSeparators.size());
        ret.addAll(horizontalSeparators);
        ret.addAll(verticalSeparators);
        sortSeparatorsByWeight(ret);
        return ret;
    }
	
}
