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
 *
 */
public class VipsParser {

	private VipsBlock _vipsBlocks = null;
	private VipsBlock _currentVipsBlock = null;
	private VipsBlock _tempVipsBlock = null;

	private int _sizeTresholdWidth = 0;
	private int _sizeTresholdHeight = 0;
	private Page _page = null;
	private int _visualBlocksCount = 0;
	private int _pageWidth = 0;
	private int _pageHeight = 0;

	/**
	 * Default constructor
	 * 
	 * @param viewport Rendered's page viewport
	 */
	public VipsParser(Page viewport) {
		this._page = viewport;
		this._vipsBlocks = new VipsBlock();
		this._sizeTresholdHeight = 80;
		this._sizeTresholdWidth = 80;
		this._pageWidth = viewport.getWidth();
		this._pageHeight = viewport.getHeight();
	}

	/**
	 * Constructor, where we can define element's size treshold
	 * @param viewport	Rendered's page viewport
	 * @param sizeTresholdWidth Element's width treshold
	 * @param sizeTresholdHeight Element's height treshold
	 */
	public VipsParser(Page viewport, int sizeTresholdWidth, int sizeTresholdHeight) {
		this._page = viewport;
		this._vipsBlocks = new VipsBlock();
		this._sizeTresholdHeight = sizeTresholdHeight;
		this._sizeTresholdWidth = sizeTresholdWidth;
	}

	/**
	 * Starts visual page segmentation on given page
	 */
	public void parse()
	{
		if (_page != null)
		{
			this._vipsBlocks = new VipsBlock();
			_visualBlocksCount = 0;

			constructVipsBlockTree(_page.getRoot(), _vipsBlocks);
			divideVipsBlockTree(_vipsBlocks);

			getVisualBlocksCount(_vipsBlocks);
			//System.err.println(String.valueOf("We have " + _visualBlocksCount + " visual blocks."));
		}
		else
			System.err.print("Page's viewPort is not defined");
	}

	/**
	 * Counts number of visual blocks in visual structure
	 * @param vipsBlock Visual structure
	 */
	private void getVisualBlocksCount(VipsBlock vipsBlock)
	{
		if (vipsBlock.isVisualBlock())
			_visualBlocksCount++;

		for (VipsBlock vipsBlockChild : vipsBlock.getChildren())
		{
			if (vipsBlockChild.getBox().getType() != Box.Type.TEXT_CONTENT)
				getVisualBlocksCount(vipsBlockChild);
		}
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
		findVisualBlocks(_vipsBlocks, list);

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
	 * @param vipsBlock Visual structure
	 */
	private void divideVipsBlockTree(VipsBlock vipsBlock)
	{
		_currentVipsBlock = vipsBlock;
		Box elementBox = vipsBlock.getBox();

		// With VIPS rules it tries to determine if element is dividable
		if (applyVipsRules(elementBox) && vipsBlock.isDividable() && !vipsBlock.isVisualBlock())
		{
			// if element is dividable, let's divide it
			_currentVipsBlock.setAlreadyDivided(true);
			for (VipsBlock vipsBlockChild : vipsBlock.getChildren())
			{
				if (vipsBlockChild.getBox().getType() != Box.Type.TEXT_CONTENT)
					divideVipsBlockTree(vipsBlockChild);
			}
		}
		else
		{
			if (vipsBlock.isDividable())
			{
				//System.err.println("Element " + elementBox.getNode().getNodeName() + " is visual block");
				vipsBlock.setIsVisualBlock(true);
				vipsBlock.setDoC(11);
			}

			if (!verifyValidity(elementBox))
			{
				_currentVipsBlock.setIsVisualBlock(false);
			}
			/*
			if (vipsBlock.isVisualBlock())
				//System.err.println("Element " + elementBox.getNode().getNodeName() + " is visual block");
			else
				System.err.println("Element " + elementBox.getNode().getNodeName() + " is not visual block");*/
		}
	}

	private int getAllTextLength(Box node)
	{
		List<Box> childrenTextNodes = new ArrayList<Box>();

		findTextChildrenNodes(node, childrenTextNodes);

		int textLength = 0;

		for (Box child : childrenTextNodes)
		{
			String childText = child.getOwnText();

			if (!childText.equals("") && !childText.equals(" ") && !childText.equals("\n"))
				textLength += childText.length();
		}

		return textLength;
	}

	private void getAllChildren(Box node, List<Box> children)
	{
		children.add(node);

		if (node.getType() == Box.Type.TEXT_CONTENT)
			return;

		for (Box child : node.getChildren())
		{
			getAllChildren(child, children);
		}
	}

	private boolean verifyValidity(Box node)
	{
	    final Rectangular bounds = node.getBounds();
	    
		if (bounds.getX1() < 0 || bounds.getY1() < 0)
			return false;

		if (node.getX2() > _pageWidth)
		{
			return false;
			//System.out.println("X " + node.getAbsoluteContentX() + "\t" + (node.getAbsoluteContentX() + node.getContentWidth()) + "\t" + _pageWidth);
		}

		if (node.getY2() > _pageHeight)
		{
			return false;
			//System.out.println("Y " + node.getAbsoluteContentY() + "\t" + (node.getAbsoluteContentY() + node.getContentHeight()) + "\t" + _pageHeight);
		}

		if (node.getWidth() <= 0 || node.getHeight() <= 0)
			return false;

		if (!node.isVisible())
			return false;

		if (getAllTextLength(node) == 0)
		{
			List<Box> children = new ArrayList<Box>();

			getAllChildren(node, children);

			for (Box child : children)
			{
				String childNodeName = child.getTagName();

				if (!child.isVisible())
					continue;

				if (childNodeName.equals("img"))
					return true;
				if (childNodeName.equals("input"))
					return true;
			}

			return false;
		}

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
		if (node.getHeight() > 0 && node.getWidth() > 0)
			return true;

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
		return (box.getType() == Type.TEXT_CONTENT);
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
			return false;

		for (Box childNode : node.getChildren())
		{
			if (childNode.getType() != Type.TEXT_CONTENT)
			{
				return false;
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
			return false;

		for (Box childNode : node.getChildren())
		{
			if (!isTextNode(childNode) || !isVirtualTextNode1(childNode))
				return false;
		}

		return true;
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
		if (isVirtualTextNode1(node))
			return true;
		if (isVirtualTextNode2(node))
			return true;

		return false;
	}

	int _cnt = 0;

	private void checkValidChildrenNodes(Box node)
	{
		if (node.getType() == Type.TEXT_CONTENT)
		{
			if (!node.getOwnText().equals(" "))
			{
				_cnt++;
			}
			return;
		}
		else
		{
			if (isValidNode(node))
				_cnt++;
		}

		for (Box childNode : node.getChildren())
		{
			checkValidChildrenNodes(childNode);
		}
	}

	/*
	 * Checks if node has valid children nodes
	 */
	private boolean hasValidChildrenNodes(Box node)
	{
		if ("img".equalsIgnoreCase(node.getTagName()) || "input".equalsIgnoreCase(node.getTagName()))
		{
			if (node.getContentBounds().getWidth() > 0 && node.getContentBounds().getHeight() > 0)
			{
				_currentVipsBlock.setIsVisualBlock(true);
				_currentVipsBlock.setDoC(8);
				return true;
			}
			else
				return false;
		}

		if (node.getChildren().isEmpty())
			return false;

		_cnt = 0;

		for (Box child : node.getChildren())
		{
			checkValidChildrenNodes(child);
		}

		return (_cnt > 0) ? true : false;
	}

	/*
	 * Returns the number of node's valid children
	 */
	private int numberOfValidChildNodes(Box node)
	{
		_cnt = 0;

		if (node.getChildren().isEmpty())
			return _cnt;

		for (Box child : node.getChildren())
		{
			checkValidChildrenNodes(child);
		}

		return _cnt;
	}

	/**
	 * On different DOM nodes it applies different sets of VIPS rules.
	 * @param node DOM node
	 * @return Returns true if element is dividable, otherwise false.
	 */
	private boolean applyVipsRules(Box node)
	{
		boolean retVal = false;

		//System.err.println("Applying VIPS rules on " + node.getNode().getNodeName() + " node");

		if (node.getDisplayType() == DisplayType.INLINE)
		{
			retVal = applyInlineTextNodeVipsRules(node);
		}
		else if ("table".equalsIgnoreCase(node.getTagName()))
		{
			retVal = applyTableNodeVipsRules(node);
		}
		else if ("tr".equalsIgnoreCase(node.getTagName()))
		{
			retVal = applyTrNodeVipsRules(node);
		}
		else if ("td".equalsIgnoreCase(node.getTagName()))
		{
			retVal = applyTdNodeVipsRules(node);
		}
		else if ("p".equalsIgnoreCase(node.getTagName()))
		{
			retVal = applyPNodeVipsRules(node);
		}
		else
		{
			retVal = applyOtherNodeVipsRules(node);
		}

		return retVal;
	}

	/**
	 * Applies VIPS rules on block nodes other than &lt;P&gt; &lt;TD&gt;
	 * &lt;TR&gt; &lt;TABLE&gt;.
	 * @param node Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyOtherNodeVipsRules(Box node)
	{
		// 1 2 3 4 6 8 9 11

		if (ruleOne(node))
			return true;

		if (ruleTwo(node))
			return true;

		if (ruleThree(node))
			return true;

		if (ruleFour(node))
			return true;

		if (ruleSix(node))
			return true;

		if (ruleEight(node))
			return true;

		if (ruleNine(node))
			return true;

		if (ruleEleven(node))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on &lt;P&gt; node.
	 * @param node Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyPNodeVipsRules(Box node)
	{
		// 1 2 3 4 5 6 8 9 11

		if (ruleOne(node))
			return true;

		if (ruleTwo(node))
			return true;

		if (ruleThree(node))
			return true;

		if (ruleFour(node))
			return true;

		if (ruleFive(node))
			return true;

		if (ruleSix(node))
			return true;

		if (ruleSeven(node))
			return true;

		if (ruleEight(node))
			return true;

		if (ruleNine(node))
			return true;

		if (ruleTen(node))
			return true;

		if (ruleEleven(node))
			return true;

		if (ruleTwelve(node))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on &lt;TD&gt; node.
	 * @param node Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyTdNodeVipsRules(Box node)
	{
		// 1 2 3 4 8 9 10 12

		if (ruleOne(node))
			return true;

		if (ruleTwo(node))
			return true;

		if (ruleThree(node))
			return true;

		if (ruleFour(node))
			return true;

		if (ruleEight(node))
			return true;

		if (ruleNine(node))
			return true;

		if (ruleTen(node))
			return true;

		if (ruleTwelve(node))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on &TR;&gt; node.
	 * @param node Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyTrNodeVipsRules(Box node)
	{
		// 1 2 3 7 9 12

		if (ruleOne(node))
			return true;

		if (ruleTwo(node))
			return true;

		if (ruleThree(node))
			return true;

		if (ruleSeven(node))
			return true;

		if (ruleNine(node))
			return true;

		if (ruleTwelve(node))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on &lt;TABLE&gt; node.
	 * @param node Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyTableNodeVipsRules(Box node)
	{
		// 1 2 3 7 9 12

		if (ruleOne(node))
			return true;

		if (ruleTwo(node))
			return true;

		if (ruleThree(node))
			return true;

		if (ruleSeven(node))
			return true;

		if (ruleNine(node))
			return true;

		if (ruleTwelve(node))
			return true;

		return false;
	}

	/**
	 * Applies VIPS rules on inline nodes.
	 * @param node Node
	 * @return Returns true if one of rules success and node is dividable.
	 */
	private boolean applyInlineTextNodeVipsRules(Box node)
	{
		// 1 2 3 4 5 6 8 9 11

		if (ruleOne(node))
			return true;

		if (ruleTwo(node))
			return true;

		if (ruleThree(node))
			return true;

		if (ruleFour(node))
			return true;

		if (ruleFive(node))
			return true;

		if (ruleSix(node))
			return true;

		if (ruleEight(node))
			return true;

		if (ruleNine(node))
			return true;

		if (ruleTwelve(node))
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
	private boolean ruleOne(Box node)
	{
		//System.err.println("Applying rule One on " + node.getNode().getNodeName() + " node");

		if (!isTextNode(node))
		{
			if (!hasValidChildrenNodes(node))
			{
				_currentVipsBlock.setIsDividable(false);
				return true;
			}
		}

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
	private boolean ruleTwo(Box node)
	{
		//System.err.println("Applying rule Two on " + node.getNode().getNodeName() + " node");

		if (numberOfValidChildNodes(node) == 1)
		{
			if (node.getChildAt(0).getType() == Type.TEXT_CONTENT)
				return false;
			if (!isTextNode(node.getChildAt(0)))
				return true;
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
	private boolean ruleThree(Box node)
	{
		//System.err.println("Applying rule Three on " + node.getNode().getNodeName() + " node");

		if (!node.isRoot())
			return false;

		boolean result = true;
		int cnt = 0;

		for (VipsBlock vipsBlock : _vipsBlocks.getChildren())
		{
			if (vipsBlock.getBox().getTagName().equalsIgnoreCase(node.getTagName()))
			{
				result = true;
				isOnlyOneDomSubTree(node, vipsBlock.getBox(), result);

				if (result)
					cnt++;
			}
		}

		return (cnt == 1) ? true : false;
	}

	/**
	 * Checks if node's subtree is unique in DOM tree.
	 * @param pattern Node for comparing
	 * @param node Node from DOM tree
	 * @param result True if element is unique otherwise false
	 */
	private void isOnlyOneDomSubTree(Box pattern, Box node, boolean result) //TODO result?!
	{
		if (!pattern.getTagName().equalsIgnoreCase(node.getTagName()))
			result = false;

		if (pattern.getChildren().size() != node.getChildren().size())
			result = false;

		if (!result)
			return;

		for (int i = 0; i < pattern.getChildren().size(); i++)
		{
			isOnlyOneDomSubTree(pattern.getChildren().get(i), node.getChildren().get(i), result);
		}
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
	private boolean ruleFour(Box node)
	{
		//System.err.println("Applying rule Four on " + node.getNode().getNodeName() + " node");

		if (node.getChildren().isEmpty())
			return false;

		for (Box box : node.getChildren())
		{
			if (box.getType() == Type.TEXT_CONTENT)
				continue;
			if (!isTextNode(box) ||	!isVirtualTextNode(box))
				return false;
		}

		_currentVipsBlock.setIsVisualBlock(true);
		_currentVipsBlock.setIsDividable(false);

		if (node.getChildren().size() == 1)
		{
			/*
			if (node.getSubBox(0) instanceof TextBox)
			{
				_currentVipsBlock.setIsVisualBlock(false);
				_currentVipsBlock.setIsDividable(true);
				_currentVipsBlock.getChildren().get(0).setIsVisualBlock(true);
				_currentVipsBlock.getChildren().get(0).setIsDividable(false);
				_currentVipsBlock.getChildren().get(0).setDoC(11);
			}
			 */
			if ("em".equals(node.getChildAt(0).getTagName()))
				_currentVipsBlock.setDoC(11);
			else
				_currentVipsBlock.setDoC(10);
			return true;
		}

		String fontWeight = "";
		int fontSize = 0;

		for (Box childNode : node.getChildren())
		{
			int childFontSize = Math.round(childNode.getTextStyle().getFontSize());

			if (childNode.getType() == Type.TEXT_CONTENT)
			{
				if (fontSize > 0)
				{
					if (fontSize != childFontSize)
					{
						_currentVipsBlock.setDoC(9);
						break;
					}
					else
						_currentVipsBlock.setDoC(10);
				}
				else
					fontSize = childFontSize;
				continue;
			}

			if (fontSize > 0)
			{
				if (Utils.fontWeight(childNode).equals(fontWeight) && childFontSize == fontSize)
				{
					_currentVipsBlock.setDoC(10);
				}
				else
				{
					_currentVipsBlock.setDoC(9);
					break;
				}
			}
			else
			{
				fontWeight = Utils.fontWeight(childNode);
				fontSize = childFontSize;
			}
		}

		return true;
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
	private boolean ruleFive(Box node)
	{
		//System.err.println("Applying rule Five on " + node.getNode().getNodeName() + " node");

		if (node.getChildren().isEmpty())
			return false;

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
	private boolean ruleSix(Box node)
	{
		//System.err.println("Applying rule Six on " + node.getNode().getNodeName() + " node");
		if (node.getChildren().isEmpty())
			return false;

		List<Box> children = new ArrayList<Box>();
		getAllChildren(node, children);

		for (Box child : children)
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
	private boolean ruleSeven(Box node)
	{
		//System.err.println("Applying rule Seven on " + node.getNode().getNodeName() + " node");
		if (node.getChildren().isEmpty())
			return false;

		if (isTextNode(node))
			return false;

		//String nodeBgColor = node.getStylePropertyValue("background-color");
		String nodeBgColor = _currentVipsBlock.getBgColor();

		for (VipsBlock vipsStructureChild : _currentVipsBlock.getChildren())
		{
			if (!(vipsStructureChild.getBgColor().equals(nodeBgColor)))
			{
				vipsStructureChild.setIsDividable(false);
				vipsStructureChild.setIsVisualBlock(true);
				// TODO DoC values
				vipsStructureChild.setDoC(7);
				return true;
			}
		}

		return false;
	}


	private void findTextChildrenNodes(Box node, List<Box> results)
	{
		if (node.getType() == Type.TEXT_CONTENT)
		{
			results.add(node);
			return;
		}

		for (Box childNode : node.getChildren())
		{
			findTextChildrenNodes(childNode, results);
		}
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
	private boolean ruleEight(Box node)
	{
		//System.err.println("Applying rule Eight on " + node.getNode().getNodeName() + " node");
		if (node.getChildren().isEmpty())
			return false;


		List<Box> children = new ArrayList<Box>();

		findTextChildrenNodes(node, children);

		int cnt = children.size();

		if (cnt == 0)
			return false;

		if (node.getWidth() == 0 || node.getHeight() == 0)
		{
			children.clear();

			getAllChildren(node, children);

			for (Box child : children)
			{
				if (child.getWidth() != 0 && child.getHeight() != 0)
					return true;
			}
		}

		if (node.getWidth() * node.getHeight() > _sizeTresholdHeight * _sizeTresholdWidth)
			return false;

		if ("ul".equalsIgnoreCase(node.getTagName()))
		{
			return true;
		}

		_currentVipsBlock.setIsVisualBlock(true);
		_currentVipsBlock.setIsDividable(false);

		if ("Xdiv".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(7);
		else if ("code".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(7);
		else if ("div".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(5);
		else
			_currentVipsBlock.setDoC(8);
		return true;
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
	private boolean ruleNine(Box node)
	{
		//System.err.println("Applying rule Nine on " + node.getNode().getNodeName() + " node");
		if (node.getChildren().isEmpty())
			return false;

		int maxSize = 0;

		for (Box childNode : node.getChildren())
		{
			int childSize = childNode.getWidth() * childNode.getHeight();

			if (maxSize < childSize)
			{
				maxSize = childSize;
			}
		}

		if (maxSize > _sizeTresholdWidth * _sizeTresholdHeight)
			return true;

		//TODO set DOC
		_currentVipsBlock.setIsVisualBlock(true);
		_currentVipsBlock.setIsDividable(false);

		if ("XDiv".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(7);
		if ("a".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(11);
		else
			_currentVipsBlock.setDoC(8);

		return true;
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
	private boolean ruleTen(Box node)
	{
		//System.err.println("Applying rule Ten on " + node.getNode().getNodeName() + " node");

		//VipsBlock previousSiblingVipsBlock = null;
		//findPreviousSiblingNodeVipsBlock(node.getNode().getPreviousSibling(), _vipsBlocks, previousSiblingVipsBlock);

		_tempVipsBlock = null;
		findPreviousSiblingNodeVipsBlock(node.getPreviousSibling(), _vipsBlocks);

		if (_tempVipsBlock == null)
			return false;

		if (_tempVipsBlock.isAlreadyDivided())
			return true;

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
	private boolean ruleEleven(Box node)
	{
		//System.err.println("Applying rule Eleven on " + node.getNode().getNodeName() + " node");

		return (isTextNode(node)) ? false : true;
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
	private boolean ruleTwelve(Box node)
	{
		//System.err.println("Applying rule Twelve on " + node.getNode().getNodeName() + " node");

		_currentVipsBlock.setIsDividable(false);
		_currentVipsBlock.setIsVisualBlock(true);

		if ("XDiv".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(7);
		else if ("li".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(8);
		else if ("span".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(8);
		else if ("sup".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(8);
		else if ("img".equalsIgnoreCase(node.getTagName()))
			_currentVipsBlock.setDoC(8);
		else
			_currentVipsBlock.setDoC(333);
		//TODO DoC Part
		return true;
	}

	/**
	 * @return the _sizeTresholdWidth
	 */
	public int getSizeTresholdWidth()
	{
		return _sizeTresholdWidth;
	}

	/**
	 * @param sizeTresholdWidth the _sizeTresholdWidth to set
	 */
	public void setSizeTresholdWidth(int sizeTresholdWidth)
	{
		this._sizeTresholdWidth = sizeTresholdWidth;
	}

	/**
	 * @return the _sizeTresholdHeight
	 */
	public int getSizeTresholdHeight()
	{
		return _sizeTresholdHeight;
	}

	/**
	 * @param sizeTresholdHeight the _sizeTresholdHeight to set
	 */
	public void setSizeTresholdHeight(int sizeTresholdHeight)
	{
		this._sizeTresholdHeight = sizeTresholdHeight;
	}

	public VipsBlock getVipsBlocks()
	{
		return _vipsBlocks;
	}

	/**
	 * Finds previous sibling node's VIPS block.
	 * @param node Node
	 * @param vipsBlock Actual VIPS block
	 * @param foundBlock VIPS block for given node
	 */
	private void findPreviousSiblingNodeVipsBlock(Box node, VipsBlock vipsBlock)
	{
		if (vipsBlock.getBox().equals(node))
		{
			_tempVipsBlock = vipsBlock;
			return;
		}
		else
			for (VipsBlock vipsBlockChild : vipsBlock.getChildren())
				findPreviousSiblingNodeVipsBlock(node, vipsBlockChild);
	}
}