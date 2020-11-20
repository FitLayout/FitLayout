/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsParser.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Box.DisplayType;
import cz.vutbr.fit.layout.model.Box.Type;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Class that parses blocks on page and finds visual blocks.
 * @author Tomas Popela
 * @author burgetr
 */
public class VipsParser 
{
    //source page
    private Page page = null;

	private VipsBlock rootBlock = null;

	private int sizeTresholdWidth = 0;
	private int sizeTresholdHeight = 0;
	private int pageWidth = 0;
	private int pageHeight = 0;

	/**
	 * Default constructor
	 * 
	 * @param page Rendered's page viewport
	 */
	public VipsParser(Page page) 
	{
		this.page = page;
		this.rootBlock = new VipsBlock();
		this.sizeTresholdHeight = 80;
		this.sizeTresholdWidth = 80;
		this.pageWidth = page.getWidth();
		this.pageHeight = page.getHeight();
	}

	/**
	 * Constructor, where we can define element's size treshold
	 * @param page	Rendered page
	 * @param sizeTresholdWidth Element's width treshold
	 * @param sizeTresholdHeight Element's height treshold
	 */
	public VipsParser(Page page, int sizeTresholdWidth, int sizeTresholdHeight) 
	{
	    this(page);
		this.sizeTresholdHeight = sizeTresholdHeight;
		this.sizeTresholdWidth = sizeTresholdWidth;
	}

	/**
	 * Starts visual page segmentation on given page
	 */
	public void parse()
	{
		this.rootBlock = new VipsBlock();
		constructVipsBlockTree(page.getRoot(), rootBlock);
		divideVipsBlockTree(rootBlock);
	}

	private void findVisualBlocks(VipsBlock vipsBlock, List<VipsBlock> list)
	{
		if (vipsBlock.isVisualBlock())
			list.add(vipsBlock);

		for (VipsBlock vipsStructureChild : vipsBlock.getChildren())
			findVisualBlocks(vipsStructureChild, list);
	}

	public List<VipsBlock> getVisualBlocks()
	{
		List<VipsBlock> list = new ArrayList<VipsBlock>();
		findVisualBlocks(rootBlock, list);

		return list;
	}

	/**
	 * Construct VIPS block tree from viewport.
	 * <p>
	 * Starts from &lt;body&gt; element.
	 * @param element Box that represents element
	 * @param node Visual structure tree node
	 */
	private void constructVipsBlockTree(Box element, VipsBlock node)
	{
		node.setBox(element);
		if (element.getType() != Box.Type.TEXT_CONTENT)
		{
			for (Box box : element.getChildren())
			{
				node.addChild(new VipsBlock());
				constructVipsBlockTree(box, node.getChildren().get(node.getChildren().size()-1));
			}
		}
	}

	/**
	 * Tries to divide DOM elements and finds visual blocks.
	 * @param block Visual structure
	 */
	private void divideVipsBlockTree(VipsBlock block)
	{
		// With VIPS rules it tries to determine if element is dividable
		if (applyVipsRules(block) && block.isDividable() && !block.isVisualBlock())
		{
			// if element is dividable, let's divide it
			block.setAlreadyDivided(true);
			for (VipsBlock child : block.getChildren())
			{
				if (child.getBox().getType() == Box.Type.TEXT_CONTENT)
				{
				  //TODO process text nodes somewhere
				}
				else
				{
				    if (!child.isPreventDivision())
				        divideVipsBlockTree(child);
				    //TODO re-enable division for further rounds?
				}
				
			}
		}
		else
		{
			if (block.isDividable())
			{
				block.setIsVisualBlock(true);
				block.setDoC(11);
			}

			if (!verifyValidity(block.getBox()))
			{
				block.setIsVisualBlock(false);
			}
		}
	}

	private boolean verifyValidity(Box node)
	{
	    final Rectangular bounds = node.getContentBounds();
	    
		if (bounds.getX1() < 0 || bounds.getY1() < 0)
			return false;

		if (node.getX2() > pageWidth)
		{
			return false;
			//System.out.println("X " + node.getAbsoluteContentX() + "\t" + (node.getAbsoluteContentX() + node.getContentWidth()) + "\t" + _pageWidth);
		}

		if (node.getY2() > pageHeight)
		{
			return false;
			//System.out.println("Y " + node.getAbsoluteContentY() + "\t" + (node.getAbsoluteContentY() + node.getContentHeight()) + "\t" + _pageHeight);
		}

		if (node.getWidth() <= 0 || node.getHeight() <= 0)
			return false;

		if (!node.isVisible())
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
		/*if ("img".equalsIgnoreCase(node.getTagName()) || "input".equalsIgnoreCase(node.getTagName()))
		{
			if (node.getContentBounds().getWidth() > 0 && node.getContentBounds().getHeight() > 0)
			{
				_currentVipsBlock.setIsVisualBlock(true);
				_currentVipsBlock.setDoC(8);
				return true;
			}
			else
				return false;
		}*/ //TODO treat this elsewhere

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
	private boolean applyVipsRules(VipsBlock block)
	{
	    final Box node = block.getBox();
		boolean retVal = false;

		//System.err.println("Applying VIPS rules on " + node.getNode().getNodeName() + " node");

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
	private boolean applyOtherNodeVipsRules(VipsBlock block)
	{
        if (block.toString().contains("screenshots"))
            System.out.println("jo!");
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
	private boolean applyPNodeVipsRules(VipsBlock block)
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
	private boolean applyTdNodeVipsRules(VipsBlock block)
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
	private boolean applyTrNodeVipsRules(VipsBlock block)
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
	private boolean applyTableNodeVipsRules(VipsBlock block)
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
	private boolean applyInlineTextNodeVipsRules(VipsBlock block)
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
	private boolean ruleOne(VipsBlock block)
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
	private boolean ruleTwo(VipsBlock block)
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
	private boolean ruleThree(VipsBlock block)
	{
	    //TODO this is not very clear
	    if (block.getBox().isRoot())
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
	private boolean ruleFour(VipsBlock block)
	{
        final Box node = block.getBox();

		if (node.getChildCount() > 0)
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
	private boolean ruleFive(VipsBlock block)
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
	private boolean ruleSix(VipsBlock block)
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
	private boolean ruleSeven(VipsBlock block)
	{
        final Box node = block.getBox();
        
		if (node.getChildren().isEmpty())
			return false;

		if (isTextNode(node))
			return false;

		String nodeBgColor = block.getBgColor();
		for (VipsBlock child : block.getChildren())
		{
			if (!(child.getBgColor().equals(nodeBgColor)))
			{
				child.setIsDividable(false);
				child.setIsVisualBlock(true);
                child.setPreventDivision(true);
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
	private boolean ruleEight(VipsBlock block)
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
	private boolean ruleNine(VipsBlock block)
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
	private boolean ruleTen(VipsBlock block)
	{
        final Box node = block.getBox();

        if (node.getPreviousSibling() != null)
        {
            final VipsBlock siblingBlock = findBlockForBox(node.getPreviousSibling(), rootBlock);
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
	private boolean ruleEleven(VipsBlock block)
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
	private boolean ruleTwelve(VipsBlock block)
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

	public VipsBlock getVipsBlocks()
	{
		return rootBlock;
	}

	/**
	 * Finds a vips block in a subtree that contains the given box.
	 * @param node the box to find
	 * @param root Subtree root
	 * @returns VIPS block for the gien block or {@code null}
	 */
	private VipsBlock findBlockForBox(Box node, VipsBlock root)
	{
		if (root.getBox().equals(node))
		{
			return root;
		}
		else
		{
			for (VipsBlock child : root.getChildren())
			{
				final VipsBlock found = findBlockForBox(node, child);
				if (found != null)
				    return found;
			}
			return null;
		}
	}
}
