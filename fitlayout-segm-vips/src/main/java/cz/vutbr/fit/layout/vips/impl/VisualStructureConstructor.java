/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructureConstructor.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Class that constructs final visual structure of page.
 * @author Tomas Popela
 *
 */
public class VisualStructureConstructor 
{
	private List<VipsBlock> _visualBlocks = null;
	private VisualStructure _visualStructure = null;
	private List<Separator> _horizontalSeparators = null;
	private List<Separator> _verticalSeparators = null;
	private int _pageWidth = 0;
	private int _pageHeight = 0;
	private int _srcOrder = 1;
	private int _iteration = 0;
	private int _pDoC = 5;
	private static int _maxDoC = 11;
	private int _minDoC = 11;

	private boolean _graphicsOutput = true;

	public VisualStructureConstructor()
	{
		this._horizontalSeparators = new ArrayList<Separator>();
		this._verticalSeparators = new ArrayList<Separator>();
	}

	public VisualStructureConstructor(int pDoC)
	{
	    this();
		setPDoC(pDoC);
	}

	/**
	 * Sets Permitted Degree of Coherence
	 * @param pDoC Permitted Degree of Coherence
	 */
	public void setPDoC(int pDoC)
	{
		if (pDoC <= 0 || pDoC> 11)
		{
			System.err.println("pDoC value must be between 1 and 11! Not " + pDoC + "!");
			return;
		}
		else
		{
			_pDoC = pDoC;
		}
	}

	/**
	 * Enables of disables graphics output
	 * @param enabled Enabled
	 */
	public void setGraphicsOutput(boolean enabled)
	{
		this._graphicsOutput = enabled;
	}

	/**
	 * Tries to construct visual structure
	 */
	public void constructVisualStructure()
	{
		_iteration++;

		// in first iterations we try to find vertical separators before horizontal
		if (_iteration < 4)
		{
			constructVerticalVisualStructure();
			constructHorizontalVisualStructure();
			constructVerticalVisualStructure();
			constructHorizontalVisualStructure();
		}
		else
		{
			// and now we are trying to find horizontal before verical sepators
			constructHorizontalVisualStructure();
			constructVerticalVisualStructure();
		}

		if (_iteration != 1)
			updateSeparators();

		//sets order to visual structure
		_srcOrder = 1;
		setOrder(_visualStructure);

		// if graphics output is enabled
		if (_graphicsOutput)
		{
			exportSeparators();
		}
	}

	/**
	 * Constructs visual structure with blocks and horizontal separators
	 */
	private void constructHorizontalVisualStructure()
	{
		// first run
		if (_visualStructure == null)
		{
			VipsSeparatorDetector detector = null;

			if (_graphicsOutput)
				detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
			else
				detector = new VipsSeparatorDetector(_pageWidth, _pageHeight);

			detector.setCleanUpSeparators(3);
			detector.setVisualBlocks(_visualBlocks);
			detector.detectHorizontalSeparators();
			this._horizontalSeparators = detector.getHorizontalSeparators();
			Collections.sort(_horizontalSeparators);

			_visualStructure = new VisualStructure();
			_visualStructure.setId("1");
			_visualStructure.setBlockRoots(_visualBlocks);
			_visualStructure.setWidth(_pageWidth);
			_visualStructure.setHeight(_pageHeight);

			for (Separator separator : _horizontalSeparators)
			{
				separator.setTopLeft(_visualStructure.getX(), separator.startPoint);
				separator.setBottomRight(_visualStructure.getX()+_visualStructure.getWidth(), separator.endPoint);
			}

			constructWithHorizontalSeparators(_visualStructure);
		}
		else
		{
			List<VisualStructure> listStructures = new ArrayList<VisualStructure>();
			findListVisualStructures(_visualStructure, listStructures);

			for (VisualStructure childVisualStructure : listStructures)
			{
				VipsSeparatorDetector detector = null;

				if (_graphicsOutput)
					detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
				else
					detector = new VipsSeparatorDetector(_pageWidth, _pageHeight);

				detector.setCleanUpSeparators(4);

				//detector.setVipsBlock(_vipsBlocks);
				detector.setVisualBlocks(childVisualStructure.getBlockRoots());
				detector.detectHorizontalSeparators();
				this._horizontalSeparators = detector.getHorizontalSeparators();

				for (Separator separator : _horizontalSeparators)
				{
					separator.setTopLeft(childVisualStructure.getX(), separator.startPoint);
					separator.setBottomRight(childVisualStructure.getX()+childVisualStructure.getWidth(), separator.endPoint);
				}

				constructWithHorizontalSeparators(childVisualStructure);
			}
		}
	}

	/**
	 * Constructs visual structure with blocks and vertical separators
	 */
	private void constructVerticalVisualStructure()
	{
		// first run
		if (_visualStructure == null)
		{
			VipsSeparatorDetector detector = null;

			if (_graphicsOutput)
				detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
			else
				detector = new VipsSeparatorDetector(_pageWidth, _pageHeight);

			detector.setCleanUpSeparators(3);
			//detector.setVipsBlock(_vipsBlocks);
			detector.setVisualBlocks(_visualBlocks);
			detector.detectVerticalSeparators();
			this._verticalSeparators = detector.getVerticalSeparators();
			Collections.sort(_verticalSeparators);

			_visualStructure = new VisualStructure();
			_visualStructure.setId("1");
			_visualStructure.setBlockRoots(_visualBlocks);
			_visualStructure.setWidth(_pageWidth);
			_visualStructure.setHeight(_pageHeight);

			for (Separator separator : _verticalSeparators)
			{
				separator.setTopLeft(separator.startPoint, _visualStructure.getY());
				separator.setBottomRight(separator.endPoint, _visualStructure.getY()+_visualStructure.getHeight());
			}

			constructWithVerticalSeparators(_visualStructure);
		}
		else
		{
			List<VisualStructure> listStructures = new ArrayList<VisualStructure>();
			findListVisualStructures(_visualStructure, listStructures);
			for (VisualStructure childVisualStructure : listStructures)
			{
				VipsSeparatorDetector detector = null;

				if (_graphicsOutput)
					detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
				else
					detector = new VipsSeparatorDetector(_pageWidth, _pageHeight);

				detector.setCleanUpSeparators(4);

				//detector.setVipsBlock(_vipsBlocks);
				detector.setVisualBlocks(childVisualStructure.getBlockRoots());
				detector.detectVerticalSeparators();
				this._verticalSeparators = detector.getVerticalSeparators();

				for (Separator separator : _verticalSeparators)
				{
					separator.setTopLeft(separator.startPoint, childVisualStructure.getY());
					separator.setBottomRight(separator.endPoint, childVisualStructure.getY()+childVisualStructure.getHeight());
				}

				constructWithVerticalSeparators(childVisualStructure);
			}
		}
	}

	/**
	 * Performs actual constructing of visual structure with horizontal separators
	 * @param actualStructure Actual visual structure
	 */
	private void constructWithHorizontalSeparators(VisualStructure actualStructure)
	{
		// if we have no visual blocks or separators
		if (actualStructure.getBlockRoots().size() == 0 || _horizontalSeparators.size() == 0)
		{
			return;
		}

		VisualStructure topVisualStructure = null;
		VisualStructure bottomVisualStructure =  null;
		List<VipsBlock> nestedBlocks =  null;

		//construct children visual structures
		for (Separator separator : _horizontalSeparators)
		{
			if (actualStructure.getChildren().size() == 0)
			{
				topVisualStructure = new VisualStructure();
				topVisualStructure.setX(actualStructure.getX());
				topVisualStructure.setY(actualStructure.getY());
				topVisualStructure.setHeight((separator.startPoint-1)-actualStructure.getY());
				topVisualStructure.setWidth(actualStructure.getWidth());
				actualStructure.addChild(topVisualStructure);

				bottomVisualStructure = new VisualStructure();
				bottomVisualStructure.setX(actualStructure.getX());
				bottomVisualStructure.setY(separator.endPoint+1);
				bottomVisualStructure.setHeight((actualStructure.getHeight()+actualStructure.getY())-separator.endPoint-1);
				bottomVisualStructure.setWidth(actualStructure.getWidth());
				actualStructure.addChild(bottomVisualStructure);

				nestedBlocks = actualStructure.getBlockRoots();
			}
			else
			{
				VisualStructure oldStructure = null;
				for (VisualStructure childVisualStructure : actualStructure.getChildren())
				{
					if (separator.startPoint >= childVisualStructure.getY() &&
							separator.endPoint <= (childVisualStructure.getY() + childVisualStructure.getHeight()))
					{
						topVisualStructure = new VisualStructure();
						topVisualStructure.setX(childVisualStructure.getX());
						topVisualStructure.setY(childVisualStructure.getY());
						topVisualStructure.setHeight((separator.startPoint-1) - childVisualStructure.getY());
						topVisualStructure.setWidth(childVisualStructure.getWidth());
						int index = actualStructure.getChildren().indexOf(childVisualStructure);
						actualStructure.addChildAt(topVisualStructure, index);

						bottomVisualStructure = new VisualStructure();
						bottomVisualStructure.setX(childVisualStructure.getX());
						bottomVisualStructure.setY(separator.endPoint+1);
						int height = (childVisualStructure.getHeight()+childVisualStructure.getY())-separator.endPoint-1;
						bottomVisualStructure.setHeight(height);
						bottomVisualStructure.setWidth(childVisualStructure.getWidth());
						actualStructure.addChildAt(bottomVisualStructure, index+1);

						oldStructure = childVisualStructure;
						break;
					}
				}
				if (oldStructure != null)
				{
					nestedBlocks = oldStructure.getBlockRoots();
					actualStructure.getChildren().remove(oldStructure);
				}
			}

			if (topVisualStructure == null || bottomVisualStructure == null)
				return;

			for (VipsBlock vipsBlock : nestedBlocks)
			{
				if (vipsBlock.getBox().getContentBounds().getY1() <= separator.startPoint)
					topVisualStructure.addBlock(vipsBlock);
				else
					bottomVisualStructure.addBlock(vipsBlock);
			}

			topVisualStructure = null;
			bottomVisualStructure = null;
		}

		// set id for visual structures
		int iterator = 1;
		for (VisualStructure visualStructure : actualStructure.getChildren())
		{
			visualStructure.setId(actualStructure.getId() + "-" + iterator);
			iterator++;
		}

		List<Separator> allSeparatorsInBlock = new ArrayList<Separator>();
		allSeparatorsInBlock.addAll(_horizontalSeparators);

		//remove all children separators
		for (VisualStructure vs : actualStructure.getChildren())
		{
			vs.getHorizontalSeparators().clear();
		}

		//save all horizontal separators in my region
		actualStructure.addHorizontalSeparators(_horizontalSeparators);
	}

	/**
	 * Performs actual constructing of visual structure with vertical separators
	 * @param actualStructure Actual visual structure
	 */
	private void constructWithVerticalSeparators(VisualStructure actualStructure)
	{
		// if we have no visual blocks or separators
		if (actualStructure.getBlockRoots().size() == 0 || _verticalSeparators.size() == 0)
		{
			return;
		}

		VisualStructure leftVisualStructure = null;
		VisualStructure rightVisualStructure =  null;
		List<VipsBlock> nestedBlocks =  null;

		//construct children visual structures
		for (Separator separator : _verticalSeparators)
		{
			if (actualStructure.getChildren().size() == 0)
			{
				leftVisualStructure = new VisualStructure();
				leftVisualStructure.setX(actualStructure.getX());
				leftVisualStructure.setY(actualStructure.getY());
				leftVisualStructure.setHeight(actualStructure.getHeight());
				leftVisualStructure.setWidth((separator.startPoint-1)-actualStructure.getX());
				actualStructure.addChild(leftVisualStructure);

				rightVisualStructure = new VisualStructure();
				rightVisualStructure.setX(separator.endPoint+1);
				rightVisualStructure.setY(actualStructure.getY());
				rightVisualStructure.setHeight(actualStructure.getHeight());
				rightVisualStructure.setWidth((actualStructure.getWidth()+actualStructure.getX()) - separator.endPoint-1);
				actualStructure.addChild(rightVisualStructure);

				nestedBlocks = actualStructure.getBlockRoots();
			}
			else
			{
				VisualStructure oldStructure = null;
				for (VisualStructure childVisualStructure : actualStructure.getChildren())
				{
					if (separator.startPoint >= childVisualStructure.getX() &&
							separator.endPoint <= (childVisualStructure.getX() + childVisualStructure.getWidth()))
					{
						leftVisualStructure = new VisualStructure();
						leftVisualStructure.setX(childVisualStructure.getX());
						leftVisualStructure.setY(childVisualStructure.getY());
						leftVisualStructure.setHeight(childVisualStructure.getHeight());
						leftVisualStructure.setWidth((separator.startPoint-1)-childVisualStructure.getX());
						int index = actualStructure.getChildren().indexOf(childVisualStructure);
						actualStructure.addChildAt(leftVisualStructure, index);

						rightVisualStructure = new VisualStructure();
						rightVisualStructure.setX(separator.endPoint+1);
						rightVisualStructure.setY(childVisualStructure.getY());
						rightVisualStructure.setHeight(childVisualStructure.getHeight());
						int width = (childVisualStructure.getWidth()+childVisualStructure.getX())-separator.endPoint-1;
						rightVisualStructure.setWidth(width);
						actualStructure.addChildAt(rightVisualStructure, index+1);

						oldStructure = childVisualStructure;
						break;
					}
				}
				if (oldStructure != null)
				{
					nestedBlocks = oldStructure.getBlockRoots();
					actualStructure.getChildren().remove(oldStructure);
				}
			}

			if (leftVisualStructure == null || rightVisualStructure == null)
				return;

			for (VipsBlock vipsBlock : nestedBlocks)
			{
				if (vipsBlock.getBox().getContentBounds().getX1() <= separator.startPoint)
					leftVisualStructure.addBlock(vipsBlock);
				else
					rightVisualStructure.addBlock(vipsBlock);
			}

			leftVisualStructure = null;
			rightVisualStructure = null;
		}

		// set id for visual structures
		int iterator = 1;
		for (VisualStructure visualStructure : actualStructure.getChildren())
		{
			visualStructure.setId(actualStructure.getId() + "-" + iterator);
			iterator++;
		}

		List<Separator> allSeparatorsInBlock = new ArrayList<Separator>();
		allSeparatorsInBlock.addAll(_verticalSeparators);

		//remove all children separators
		for (VisualStructure vs : actualStructure.getChildren())
		{
			vs.getVerticalSeparators().clear();
		}

		//save all horizontal separators in my region
		actualStructure.addVerticalSeparators(_verticalSeparators);
	}

	/**
	 * Exports all separators to output images
	 */
	private void exportSeparators()
	{
		VipsSeparatorGraphicsDetector detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
		List<Separator> allSeparators = new ArrayList<Separator>();

		getAllHorizontalSeparators(_visualStructure, allSeparators);
		Collections.sort(allSeparators);

		detector.setHorizontalSeparators(allSeparators);
		detector.exportHorizontalSeparatorsToImage(_iteration);

		allSeparators.clear();

		getAllVerticalSeparators(_visualStructure, allSeparators);
		Collections.sort(allSeparators);

		detector.setVerticalSeparators(allSeparators);
		detector.exportVerticalSeparatorsToImage(_iteration);

		detector.setVisualBlocks(_visualBlocks);
		detector.exportAllToImage(_iteration);
	}

	/**
	 * Sets page's size
	 * @param width Page's width
	 * @param height Page's height
	 */
	public void setPageSize(int width, int height)
	{
		this._pageHeight = height;
		this._pageWidth = width;
	}

	/**
	 * @return Returns final visual structure
	 */
	public VisualStructure getVisualStructure()
	{
		return _visualStructure;
	}

	/**
	 * Sets VipsBlock structure and also finds and saves all visual blocks from its
	 * @param vipsBlocks VipsBlock structure
	 */
	public void setVipsBlocks(List<VipsBlock> vipsBlocks)
	{
		_visualBlocks = vipsBlocks;
	}

	/**
	 * Returns all visual blocks in page
	 * @return Visual Blocks
	 */
	public List<VipsBlock> getVisualBlocks()
	{
		return _visualBlocks;
	}

	/**
	 * Returns all horizontal separators detected on page
	 * @return List of horizontal separators
	 */
	public List<Separator> getHorizontalSeparators()
	{
		return _horizontalSeparators;
	}

	/**
	 * Sets horizontal separators to page
	 * @param  horizontalSeparators List of horizontal separators
	 */
	public void setHorizontalSeparator(List<Separator> horizontalSeparators)
	{
		this._horizontalSeparators = horizontalSeparators;
	}

	/**
	 * Returns all vertical separators detected on page
	 * @return List of vertical separators
	 */
	public List<Separator> getVerticalSeparators()
	{
		return _verticalSeparators;
	}

	/**
	 * Sets vertical separators to page
	 * @param  verticalSeparators List of vertical separators
	 */
	public void setVerticalSeparator(List<Separator> verticalSeparators)
	{
		this._verticalSeparators = verticalSeparators;
	}

	/**
	 * Sets vertical and horizontal separators to page
	 * @param horizontalSeparators List of horizontal separators
	 * @param verticalSeparators List of vertical separators
	 */
	public void setSeparators(List<Separator> horizontalSeparators, List<Separator> verticalSeparators)
	{
		this._verticalSeparators = verticalSeparators;
		this._horizontalSeparators = horizontalSeparators;
	}

	/**
	 * Finds list visual structures in visual structure tree
	 * @param visualStructure Actual structure
	 * @param results Results
	 */
	private void findListVisualStructures(VisualStructure visualStructure, List<VisualStructure> results)
	{
		if (visualStructure.getChildren().size() == 0)
			results.add(visualStructure);

		for (VisualStructure child : visualStructure.getChildren())
			findListVisualStructures(child, results);
	}

	/**
	 * Replaces given old blocks with given new one
	 * @param oldBlocks	List of old blocks
	 * @param newBlocks List of new blocks
	 * @param actualStructure Actual Structure
	 * @param pathStructures Path from structure to root of the structure
	 */
	private void replaceBlocksInPredecessors(List<VipsBlock> oldBlocks, List<VipsBlock> newBlocks, VisualStructure actualStructure, List<String> pathStructures)
	{
		for (VisualStructure child : actualStructure.getChildren())
		{
			replaceBlocksInPredecessors(oldBlocks, newBlocks, child, pathStructures);
		}

		for (String structureId : pathStructures)
		{
			if (actualStructure.getId().equals(structureId))
			{
				List<VipsBlock> tempBlocks = new ArrayList<VipsBlock>();
				tempBlocks.addAll(actualStructure.getBlockRoots());

				//remove old blocks
				for (VipsBlock block : tempBlocks)
				{
					for (VipsBlock oldBlock : oldBlocks)
					{
						if (block.equals(oldBlock))
						{
							actualStructure.getBlockRoots().remove(block);
						}
					}
				}
				//add new blocks
				actualStructure.addBlocks(newBlocks);
			}
		}
	}

	/**
	 * Generates element's id's for elements that are on path
	 * @param Path (Start visual strucure id)
	 * @return List of id's
	 */
	private List<String> generatePathStructures(String path)
	{
		List<String> pathStructures = new ArrayList<String>();

		String[] aaa = path.split("-");

		String tmp = "";

		for (int i = 0; i < aaa.length - 1; i++)
		{
			tmp += aaa[i];
			pathStructures.add(tmp);
			tmp += "-";
		}

		return pathStructures;
	}

	/**
	 * Updates VipsBlock structure with the new one and also updates visual blocks on page
	 * @param vipsBlocks New VipsBlock structure
	 */
	public void updateVipsBlocks(VipsBlock vipsBlocks)
	{
	    //TODO what is this?
		//setVipsBlocks(vipsBlocks);

		List<VisualStructure> listsVisualStructures = new ArrayList<VisualStructure>();
		List<VipsBlock> oldNestedBlocks = new ArrayList<VipsBlock>();
		findListVisualStructures(_visualStructure, listsVisualStructures);

		for (VisualStructure visualStructure : listsVisualStructures)
		{
			oldNestedBlocks.addAll(visualStructure.getBlockRoots());
			visualStructure.clearBlocks();
			for (VipsBlock visualBlock : _visualBlocks)
			{
				final Rectangular bb = visualBlock.getBox().getContentBounds();
                if (bb.getX1() >= visualStructure.getX() &&
						bb.getX1() <= (visualStructure.getX() + visualStructure.getWidth()))
				{
					if (bb.getY1() >= visualStructure.getY() &&
							bb.getY1() <= (visualStructure.getY() + visualStructure.getHeight()))
					{
						if (bb.getHeight() != 0 && bb.getWidth() != 0)
							visualStructure.addBlock(visualBlock);
					}
				}
			}
			if (visualStructure.getBlockRoots().size() == 0)
			{
				visualStructure.addBlocks(oldNestedBlocks);
				_visualBlocks.addAll(oldNestedBlocks);
			}

			String path = visualStructure.getId();

			List<String> pathStructures = generatePathStructures(path);

			replaceBlocksInPredecessors(oldNestedBlocks, visualStructure.getBlockRoots(), _visualStructure, pathStructures);

			oldNestedBlocks.clear();
		}
	}

	/**
	 * Sets order to visual structure
	 * @param visualStructure
	 */
	private void setOrder(VisualStructure visualStructure)
	{
		//visualStructure.setOrder(_srcOrder);
		_srcOrder++;

		for (VisualStructure child : visualStructure.getChildren())
			setOrder(child);
	}

	/**
	 * Finds all horizontal and vertical separators in given structure
	 * @param visualStructure Given structure
	 * @param result Results
	 */
	private void getAllSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		findAllHorizontalSeparators(visualStructure, result);
		findAllVerticalSeparators(visualStructure, result);
		removeDuplicates(result);
	}

	/**
	 * Finds all horizontal separators in given structure
	 * @param visualStructure Given structure
	 * @param result Results
	 */
	private void getAllHorizontalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		findAllHorizontalSeparators(visualStructure, result);
		removeDuplicates(result);
	}

	/**
	 * Finds all vertical separators in given structure
	 * @param visualStructure Given structure
	 * @param result Results
	 */
	private void getAllVerticalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		findAllVerticalSeparators(visualStructure, result);
		removeDuplicates(result);
	}

	/**
	 * Finds all horizontal separators in given structure
	 * @param visualStructure Given structure
	 * @param result Results
	 */
	private void findAllHorizontalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		result.addAll(visualStructure.getHorizontalSeparators());

		for (VisualStructure child : visualStructure.getChildren())
		{
			findAllHorizontalSeparators(child, result);
		}
	}

	/**
	 * Finds all vertical separators in given structure
	 * @param visualStructure Given structure
	 * @param result Results
	 */
	private void findAllVerticalSeparators(VisualStructure visualStructure, List<Separator> result)
	{
		result.addAll(visualStructure.getVerticalSeparators());

		for (VisualStructure child : visualStructure.getChildren())
		{
			findAllVerticalSeparators(child, result);
		}
	}

	/**
	 * Updates separators when replacing blocks
	 * @param visualStructure Actual visual structure
	 */
	private void updateSeparatorsInStructure(VisualStructure visualStructure)
	{
		List<VipsBlock> adjacentBlocks = new ArrayList<VipsBlock>();

		List<Separator> allSeparators = new ArrayList<Separator>();
		allSeparators.addAll(visualStructure.getHorizontalSeparators());

		// separator between blocks
		for (Separator separator : allSeparators)
		{
			int aboveBottom = 0;
			int belowTop = _pageHeight;
			VipsBlock above = null;
			VipsBlock below = null;
			adjacentBlocks.clear();

			for (VipsBlock block : visualStructure.getBlockRoots())
			{
				final Rectangular bb = block.getBox().getContentBounds();
                final int top = bb.getY1();
				final int bottom = bb.getY1() + bb.getHeight();

				if (bottom <= separator.startPoint && bottom > aboveBottom)
				{
					aboveBottom = bottom;
					above = block;
				}

				if (top >= separator.endPoint && top < belowTop)
				{
					belowTop = top;
					below = block;
					adjacentBlocks.add(block);
				}
			}

			if (above == null || below == null)
				continue;

			adjacentBlocks.add(above);
			adjacentBlocks.add(below);

			if (aboveBottom == separator.startPoint - 1 && belowTop == separator.endPoint + 1)
				continue;

			if (adjacentBlocks.size() < 2)
				continue;

			VipsSeparatorDetector detector = null;

			if (_graphicsOutput)
				detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
			else
				detector = new VipsSeparatorDetector(_pageWidth, _pageHeight);

			detector.setCleanUpSeparators(3);
			if (_iteration > 3)
				detector.setCleanUpSeparators(6);

			//detector.setVipsBlock(_vipsBlocks);
			detector.setVisualBlocks(adjacentBlocks);
			detector.detectHorizontalSeparators();

			List<Separator> tempSeparators = new ArrayList<Separator>();
			tempSeparators.addAll(visualStructure.getHorizontalSeparators());

			if (detector.getHorizontalSeparators().size() == 0)
				continue;

			Separator newSeparator = detector.getHorizontalSeparators().get(0);
			newSeparator.setTopLeft(visualStructure.getX(), newSeparator.startPoint);
			newSeparator.setBottomRight(visualStructure.getX()+visualStructure.getWidth(), newSeparator.endPoint);

			//remove all separators, that are included in block
			for (Separator other : tempSeparators)
			{
				if (other.equals(separator))
				{
					visualStructure.getHorizontalSeparators().add(visualStructure.getHorizontalSeparators().indexOf(other)+1, newSeparator);
					visualStructure.getHorizontalSeparators().remove(other);
					break;
				}
			}
		}

		// new blocks in separator
		for (Separator separator : allSeparators)
		{
			int blockTop = _pageHeight;
			int blockDown = 0;
			adjacentBlocks.clear();

			for (VipsBlock block : visualStructure.getBlockRoots())
			{
				final Rectangular bb = block.getBox().getContentBounds();
                final int top = bb.getY1();
				final int bottom = bb.getY1() + bb.getHeight();

				// block is inside the separator
				if (top > separator.startPoint && bottom < separator.endPoint)
				{
					adjacentBlocks.add(block);

					if (top < blockTop)
						blockTop = top;

					if (bottom > blockDown)
						blockDown = bottom;
				}
			}

			if (adjacentBlocks.size() == 0)
				continue;

			VipsSeparatorDetector detector = null;

			if (_graphicsOutput)
				detector = new VipsSeparatorGraphicsDetector(_pageWidth, _pageHeight);
			else
				detector = new VipsSeparatorDetector(_pageWidth, _pageHeight);

			detector.setCleanUpSeparators(3);
			if (_iteration > 3)
				detector.setCleanUpSeparators(6);

			detector.setVisualBlocks(adjacentBlocks);
			detector.detectHorizontalSeparators();

			List<Separator> tempSeparators = new ArrayList<Separator>();
			tempSeparators.addAll(visualStructure.getHorizontalSeparators());

			List<Separator> newSeparators = new ArrayList<Separator>();

			Separator newSeparatorTop = new Separator(separator.startPoint, blockTop - 1, separator.weight);
			newSeparatorTop.setTopLeft(visualStructure.getX(), newSeparatorTop.startPoint);
			newSeparatorTop.setBottomRight(visualStructure.getX()+visualStructure.getWidth(), newSeparatorTop.endPoint);

			newSeparators.add(newSeparatorTop);

			Separator newSeparatorBottom = new Separator(blockDown + 1, separator.endPoint, separator.weight);
			newSeparatorBottom.setTopLeft(visualStructure.getX(), newSeparatorBottom.startPoint);
			newSeparatorBottom.setBottomRight(visualStructure.getX()+visualStructure.getWidth(), newSeparatorBottom.endPoint);

			if (detector.getHorizontalSeparators().size() != 0)
			{
				newSeparators.addAll(detector.getHorizontalSeparators());
			}

			newSeparators.add(newSeparatorBottom);

			//remove all separators, that are included in block
			for (Separator other : tempSeparators)
			{
				if (other.equals(separator))
				{
					visualStructure.getHorizontalSeparators().addAll(visualStructure.getHorizontalSeparators().indexOf(other)+1, newSeparators);
					visualStructure.getHorizontalSeparators().remove(other);
					break;
				}
			}
		}
		for (VisualStructure child : visualStructure.getChildren())
		{
			updateSeparatorsInStructure(child);
		}
	}

	/**
	 * Updates separators on whole page
	 */
	private void updateSeparators()
	{
		updateSeparatorsInStructure(_visualStructure);
	}

	/**
	 * Removes duplicates from list of separators
	 * @param separators
	 */
	private void removeDuplicates(List<Separator> separators)
	{
		HashSet<Separator> hashSet = new HashSet<Separator>(separators);
		separators.clear();
		separators.addAll(hashSet);
	}

	/**
	 * Converts normalized weight of separator to DoC
	 * @param Normalized weight of separator
	 * @return DoC
	 */
	private int getDoCValue(int value)
	{
		if (value == 0)
			return _maxDoC;

		return ((_maxDoC + 1) - value);
	}

	/**
	 * Normalizes separators weights with linear normalization
	 */
	public void normalizeSeparatorsSoftMax()
	{
		List<Separator> separators = new ArrayList<Separator>();
		getAllSeparators(_visualStructure, separators);
		Collections.sort(separators);

		double stdev = getStdDeviation(separators);
		double meanValue = 0;
		double lambda = 3.0;
		double alpha = 1.0;

		for (Separator separator : separators)
		{
			meanValue += separator.weight;
		}

		meanValue /= separators.size();

		for (Separator separator : separators)
		{
			double normalizedValue = (separator.weight - meanValue) / (lambda * (stdev / (2 * Math.PI)));
			normalizedValue = 1 / (1 + Math.exp(-alpha * normalizedValue) + 1);
			normalizedValue = normalizedValue * (11 - 1) + 1;
			separator.normalizedWeight = getDoCValue((int) Math.round(normalizedValue));

			if (separator.weight == 3)
				separator.normalizedWeight = 11;
			/*			System.out.println(separator.startPoint + "\t" + separator.endPoint + "\t" +
					(separator.endPoint - separator.startPoint + 1) +
					"\t" + separator.weight + "\t" + separator.normalizedWeight +
					"\t" + normalizedValue);*/
		}

		//updateDoC(_visualStructure);

		_visualStructure.setDoC(1);
	}

	/**
	 * Normalizes separators weights with linear normalization
	 */
	public void normalizeSeparatorsMinMax()
	{
		List<Separator> separators = new ArrayList<Separator>();

		getAllSeparators(_visualStructure, separators);

		Separator maxSep = new Separator(0, _pageHeight);
		separators.add(maxSep);
		maxSep.weight = 40;

		Collections.sort(separators);

		double minWeight = separators.get(0).weight;
		double maxWeight = separators.get(separators.size()-1).weight;

		for (Separator separator : separators)
		{
			double normalizedValue = (separator.weight - minWeight) / (maxWeight - minWeight) * (11 - 1) + 1;
			separator.normalizedWeight = getDoCValue((int) Math.ceil(normalizedValue));
			/*		System.out.println(separator.startPoint + "\t" + separator.endPoint + "\t" +
					(separator.endPoint - separator.startPoint + 1) +
					"\t" + separator.weight + "\t" + separator.normalizedWeight +
					"\t" + normalizedValue);*/
		}

		//updateDoC(_visualStructure);

		_visualStructure.setDoC(1);
	}

	/**
	 * Finds minimal DoC in given structure
	 * @param visualStructure
	 */
	private void findMinimalDoC(VisualStructure visualStructure)
	{
		if (!visualStructure.getId().equals("1"))
		{
			if (visualStructure.getDoC() < _minDoC)
				_minDoC = visualStructure.getDoC();
		}

		for (VisualStructure child : visualStructure.getChildren())
		{
			findMinimalDoC(child);
		}
	}

	/**
	 * Returns minimal DoC on page
	 * @return Minimal DoC
	 */
	public int getMinimalDoC()
	{
		_minDoC = 11;

		findMinimalDoC(_visualStructure);

		return _minDoC;
	}

	/**
	 * Checks if it's necessary to continue in segmentation
	 * @return True if it's necessary to continue in segmentation, otherwise false
	 */
	public boolean continueInSegmentation()
	{
		getMinimalDoC();

		if (_pDoC < _minDoC)
			return false;

		return true;
	}

	/**
	 * Counts standard deviation from list of separators
	 * @param separators List of separators
	 * @return Standard deviation
	 */
	private double getStdDeviation(List<Separator> separators)
	{
		double meanValue = 0.0;
		double stddev = 0.0;
		List<Double> deviations = new ArrayList<Double>();
		List<Double> squaredDeviations = new ArrayList<Double>();
		double sum = 0.0;

		for (Separator separator : separators)
		{
			meanValue += separator.weight;
		}

		meanValue /= separators.size();

		for (Separator separator : separators)
		{
			deviations.add(separator.weight - meanValue);
		}

		for (Double deviation : deviations)
		{
			squaredDeviations.add(deviation * deviation);
		}

		for (Double squaredDeviation : squaredDeviations)
		{
			sum += squaredDeviation;
		}

		stddev = Math.sqrt(sum/squaredDeviations.size());

		return stddev;
	}

}
