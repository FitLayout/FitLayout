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
import cz.vutbr.fit.layout.model.Box.DisplayType;
import cz.vutbr.fit.layout.model.Box.Type;

/**
 * Detects the visual blocks in the page.
 * 
 * @author Tomas Popela
 * @author burgetr
 */
public class VisualBlockDetector 
{
    private VisualArea rootArea;

	private int sizeTresholdWidth = 0;
	private int sizeTresholdHeight = 0;

	/**
	 * Creates a detector for the given root visual area.
	 * 
	 * @param rootArea the root visual area to create detector for.
	 */
	public VisualBlockDetector(VisualArea rootArea) 
	{
	    this.rootArea = rootArea;
		this.sizeTresholdHeight = 80;
		this.sizeTresholdWidth = 80;
	}

	/**
	 * Creates a new detector with the specified thresholds.
	 * 
     * @param rootArea the root visual area to create detector for.
	 * @param sizeTresholdWidth Element's width treshold
	 * @param sizeTresholdHeight Element's height treshold
	 */
	public VisualBlockDetector(VisualArea rootArea, int sizeTresholdWidth, int sizeTresholdHeight) 
	{
	    this(rootArea);
		this.sizeTresholdHeight = sizeTresholdHeight;
		this.sizeTresholdWidth = sizeTresholdWidth;
	}

	/**
	 * Creates the VIPS block trees and identifies the visual blocks.
	 */
	public void parse()
	{
	    for (VisualBlock rootBlock : rootArea.getBlockRoots())
	    {
    	    //construct the tree of blocks, one for each source box
	        rootBlock.reset();
	        rootBlock.setRoot(rootBlock); //the root block is the root of the whole subtree
    		constructVipsBlockTree(rootBlock);
    		//divide the blocks according to the block extraction algorithm
    		divideVipsBlockTree(rootBlock);
	    }
	}

	private void findVisualBlocks(VisualBlock vipsBlock, List<VisualBlock> list)
	{
		if (vipsBlock.isVisualBlock())
			list.add(vipsBlock);
		for (VisualBlock vipsStructureChild : vipsBlock.getChildren())
			findVisualBlocks(vipsStructureChild, list);
	}

	/**
	 * Selects all the extracted blocks from the tree of blocks.
	 * @return a list of extracted blocks
	 */
	public List<VisualBlock> getVisualBlocks()
	{
		List<VisualBlock> list = new ArrayList<VisualBlock>();
        for (VisualBlock rootBlock : rootArea.getBlockRoots())
            findVisualBlocks(rootBlock, list);
		return list;
	}

	/**
	 * Construct VIPS block tree starting at the root box.
	 * <p>
	 * Starts from &lt;body&gt; element.
	 * @param root Box that represents the current box
	 * @param rootBlock Visual structure tree node
	 */
	private void constructVipsBlockTree(VisualBlock rootBlock)
	{
		final Box root = rootBlock.getBox();
		if (root.getType() != Box.Type.TEXT_CONTENT)
		{
			for (Box child : root.getChildren())
			{
			    final VisualBlock childBlock = new VisualBlock();
			    childBlock.setRoot(rootBlock.getRoot());
			    childBlock.setBox(child);
				rootBlock.addChild(childBlock);
				constructVipsBlockTree(childBlock);
			}
		}
	}

	/**
	 * Tries to divide DOM elements and finds visual blocks.
	 * @param block Visual structure
	 */
	private void divideVipsBlockTree(VisualBlock block)
	{
		// With VIPS rules it tries to determine if element is dividable
		if (applyVipsRules(block) && block.isDividable() && !block.isVisualBlock())
		{
			// if element is dividable, let's divide it
			block.setAlreadyDivided(true);
			for (VisualBlock child : block.getChildren())
			{
		        divideVipsBlockTree(child);
			}
		}
		else
		{
		    //remove invalid blocks
			if (!verifyValidity(block))
			{
				block.setIsVisualBlock(false);
			}
		}
	}

	private boolean verifyValidity(VisualBlock block)
	{
	    if (!rootArea.getBounds().encloses(block.getBounds()))
	        return false;
	    
		if (block.getBounds().getWidth() <= 0 || block.getBounds().getHeight() <= 0) //TODO thresholds?
			return false;

		if (!block.getBox().isVisible())
			return false;

		return true;
	}

	/**
	 * Checks, if node is a valid node.
	 * <p>
	 * Node is valid, if it's visible in browser. This means, that the node's
	 * width and height are not zero.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if node is valid, otherwise false.
	 */
	private boolean isValidNode(Box node)
	{
	    if (node.getType() == Type.TEXT_CONTENT)
	        return true;
	    else if (node.getHeight() > 0 && node.getWidth() > 0)
			return true;
	    else
	        return false;
	}

	/**
	 * Checks, if node is a text node.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if node is a text node, otherwise false.
	 */
	private boolean isTextNode(Box box)
	{
		return (box.getType() == Type.TEXT_CONTENT || box.getType() == Type.REPLACED_CONTENT);
	}

	/**
	 * Checks, if node is a virtual text node.
	 * <p>
	 * Inline node with only text node children is a virtual text node.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if node is virtual text node, otherwise false.
	 */
	private boolean isVirtualTextNode1(Box node)
	{
		if (node.getDisplayType() == DisplayType.BLOCK)
		{
			return false;
		}
		else
		{
    		for (Box childNode : node.getChildren())
    		{
    			if (childNode.getType() != Type.TEXT_CONTENT)
    			{
    				return false;
    			}
    		}
		}
		return true;
	}

	/**
	 * Checks, if node is virtual text node.
	 * <p>
	 * Inline node with only text node and virtual text node children is a
	 * virtual text node.
	 *
	 * @param node
	 *            Input node
	 * 
	 * @return True, if node is virtual text node, otherwise false.
	 */
	private boolean isVirtualTextNode2(Box node)
	{
		if (node.getDisplayType() == DisplayType.BLOCK)
		{
			return false;
		}
		else
		{
    		for (Box childNode : node.getChildren())
    		{
    			if (!isTextNode(childNode) || !isVirtualTextNode1(childNode))
    				return false;
    		}
    		return true;
		}
	}

	/**
	 * Checks, if node is virtual text node.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if node is virtual text node, otherwise false.
	 */
	private boolean isVirtualTextNode(Box node)
	{
		return isVirtualTextNode1(node) || isVirtualTextNode2(node);
	}

	private int countValidChildNodes(Box node)
	{
	    int ret = 0;
		for (Box childNode : node.getChildren())
		{
			if (isValidNode(childNode))
			    ret++;
		}
		return ret;
	}
	
	/*
	 * Checks if node has valid children nodes
	 */
	private Box getFirstValidChildNode(Box node)
	{
        for (Box childNode : node.getChildren())
        {
            if (isValidNode(childNode))
                return childNode;
        }
        return null;
	}

	private boolean hasValidChildNodes(Box node)
	{
	    return getFirstValidChildNode(node) != null;
	}
	
	/**
	 * On different DOM nodes it applies different sets of VIPS rules.
	 * @param node DOM node
	 * @return Returns true if element is dividable, otherwise false.
	 */
	private boolean applyVipsRules(VisualBlock block)
	{
	    final Box node = block.getBox();
		boolean retVal = false;


		if (node.getDisplayType() == DisplayType.INLINE)
		{
			retVal = applyInlineTextNodeVipsRules(block);
		}
		else if ("table".equalsIgnoreCase(node.getTagName()))
		{
			retVal = applyTableNodeVipsRules(block);
		}
		else if ("tr".equalsIgnoreCase(node.getTagName()))
		{
			retVal = applyTrNodeVipsRules(block);
		}
		else if ("td".equalsIgnoreCase(node.getTagName()))
		{
			retVal = applyTdNodeVipsRules(block);
		}
		else if ("p".equalsIgnoreCase(node.getTagName()))
		{
			retVal = applyPNodeVipsRules(block);
		}
		else
		{
			retVal = applyOtherNodeVipsRules(block);
		}

		return retVal;
	}

	/**
	 * Applies VIPS rules on block nodes other than &lt;P&gt; &lt;TD&gt;
	 * &lt;TR&gt; &lt;TABLE&gt;.
	 * @param block Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyOtherNodeVipsRules(VisualBlock block)
	{
		// 1 2 3 4 6 8 9 11

		if (ruleOne(block))
			return true;

		if (ruleTwo(block))
			return true;

		if (ruleThree(block))
			return true;

		if (ruleFour(block))
			return true;

		if (ruleSix(block))
			return true;

		if (ruleEight(block))
			return true;

		if (ruleNine(block))
			return true;

		if (ruleEleven(block))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on &lt;P&gt; node.
	 * @param block Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyPNodeVipsRules(VisualBlock block)
	{
		// 1 2 3 4 5 6 8 9 11

		if (ruleOne(block))
			return true;

		if (ruleTwo(block))
			return true;

		if (ruleThree(block))
			return true;

		if (ruleFour(block))
			return true;

		if (ruleFive(block))
			return true;

		if (ruleSix(block))
			return true;

		if (ruleSeven(block))
			return true;

		if (ruleEight(block))
			return true;

		if (ruleNine(block))
			return true;

		if (ruleTen(block))
			return true;

		if (ruleEleven(block))
			return true;

		if (ruleTwelve(block))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on &lt;TD&gt; node.
	 * @param block Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyTdNodeVipsRules(VisualBlock block)
	{
		// 1 2 3 4 8 9 10 12

		if (ruleOne(block))
			return true;

		if (ruleTwo(block))
			return true;

		if (ruleThree(block))
			return true;

		if (ruleFour(block))
			return true;

		if (ruleEight(block))
			return true;

		if (ruleNine(block))
			return true;

		if (ruleTen(block))
			return true;

		if (ruleTwelve(block))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on &TR;&gt; node.
	 * @param block Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyTrNodeVipsRules(VisualBlock block)
	{
		// 1 2 3 7 9 12

		if (ruleOne(block))
			return true;

		if (ruleTwo(block))
			return true;

		if (ruleThree(block))
			return true;

		if (ruleSeven(block))
			return true;

		if (ruleNine(block))
			return true;

		if (ruleTwelve(block))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on &lt;TABLE&gt; node.
	 * @param block Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyTableNodeVipsRules(VisualBlock block)
	{
		// 1 2 3 7 9 12

		if (ruleOne(block))
			return true;

		if (ruleTwo(block))
			return true;

		if (ruleThree(block))
			return true;

		if (ruleSeven(block))
			return true;

		if (ruleNine(block))
			return true;

		if (ruleTwelve(block))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on inline nodes.
	 * @param block Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyInlineTextNodeVipsRules(VisualBlock block)
	{
		// 1 2 3 4 5 6 8 9 11

		if (ruleOne(block))
			return true;

		if (ruleTwo(block))
			return true;

		if (ruleThree(block))
			return true;

		if (ruleFour(block))
			return true;

		if (ruleFive(block))
			return true;

		if (ruleSix(block))
			return true;

		if (ruleEight(block))
			return true;

		if (ruleNine(block))
			return true;

		if (ruleTwelve(block))
			return true;

		return false;
	}

	/**
	 * VIPS Rule One
	 * <p>
	 * If the DOM node is not a text node and it has no valid children, then
	 * this node cannot be divided and will be cut.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleOne(VisualBlock block)
	{
	    final Box node = block.getBox();
		if (!isTextNode(node) && !hasValidChildNodes(node))
		{
			block.setIsDividable(false);
			return true;
		}
		else
		    return false;
	}

	/**
	 * VIPS Rule Two
	 * <p>
	 * If the DOM node has only one valid child and the child is not a text
	 * node, then divide this node
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleTwo(VisualBlock block)
	{
	    final Box node = block.getBox();
		if (countValidChildNodes(node) == 1)
		{
		    final Box child = getFirstValidChildNode(node);
			return !isTextNode(child);
		}
		return false;
	}

	/**
	 * VIPS Rule Three
	 * <p>
	 * If the DOM node is the root node of the sub-DOM tree (corresponding to
	 * the block), and there is only one sub DOM tree corresponding to this
	 * block, divide this node.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleThree(VisualBlock block)
	{
	    if (rootArea.getBlockRoots().size() == 1
	            && rootArea.getBlockRoots().get(0) == block)
	    {
	        return true;
	    }
	    return false;
	}

	/**
	 * VIPS Rule Four
	 * <p>
	 * If all of the child nodes of the DOM node are text nodes or virtual text
	 * nodes, do not divide the node. <br>
	 * If the font size and font weight of all these child nodes are same, set
	 * the DoC of the extracted block to 10.
	 * Otherwise, set the DoC of this extracted block to 9.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleFour(VisualBlock block)
	{
        final Box node = block.getBox();

        if (isTextNode(node)) 
        {
            //text node always succeeds - it has a consistent style and is not further dividable
            block.setIsVisualBlock(true);
            block.setIsDividable(false);
            block.setDoC(10);
            return true;
        }
        else if (node.getChildCount() > 0)
		{
		    //all children must be text nodes or virtual text nodes
    		for (Box box : node.getChildren())
    		{
    			if (!isTextNode(box) && !isVirtualTextNode(box))
    				return false;
    		}

    		block.setIsVisualBlock(true);
    		block.setIsDividable(false);

    		//determine the DoC
    		final float fw = node.getChildAt(0).getTextStyle().getFontWeight();
    		final float fs = node.getChildAt(0).getTextStyle().getFontSize();
    		boolean allEqual = true;
    		for (int i = 1; i < node.getChildCount() && allEqual; i++)
    		{
                float nfw = node.getChildAt(i).getTextStyle().getFontWeight();
                float nfs = node.getChildAt(i).getTextStyle().getFontSize();
    		    if (Math.abs(fw - nfw) > 0.01 && Math.abs(fs - nfs) > 0.01)
    		        allEqual = false;
    		}
    		block.setDoC(allEqual ? 10 : 9);
    		return true;
		}
		else
		    return false; //no children
	}

	/**
	 * VIPS Rule Five
	 * <p>
	 * If one of the child nodes of the DOM node is line-break node, then
	 * divide this DOM node.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleFive(VisualBlock block)
	{
        final Box node = block.getBox();

		for (Box childNode : node.getChildren())
		{
			if (childNode.getDisplayType() != DisplayType.INLINE)
				return true;
		}

		return false;
	}

	/**
	 * VIPS Rule Six
	 * <p>
	 * If one of the child nodes of the DOM node has HTML tag &lt;hr&gt;, then
	 * divide this DOM node
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleSix(VisualBlock block)
	{
        final Box node = block.getBox();
		for (Box child : node.getChildren())
		{
			if ("hr".equalsIgnoreCase(child.getTagName()))
				return true;
		}
		return false;
	}

	/**
	 * VIPS Rule Seven
	 * <p>
	 * If the background color of this node is different from one of its
	 * children’s, divide this node and at the same time, the child node with
	 * different background color will not be divided in this round.
	 * Set the DoC value (6-8) for the child node based on the &lt;html&gt;
	 * tag of the child node and the size of the child node.
	 * 
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleSeven(VisualBlock block)
	{
        final Box node = block.getBox();
        
		if (node.getChildren().isEmpty())
			return false;

		if (isTextNode(node))
			return false;

		String nodeBgColor = block.getBgColor();
		for (VisualBlock child : block.getChildren())
		{
			if (!(child.getBgColor().equals(nodeBgColor)))
			{
				child.setIsDividable(false);
				child.setIsVisualBlock(true);
				// TODO DoC values
				child.setDoC(7);
				return true;
			}
		}

		return false;
	}


	/**
	 * VIPS Rule Eight
	 * <p>
	 * If the node has at least one text node child or at least one virtual
	 * text node child, and the node's relative size is smaller than
	 * a threshold, then the node cannot be divided.
	 * Set the DoC value (from 5-8) based on the html tag of the node.
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleEight(VisualBlock block)
	{
        final Box node = block.getBox();
        
		if (node.getChildCount() > 0)
		{
    		if (node.getWidth() * node.getHeight() < sizeTresholdHeight * sizeTresholdWidth)
    		{
    		    boolean hasTextChild = false;
    		    for (Box child : node.getChildren())
    		    {
    		        if (isTextNode(child) || isVirtualTextNode(child))
    		        {
    		            hasTextChild = true;
    		            break;
    		        }
    		    }
    		    if (hasTextChild)
    		    {
            		block.setIsVisualBlock(true);
            		block.setIsDividable(false);
            
            		if ("Xdiv".equalsIgnoreCase(node.getTagName()))
            			block.setDoC(7);
            		else if ("code".equalsIgnoreCase(node.getTagName()))
            			block.setDoC(7);
            		else if ("div".equalsIgnoreCase(node.getTagName()))
            			block.setDoC(5);
            		else
            			block.setDoC(8);
            		return true;
    		    }
    		    else
    		        return false; //no text child
    		}
    		else
    		    return false; //not smaller than a treshold
		}
		else
		    return false; //no children
	}

	/**
	 * VIPS Rule Nine
	 * <p>
	 * If the child of the node with maximum size are small than
	 * a threshold (relative size), do not divide this node. <br>
	 * Set the DoC based on the html tag and size of this node.
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleNine(VisualBlock block)
	{
        final Box node = block.getBox();
        
		if (node.getChildCount() > 0)
		{
    		int maxSize = 0;
    		for (Box childNode : node.getChildren())
    		{
    			int childSize = childNode.getWidth() * childNode.getHeight();
    			if (maxSize < childSize)
    				maxSize = childSize;
    		}
    		if (maxSize < sizeTresholdWidth * sizeTresholdHeight)
    		{
        		//TODO set DOC
        		block.setIsVisualBlock(true);
        		block.setIsDividable(false);
        
        		if ("XDiv".equalsIgnoreCase(node.getTagName()))
        			block.setDoC(7);
        		if ("a".equalsIgnoreCase(node.getTagName()))
        			block.setDoC(11);
        		else
        			block.setDoC(8);
        		
                return true;
    		}
    		else
    		    return false; //not smaller than the threshold
		}
		else
		    return false;
	}

	/**
	 * VIPS Rule Ten
	 * <p>
	 * If previous sibling node has not been divided, do not divide this node
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleTen(VisualBlock block)
	{
        final Box node = block.getBox();

        if (node.getPreviousSibling() != null)
        {
            final VisualBlock siblingBlock = findBlockForBox(node.getPreviousSibling(), block.getRoot());
            if (siblingBlock != null && siblingBlock.isAlreadyDivided())
            {
                block.setIsDividable(true);
                return true;
            }
            else
                return false;
        }
        else
            return false;
	}

	/**
	 * VIPS Rule Eleven
	 * <p>
	 * Divide this node.
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleEleven(VisualBlock block)
	{
        return true;
	}

	/**
	 * VIPS Rule Twelve
	 * <p>
	 * Do not divide this node <br>
	 * Set the DoC value based on the html tag and size of this node.
	 * @param node
	 *            Input node
	 * 
	 * @return True, if rule is applied, otherwise false.
	 */
	private boolean ruleTwelve(VisualBlock block)
	{
        final Box node = block.getBox();

		block.setIsDividable(false);
		block.setIsVisualBlock(true);

		if ("XDiv".equalsIgnoreCase(node.getTagName()))
			block.setDoC(7);
		else if ("li".equalsIgnoreCase(node.getTagName()))
			block.setDoC(8);
		else if ("span".equalsIgnoreCase(node.getTagName()))
			block.setDoC(8);
		else if ("sup".equalsIgnoreCase(node.getTagName()))
			block.setDoC(8);
		else if ("img".equalsIgnoreCase(node.getTagName()))
			block.setDoC(8);
		else
			block.setDoC(333);
		//TODO DoC Part
		return true;
	}

	/**
	 * @return the _sizeTresholdWidth
	 */
	public int getSizeTresholdWidth()
	{
		return sizeTresholdWidth;
	}

	/**
	 * @param sizeTresholdWidth the _sizeTresholdWidth to set
	 */
	public void setSizeTresholdWidth(int sizeTresholdWidth)
	{
		this.sizeTresholdWidth = sizeTresholdWidth;
	}

	/**
	 * @return the _sizeTresholdHeight
	 */
	public int getSizeTresholdHeight()
	{
		return sizeTresholdHeight;
	}

	/**
	 * @param sizeTresholdHeight the _sizeTresholdHeight to set
	 */
	public void setSizeTresholdHeight(int sizeTresholdHeight)
	{
		this.sizeTresholdHeight = sizeTresholdHeight;
	}

	/**
	 * Finds a vips block in a subtree that contains the given box.
	 * @param node the box to find
	 * @param root Subtree root
	 * @returns VIPS block for the gien block or {@code null}
	 */
	private VisualBlock findBlockForBox(Box node, VisualBlock root)
	{
		if (root.getBox().equals(node))
		{
			return root;
		}
		else
		{
			for (VisualBlock child : root.getChildren())
			{
				final VisualBlock found = findBlockForBox(node, child);
				if (found != null)
				    return found;
			}
			return null;
		}
	}
}
