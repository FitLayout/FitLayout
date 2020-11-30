/**
 * VIPS - Visual Internet Page Segmentation for FitLayout
 * 
 * Tomas Popela, 2012
 * Radek Burget, 2020 
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
public class SeparatorDetector 
{
	private List<VisualBlock> visualBlocks;
	private List<Separator> horizontalSeparators;
	private List<Separator> verticalSeparators;
	private Rectangular pageBounds;

	/**
	 * Creates the separator detector from a list of visual blocks.
	 * 
	 * @param visualBlocks the visual blocks to consider
	 * @param pageBounds the sub-page bounds
	 */
	public SeparatorDetector(List<VisualBlock> visualBlocks, Rectangular pageBounds) 
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
	public void setVisualBlocks(List<VisualBlock> visualBlocks)
	{
		this.visualBlocks = visualBlocks;
	}

	/**
	 * Gets VIPS block that is used for separators computing.
	 * @return Visual structure
	 */
	public List<VisualBlock> getVisualBlocks()
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
		for (VisualBlock vipsBlock : visualBlocks)
		{
			final int blockStart = vipsBlock.getBounds().getX1();
			final int blockEnd = vipsBlock.getBounds().getX2();
			updateSeparatorsForBlock(verticalSeparators, blockStart, blockEnd);
		}
	}

    /**
     * Computes the horizontal visual separators.
     */
    private void findHorizontalSeparators()
    {
        for (VisualBlock vipsBlock : visualBlocks)
        {
            final int blockStart = vipsBlock.getBounds().getY1();
            final int blockEnd = vipsBlock.getBounds().getY2();
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
            else if (blockStart <= sep.startPoint && blockEnd >= sep.startPoint && blockEnd < sep.endPoint)
            {
                sep.startPoint = blockEnd + 1;
            }
            else if (blockEnd >= sep.endPoint && blockStart > sep.startPoint && blockStart <= sep.endPoint)
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
			ruleTwo(separator);
			ruleThree(separator);
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
			ruleTwo(separator);
			ruleThree(separator);
			ruleFour(separator);
			ruleFive(separator);
		}
	}

	/**
	 * The greater the distance between blocks on different
	 * side of the separator, the higher the weight. <p>
	 * For every 10 points of width we increase weight by 2 points.
	 * @param separator Separator
	 */
	private void ruleOne(Separator separator)
	{
		final int width = separator.endPoint - separator.startPoint + 1;

		if (width > 15)
			separator.weight += ((width - 16) / 10 + 1) * 2 + 2; // 6 for 16, 8 for 26, 10 for 36, etc.
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
	private void ruleTwo(Separator separator)
	{
		final List<VisualBlock> overlappedElements = findOverlappedElements(separator);
		for (VisualBlock vipsBlock : overlappedElements)
		{
			if ("hr".equalsIgnoreCase(vipsBlock.getBox().getTagName()))
			{
				separator.weight += 2;
				break;
			}
		}
	}

	/**
	 * Finds elements that are overlapped with a separator.
	 * @param separator Separator, that we look at
	 */
	private List<VisualBlock> findOverlappedElements(Separator separator)
	{
	    final List<VisualBlock> result = new ArrayList<>();
		for (VisualBlock vipsBlock : visualBlocks)
		{
            final int blockStart;
            final int blockEnd;
		    if (separator.isVertical())
		    {
                blockStart = vipsBlock.getBounds().getX1();
                blockEnd = vipsBlock.getBounds().getX2();
		    }
		    else
		    {
                blockStart = vipsBlock.getBounds().getY1();
                blockEnd = vipsBlock.getBounds().getY2();
		    }

            if ((blockStart >= separator.startPoint && blockStart <= separator.endPoint)
                    || (blockEnd >= separator.startPoint && blockEnd <= separator.endPoint))
            {
                result.add(vipsBlock);
            }
		}
		return result;
	}

	/**
	 * If background colors of the blocks on two sides of the separator
	 * are different, the weight will be increased.
	 * 
	 * @param separator Separator
	 */
	private void ruleThree(Separator separator)
	{
		List<VisualBlock> adjacentBefore = new ArrayList<>();
		List<VisualBlock> adjacentAfter = new ArrayList<>();
		findAdjacentBlocks(separator, adjacentBefore, adjacentAfter);

		if (!adjacentBefore.isEmpty() && !adjacentAfter.isEmpty())
		{
    		boolean weightIncreased = false;
    		for (VisualBlock before : adjacentBefore)
    		{
    			for (VisualBlock after : adjacentAfter)
    			{
    				if (!before.getBgColor().equals(after.getBgColor()))
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
	}

	/**
	 * Finds elements that are adjacent to horizontal separator.
	 * @param separator Separator, that we look at
	 * @param vipsBlock Visual block corresponding to element
	 * @param before Elements, that we found on top side of separator
	 * @param after Elements, that we found on bottom side side of separator
	 */
	private void findAdjacentBlocks(Separator separator, List<VisualBlock> before, List<VisualBlock> after)
	{
		for (VisualBlock vipsBlock : visualBlocks)
		{
            final int blockStart;
            final int blockEnd;
            if (separator.isVertical())
            {
                blockStart = vipsBlock.getBounds().getX1();
                blockEnd = vipsBlock.getBounds().getX2();
            }
            else
            {
                blockStart = vipsBlock.getBounds().getY1();
                blockEnd = vipsBlock.getBounds().getY2();
            }

			if (blockStart == separator.endPoint + 1)
			{
				after.add(vipsBlock);
			}
			else if (blockEnd == separator.startPoint - 1)
			{
				before.add(0, vipsBlock);
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
		final List<VisualBlock> adjacentTop = new ArrayList<>();
		final List<VisualBlock> adjacentBottom = new ArrayList<>();
		findAdjacentBlocks(separator, adjacentTop, adjacentBottom);
		
		if (!adjacentTop.isEmpty() && !adjacentBottom.isEmpty())
		{
    		boolean weightIncreased = false;
    		for (VisualBlock top : adjacentTop)
    		{
    			for (VisualBlock bottom : adjacentBottom)
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
    		for (VisualBlock top : adjacentTop)
    		{
    			for (VisualBlock bottom : adjacentBottom)
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
	}

	/**
	 * For horizontal separators, when the structures of the blocks on the two
	 * sides of the separator are very similar (e.g. both are text),
	 * the weight of the separator will be decreased.
	 * @param separator Separator
	 */
	private void ruleFive(Separator separator)
	{
		final List<VisualBlock> adjacentTop = new ArrayList<>();
		final List<VisualBlock> adjacentBottom = new ArrayList<>();
		findAdjacentBlocks(separator, adjacentTop, adjacentBottom);

        if (!adjacentTop.isEmpty() && !adjacentBottom.isEmpty())
        {
    		boolean weightDecreased = false;
    		for (VisualBlock top : adjacentTop)
    		{
    			for (VisualBlock bottom : adjacentBottom)
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
	}

	public List<Separator> getHorizontalSeparators()
	{
		return horizontalSeparators;
	}

	public List<Separator> getVerticalSeparators()
	{
		return verticalSeparators;
	}

    /**
     * Gets all the separators (both horizontal and vertical) sorted by weight.
     * @return a list of all separators
     */
	public List<Separator> getAllSeparators()
    {
        List<Separator> ret = new ArrayList<>(horizontalSeparators.size() + verticalSeparators.size());
        ret.addAll(horizontalSeparators);
        ret.addAll(verticalSeparators);
        sortSeparatorsByWeight(ret);
        return ret;
    }
	
}
