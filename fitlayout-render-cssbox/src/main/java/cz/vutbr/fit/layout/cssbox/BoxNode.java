/**
 * BoxNode.java
 *
 * Created on 2.6.2006, 11:39:46 by burgetr
 */
package cz.vutbr.fit.layout.cssbox;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fit.cssbox.layout.BlockReplacedBox;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.InlineReplacedBox;
import org.fit.cssbox.layout.ListItemBox;
import org.fit.cssbox.layout.ReplacedBox;
import org.fit.cssbox.layout.ReplacedContent;
import org.fit.cssbox.layout.ReplacedImage;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.layout.Viewport;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import cz.vutbr.fit.layout.impl.DefaultTreeNode;
import cz.vutbr.fit.layout.model.Border;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.ContentObject;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Border.Side;
import cz.vutbr.fit.layout.model.Border.Style;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.TermColor;
import cz.vutbr.web.css.CSSProperty.BorderStyle;

/**
 * A node of a tree of visual blocks.
 * 
 * @author burgetr
 */
public class BoxNode extends DefaultTreeNode<cz.vutbr.fit.layout.model.Box> implements cz.vutbr.fit.layout.model.Box
{
    /** Overlapping threshold - the corners are considered to overlap if the boxes
     *  share more than OVERLAP pixels */
    private static final int OVERLAP = 2;
    
    /** Which percentage of the box area must be inside of another box in order
     * to consider it as a child box (from 0 to 1) */
    private static final double AREAP = 0.8;
    
    /** The CSSBox box that forms this node */
    protected Box box;
    
    /** The page this box belongs to. */
    protected Page page;
    
    /** Order in the box tree. It is assumed that the box tree obtained from the
     * rendering engine is sorted so that the siblings in the bottom are before the
     * siblings in front (i.e. in the drawing order). Usually, this corresponds
     * to the order in the document code. The order value doesn't correspond
     * to the order value of the source box because the box order values correspond
     * to the box creation order and not to the drawing order. */
    protected int order;
    
    /** The transformation that should be applied to the box */
    protected BoxTransform transform;
    
    /** The total bounds of the box node. Normally, the bounds are the same
     * as the content bounds. However, the BoxNode may be extended
     * in order to enclose all the overlapping boxes */
    protected Rectangular bounds;
    
    /** The total content bounds of the node. */
    protected Rectangular content;
    
    /** Visual bounds of the node. */
    protected Rectangular visual = null;
    
    /** Is the box separated by background */
    protected boolean backgroundSeparated = false;
    
    /** Efficient background color */
    protected Color efficientBackground = null;
    
    /** Potential nearest parent node in the box tree */
    public BoxNode nearestParent = null;
    
    /** Zoom relative to original box sizes */
    public float zoom;
    
    //===================================================================================
    
    /**
     * Creates a new node containing a box.
     * @param box the contained box
     */
    public BoxNode(Box box, Page page, float zoom)
    {
        super(cz.vutbr.fit.layout.model.Box.class);
        this.box = box;
        this.page = page;
        this.zoom = zoom;
        //copy the bounds from the box
        if (box != null)
        {
            transform = new BoxTransform(box);
	        content = computeContentBounds();
	        bounds = new Rectangular(content); //later, this will be recomputed using recomputeBounds()
        }
    }

    @Override
    public int getId()
    {
        return getOrder();
    }
    
    /**
     * @return the order in the display tree
     */
    public int getOrder()
    {
        return order;
    }

    /**
     * @param order the display tree order to set
     */
    public void setOrder(int order)
    {
        this.order = order;
    }

    public boolean isRootNode()
    {
        //System.out.println(this + " => " + this.nearestParent);
        return nearestParent == null;
    }
    
    @Override
    public String toString()
    {
        Box box = getBox();
        String ret = "";
        if (efficientBackground != null)
            ret += (box != null && isVisuallySeparated()) ? "+" : "-";
        ret += order + ": ";
        if (box == null)
            ret += "- empty -";
        else if (box instanceof Viewport)
            ret += box.toString();
        else if (box instanceof ElementBox)
        {
            ElementBox elem = (ElementBox) box;
            ret += elem.getElement().getTagName();
            ret += " [" + elem.getElement().getAttribute("id") + "]";
            ret += " [" + elem.getElement().getAttribute("class") + "]";
            ret += " B" + getBounds().toString();
            ret += " V" + getVisualBounds().toString();
        }
        else if (box instanceof TextBox)
        {
            ret = ((TextBox) box).getText();
            ret += " (" + box.getAbsoluteBounds().x + ","
            			+ box.getAbsoluteBounds().y + ","
            			+ (box.getAbsoluteBounds().x + box.getAbsoluteBounds().width - 1) + ","
            			+ (box.getAbsoluteBounds().y + box.getAbsoluteBounds().height - 1) + ")";
        }
        else
            ret = "?: " + box.toString();
        
        return ret;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof BoxNode)
        {
            return ((BoxNode) obj).getBox() == getBox();
        }
        else
            return false;
    }
    
    @Override
    public boolean isBackgroundSeparated()
    {
        return backgroundSeparated;
    }

    public void setBackgroundSeparated(boolean backgroundSeparated)
    {
        this.backgroundSeparated = backgroundSeparated;
    }
    
    /**
     * @return the efficient background color
     */
    public Color getEfficientBackground()
    {
        return efficientBackground;
    }

    /**
     * @param bgcolor the efficientBackground to set
     */
    public void setEfficientBackground(Color bgcolor)
    {
        this.efficientBackground = bgcolor;
    }
    
    //===================================================================================


    /**
     * @return <code>true</code> if the box is visually separated by a border or
     * a different background color.
     */
    public boolean isVisuallySeparated()
    {
        Box box = getBox();

        //invisible boxes are not separated
        if (!isVisible()) 
            return false;
        //viewport is visually separated
        else if (box instanceof Viewport)
            return true;
        //non-empty text boxes are visually separated
        else if (box instanceof TextBox) 
        {
            if (box.isEmpty())
                return false;
            else
                return true;
        }
        //replaced boxes are visually separated
        else if (box instanceof InlineReplacedBox || box instanceof BlockReplacedBox)
        {
            return true;
        }
        //list item boxes with a bullet
        else if (box instanceof ListItemBox)
        {
            return ((ListItemBox) box).hasVisibleBullet();
        }
        //other element boxes
        else 
        {
            //check if separated by border -- at least one border needed
            if (getBorderCount() >= 1)
                return true;
            //check the background
            else if (isBackgroundSeparated())
                return true;
            return false;
        }
    }
    
    /**
     * Returns the minimal bounds of the box for enclosing all the contained boxes.
     * @return the minimal visual bounds
     */
    private Rectangular getMinimalVisualBounds()
    {
        final Box box = getBox();
        if (box instanceof TextBox)
            return new RectangularZ(box.getAbsoluteBounds().intersection(box.getClipBlock().getClippedContentBounds()), zoom);
        else if (box != null && box.isReplaced())
            return new RectangularZ(box.getMinimalAbsoluteBounds().intersection(box.getClipBlock().getClippedContentBounds()), zoom);
        else
        {
        	Rectangular ret = null;
            for (int i = 0; i < getChildCount(); i++)
            {
                BoxNode subnode = (BoxNode) getChildAt(i); 
                Box sub = subnode.getBox();
                Rectangular sb = subnode.getVisualBounds();
                if (sub.isDisplayed() && subnode.isVisible() && sb.getWidth() > 0 && sb.getHeight() > 0)
                {
	                if (ret == null)
	                	ret = new Rectangular(sb);
	                else
	                	ret.expandToEnclose(sb);
                }
            }
            //if nothing has been found return an empty rectangle at the top left corner
            if (ret == null)
            {
                Rectangle b = box.getAbsoluteBounds().intersection(box.getClipBlock().getClippedContentBounds());
            	return new RectangularZ(b.x, b.y, zoom);
            }
            else
            	return ret;
        }
    }
    
    /**
     * Returns the bounds of the box as they visually appear to the user.
     * @return the visual bounds
     */
    @Override
    public Rectangular getVisualBounds()
    {
        if (visual == null)
            visual = computeVisualBounds();
        return visual;
    }
    
    private Rectangular computeVisualBounds()
    {
        Box box = getBox();
        Rectangular ret = null;
        
        if (box instanceof Viewport)
        {
            ret = new RectangularZ(((Viewport) box).getClippedBounds(), zoom);
        }
        else if (box instanceof ElementBox)
        {
            ElementBox elem = (ElementBox) box;
            //one border only -- the box represents the border only
            if (getBorderCount() == 1 && !isBackgroundSeparated())
            {
            	Rectangular b = new RectangularZ(elem.getAbsoluteBorderBounds().intersection(elem.getClipBlock().getClippedContentBounds()), zoom); //clipped absolute bounds
            	if (hasTopBorder())
            		ret = new Rectangular(b.getX1(), b.getY1(), b.getX2(), b.getY1() + zoom(elem.getBorder().top) - 1);
            	else if (hasBottomBorder())
            		ret = new Rectangular(b.getX1(), b.getY2() - zoom(elem.getBorder().bottom) + 1, b.getX2(), b.getY2());
            	else if (hasLeftBorder())
            		ret = new Rectangular(b.getX1(), b.getY1(), b.getX1() + zoom(elem.getBorder().left) - 1, b.getY2());
            	else if (hasRightBorder())
            		ret = new Rectangular(b.getX2() - zoom(elem.getBorder().right) + 1, b.getY1(), b.getX2(), b.getY2());
            }
            //at least two borders or a border and background - take the border bounds
            else if (getBorderCount() >= 2 || (getBorderCount() == 1 && isBackgroundSeparated()))
            {
                ret = new RectangularZ(elem.getAbsoluteBorderBounds().intersection(elem.getClipBlock().getClippedContentBounds()), zoom);
            }
            
            //consider the background if different from the parent
            if (isBackgroundSeparated())
            {
                Rectangular bg = new RectangularZ(elem.getAbsoluteBackgroundBounds().intersection(elem.getClipBlock().getClippedContentBounds()), zoom);
                if (ret == null)
                    ret = bg;
                else
                    ret.expandToEnclose(bg);
            }
            //no visual separators, consider the contents
            else
            {
                Rectangular cont  = getMinimalVisualBounds();
                if (ret == null)
                    ret = cont;
                else
                    ret.expandToEnclose(cont);
            }
        }
        else //not an element
            ret = getMinimalVisualBounds();
        
        return ret;
    }
    
    /**
     * Re-computes the visual bounds of the whole subtree.
     */
    public void recomputeVisualBounds()
    {
        for (int i = 0; i < getChildCount(); i++)
            ((BoxNode) getChildAt(i)).recomputeVisualBounds();
        visual = computeVisualBounds();
    }
    
    /**
     * Recomputes the total bounds of the whole subtree. The bounds of each box will
     * correspond to its visual bounds. If the child boxes exceed the parent box,
     * the parent box bounds will be expanded accordingly.
     */
    public void recomputeBounds()
    {
        bounds = new Rectangular(visual);
        for (int i = 0; i < getChildCount(); i++)
        {
            BoxNode child = (BoxNode) getChildAt(i);
            child.recomputeBounds();
            expandToEnclose(child);
        }
    }
    
    /**
     * Computes node the content bounds. They correspond to the background bounds
     * however, when a border is present, it is included in the contents. Moreover,
     * the box is clipped by its clipping box.
     */
    private Rectangular computeContentBounds()
    {
        Box box = getBox();
        Rectangular ret = null;
        
        if (box instanceof Viewport)
        {
            ret = new RectangularZ(((Viewport) box).getClippedBounds(), zoom);
        }
        else if (box instanceof ElementBox)
        {
            ElementBox elem = (ElementBox) box;
            //at least one border - take the border bounds
            //TODO: when only one border is present, we shouldn't take the whole border box? 
            if (elem.getBorder().top > 0 || elem.getBorder().left > 0 ||
                elem.getBorder().bottom > 0 || elem.getBorder().right > 0)
            {
                ret = new RectangularZ(elem.getAbsoluteBorderBounds(), zoom);
            }
            //no border
            else
            {
                ret = new RectangularZ(elem.getAbsoluteBackgroundBounds(), zoom);
            }
        }
        else //not an element - return the whole box
            ret = new RectangularZ(box.getAbsoluteBounds(), zoom);

        //clip with the clipping bounds
        if (box.getClipBlock() != null)
        {
            Rectangular clip = new RectangularZ(box.getClipBlock().getClippedContentBounds(), zoom);
            ret = ret.intersection(clip);
        }
        
        return ret;
    }
    
    /**
     * @return the number of defined borders for the box
     */
    public int getBorderCount()
    {
        int bcnt = 0;
        if (hasTopBorder()) bcnt++;
        if (hasBottomBorder()) bcnt++;
        if (hasLeftBorder()) bcnt++;
        if (hasRightBorder()) bcnt++;
        return bcnt;
    }
    
    /**
     * @return <code>true</code> if the box has a top border
     */
    @Override
    public boolean hasTopBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox && ((ElementBox) box).getBorder().top > 0)
            return true;
        else
            return false;
    }
    
    /**
     * Obtains the top border of the box
     * @return the width of the border or 0 when there is no border
     */
    @Override
    public int getTopBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
            return ((ElementBox) box).getBorder().top;
        else
            return 0;
    }

    /**
     * @return <code>true</code> if the box has a bottom border
     */
    @Override
    public boolean hasBottomBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox && ((ElementBox) box).getBorder().bottom > 0)
            return true;
        else
            return false;
    }
    
    /**
     * Obtains the bottom border of the box
     * @return the width of the border or 0 when there is no border
     */
    @Override
    public int getBottomBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
            return ((ElementBox) box).getBorder().bottom;
        else
            return 0;
    }

    /**
     * @return <code>true</code> if the box has a left border
     */
    @Override
    public boolean hasLeftBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox && ((ElementBox) box).getBorder().left > 0)
            return true;
        else
            return false;
    }
    
    /**
     * Obtains the left border of the box
     * @return the width of the border or 0 when there is no border
     */
    @Override
    public int getLeftBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
            return ((ElementBox) box).getBorder().left;
        else
            return 0;
    }

    /**
     * @return <code>true</code> if the box has a right border
     */
    @Override
    public boolean hasRightBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox && ((ElementBox) box).getBorder().right > 0)
            return true;
        else
            return false;
    }
    
    /**
     * Obtains the right border of the box
     * @return the width of the border or 0 when there is no border
     */
    @Override
    public int getRightBorder()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
            return ((ElementBox) box).getBorder().right;
        else
            return 0;
    }

    @Override
    public Border getBorderStyle(Side side)
    {
        Box box = getBox();
        if (box instanceof ElementBox)
        {
            final NodeData style = ((ElementBox) box).getStyle();
            TermColor tclr = style.getValue(TermColor.class, "border-"+side+"-color");
            CSSProperty.BorderStyle bst = style.getProperty("border-"+side+"-style");
            if (bst == null)
                bst = BorderStyle.NONE;
            
            Color clr = null;
            if (tclr != null)
                clr = Units.toColor(tclr.getValue());
            if (clr == null)
            {
                clr = Units.toColor(box.getVisualContext().getColor());
                if (clr == null)
                    clr = Color.BLACK;
            }

            int rwidth = 0;
            switch (side)
            {
                case BOTTOM:
                    rwidth = getBottomBorder();
                    break;
                case LEFT:
                    rwidth = getLeftBorder();
                    break;
                case RIGHT:
                    rwidth = getRightBorder();
                    break;
                case TOP:
                    rwidth = getTopBorder();
                    break;
            }
            
            Border.Style rstyle;
            switch (bst)
            {
                case NONE:
                case HIDDEN:
                    rstyle = Style.NONE;
                    break;
                case DASHED:
                    rstyle = Style.DASHED;
                    break;
                case DOTTED:
                    rstyle = Style.DOTTED;
                    break;
                case DOUBLE:
                    rstyle = Style.DOUBLE;
                    break;
                default:
                    rstyle = Style.SOLID;
                    break;
            }
            
            return new Border(rwidth, rstyle, clr);
        }
        else
            return new Border();
    }

    /**
     * Get the effective text color. If the text color is set, it is returned.
     * When the color is not set, the parent boxes are considered.
     * @return the background color string
     */
    public String getEfficientColor()
    {
        Box box = getBox(); 
        do
        {
            if (box instanceof ElementBox)
            {
                String color = ((ElementBox) box).getStylePropertyValue("color");
                if (!color.equals(""))
                    return color;
            }
            box = box.getParent();
        } while (box != null);
        return "";
    }

    /**
     * Obtains the text color of the first box in the area.
     * @return The color.
     */
    public Color getStartColor()
    {
    	return Units.toColor(getBox().getVisualContext().getColor());
    }
    
    /**
     * Checks if the box text is visible (it does not contain spaces only). This is not equivalent to isWhitespace() because
     * non-beraking spaces are not whitespace characters but they are not visible.
     * @return <true> if the box text consists of space characters only
     */
    public boolean containsVisibleTextString()
    {
        String s = getText();
        for (int i = 0; i < s.length(); i++)
        {
            char ch = s.charAt(i);
            if (!Character.isSpaceChar(ch))
                return true;
        }
        return false;
    }
    
    /**
     * Checks whether the box is inside of the visible area and the text is visible and its color is different from the background
     * @return <code>true</code> if the box is visible
     */
    @Override
    public boolean isVisible()
    {
        return !getVisualBounds().isEmpty();
    }
    
    /**
     * Returns the declared background color of the element or null when transparent.
     */
    @Override
    public cz.vutbr.fit.layout.model.Color getBackgroundColor()
    {
        if (getBox() instanceof ElementBox)
        {
            Color clr = Units.toColor(((ElementBox) getBox()).getBgcolor());
            if (clr != null)
            {
                cz.vutbr.fit.layout.model.Color nclr = new cz.vutbr.fit.layout.model.Color(clr.getRed(), clr.getGreen(), clr.getBlue(), clr.getAlpha());
                return nclr;
            }
            else
                return null;
        }
        else
            return null;
    }
    
    public void applyTransform(BoxTransform trans)
    {
        if (!trans.isEmpty())
        {
            System.out.println("Old: " + bounds);
            bounds = trans.transformRect(bounds);
            System.out.println("New: " + bounds);
            if (content != null)
                content = trans.transformRect(content);
            if (visual != null)
                visual = trans.transformRect(visual);
        }
    }
    
    public void applyTransformRecursively(BoxTransform trans)
    {
        for (int i = 0; i < getChildCount(); i++)
            ((BoxNode) getChildAt(i)).applyTransformRecursively(trans);
    }
    
    public void applyTransforms()
    {
        //apply our transform recursively
        if (!transform.isEmpty())
        {
            applyTransform(transform);
            applyTransformRecursively(transform);
        }
        //apply their transforms to the children, remove the invisible ones
        //TODO this is a temporaty hack, it should be solved better
        List<BoxNode> toRemove = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++)
        {
            BoxNode child = (BoxNode) getChildAt(i); 
            child.applyTransforms();
            if (!this.getVisualBounds().intersects(child.getVisualBounds()))
                toRemove.add(child);
        }
        if (!toRemove.isEmpty())
        {
            for (BoxNode child : toRemove)
                removeChild(child);
            recomputeVisualBounds();
            visual = transform.transformRect(visual);
        }
    }
    
    //===================================================================================
    
    /**
     * @return the contained box
     */
    public Box getBox()
    {
        return box;
    }
    
    /**
	 * @return the total bounds of the node
	 */
    @Override
	public Rectangular getBounds()
	{
		return bounds;
	}

	/**
	 * @return the content bounds of the node
	 */
    @Override
	public Rectangular getContentBounds()
	{
		return content;
	}

	/** 
     * Checks if another node is located inside the visual bounds of this box.
     * @param childNode the node to check
     * @return <code>true</code> if the child node is completely inside this node, <code>false</code> otherwise 
     */
    public boolean visuallyEncloses(BoxNode childNode)
    {
        final int cx1 = childNode.getVisualBounds().getX1();
        final int cy1 = childNode.getVisualBounds().getY1();
        final int cx2 = childNode.getVisualBounds().getX2();
        final int cy2 = childNode.getVisualBounds().getY2();
        final int px1 = getVisualBounds().getX1();
        final int py1 = getVisualBounds().getY1();
        final int px2 = getVisualBounds().getX2();
        final int py2 = getVisualBounds().getY2();
        
        /*if (childNode.toString().contains("7 (45,765,64,801)") && this.toString().contains("+39"))
            System.out.println("jo!");*/
        
        //how many corners of the parent are inside of the child
        int rxcnt = 0;
        if (px1 >= cx1 && px1 <= cx2 &&
            py1 >= cy1 && py1 <= cy2) rxcnt++; //top left
        if (px2 >= cx1 && px2 <= cx2 &&
            py1 >= cy1 && py1 <= cy2) rxcnt++; //top right
        if (px1 >= cx1 && px1 <= cx2 &&
            py2 >= cy1 && py2 <= cy2) rxcnt++; //bottom left
        if (px2 >= cx1 && px2 <= cx2 &&
            py2 >= cy1 && py2 <= cy2) rxcnt++; //bottom right
        //shared areas
        final int shared = getVisualBounds().intersection(childNode.getVisualBounds()).getArea();
        final double sharedperc = (double) shared / childNode.getBounds().getArea();
        
        if (rxcnt == 4)
            return false; //reverse relation - the child contains the parent
        else
            return this.getOrder() < childNode.getOrder() && sharedperc >= AREAP;
    }

    public boolean visuallyEnclosesOld(BoxNode childNode)
    {
        final int cx1 = childNode.getVisualBounds().getX1();
        final int cy1 = childNode.getVisualBounds().getY1();
        final int cx2 = childNode.getVisualBounds().getX2();
        final int cy2 = childNode.getVisualBounds().getY2();
        final int px1 = getVisualBounds().getX1();
        final int py1 = getVisualBounds().getY1();
        final int px2 = getVisualBounds().getX2();
        final int py2 = getVisualBounds().getY2();
        
        /*if (childNode.toString().contains("7 (45,765,64,801)") && this.toString().contains("+39"))
            System.out.println("jo!");*/
        
        //check how many corners of the child are inside enough (with some overlap)
        int ccnt = 0;
        if (cx1 >= px1 + OVERLAP && cx1 <= px2 - OVERLAP &&
            cy1 >= py1 + OVERLAP && cy1 <= py2 - OVERLAP) ccnt++; //top left
        if (cx2 >= px1 + OVERLAP && cx2 <= px2 - OVERLAP &&
            cy1 >= py1 + OVERLAP && cy1 <= py2 - OVERLAP) ccnt++; //top right
        if (cx1 >= px1 + OVERLAP && cx1 <= px2 - OVERLAP &&
            cy2 >= py1 + OVERLAP && cy2 <= py2 - OVERLAP) ccnt++; //bottom left
        if (cx2 >= px1 + OVERLAP && cx2 <= px2 - OVERLAP &&
            cy2 >= py1 + OVERLAP && cy2 <= py2 - OVERLAP) ccnt++; //bottom right
        //check how many corners of the child are inside the parent exactly
        int xcnt = 0;
        if (cx1 >= px1 && cx1 <= px2 &&
            cy1 >= py1 && cy1 <= py2) xcnt++; //top left
        if (cx2 >= px1 && cx2 <= px2 &&
            cy1 >= py1 && cy1 <= py2) xcnt++; //top right
        if (cx1 >= px1 && cx1 <= px2 &&
            cy2 >= py1 && cy2 <= py2) xcnt++; //bottom left
        if (cx2 >= px1 && cx2 <= px2 &&
            cy2 >= py1 && cy2 <= py2) xcnt++; //bottom right
        //and reverse direction - how many corners of the parent are inside of the child
        int rxcnt = 0;
        if (px1 >= cx1 && px1 <= cx2 &&
            py1 >= cy1 && py1 <= cy2) rxcnt++; //top left
        if (px2 >= cx1 && px2 <= cx2 &&
            py1 >= cy1 && py1 <= cy2) rxcnt++; //top right
        if (px1 >= cx1 && px1 <= cx2 &&
            py2 >= cy1 && py2 <= cy2) rxcnt++; //bottom left
        if (px2 >= cx1 && px2 <= cx2 &&
            py2 >= cy1 && py2 <= cy2) rxcnt++; //bottom right
        //shared areas
        final int shared = getVisualBounds().intersection(childNode.getVisualBounds()).getArea();
        final double sharedperc = (double) shared / childNode.getBounds().getArea();
        
        //no overlap
        if (xcnt == 0)
            return false;
        //fully overlapping or over a corner - the order decides
        else if ((cx1 == px1 && cy1 == py1 && cx2 == px2 && cy2 == py2) //full overlap
                 || (ccnt == 1 && xcnt <= 1)) //over a corner
            return this.getOrder() < childNode.getOrder() && sharedperc >= AREAP;
        //fully inside
        else if (xcnt == 4)
            return true;
        //partly inside (at least two corners)
        else if (xcnt >= 2)
        {
            if (rxcnt == 4) //reverse relation - the child contains the parent
                return false;
            else //child partly inside the parent
                return this.getOrder() < childNode.getOrder() && sharedperc >= AREAP;
        }
        //not inside
        else
            return false;
    }
    
    /** 
     * Checks if another node is fully located inside the content bounds of this box.
     * @param childNode the node to check
     * @return <code>true</code> if the child node is completely inside this node, <code>false</code> otherwise 
     */
    public boolean contentEncloses(BoxNode childNode)
    {
    	//System.out.println(childNode + " => " + childNode.getVisualBounds());
        final int cx1 = childNode.getContentBounds().getX1();
        final int cy1 = childNode.getContentBounds().getY1();
        final int cx2 = childNode.getContentBounds().getX2();
        final int cy2 = childNode.getContentBounds().getY2();
        final int px1 = getContentBounds().getX1();
        final int py1 = getContentBounds().getY1();
        final int px2 = getContentBounds().getX2();
        final int py2 = getContentBounds().getY2();
        
        //check how many corners of the child are inside the parent exactly
        int xcnt = 0;
        if (cx1 >= px1 && cx1 <= px2 &&
            cy1 >= py1 && cy1 <= py2) xcnt++; //top left
        if (cx2 >= px1 && cx2 <= px2 &&
            cy1 >= py1 && cy1 <= py2) xcnt++; //top right
        if (cx1 >= px1 && cx1 <= px2 &&
            cy2 >= py1 && cy2 <= py2) xcnt++; //bottom left
        if (cx2 >= px1 && cx2 <= px2 &&
            cy2 >= py1 && cy2 <= py2) xcnt++; //bottom right
        
        if ((cx1 == px1 && cy1 == py1 && cx2 == px2 && cy2 == py2)) //exact overlap
           return this.getOrder() < childNode.getOrder();
        else
            return xcnt == 4;
    }
    
    /** 
     * Expands the box node in order to fully enclose another box 
     */
    public void expandToEnclose(BoxNode child)
    {
    	bounds.expandToEnclose(child.getBounds());
    }
    
    /**
     * Takes a list of nodes and selects the nodes that are located directly inside 
     * of this node's box. The {@code nearestParent} of the selected boxes is set to this box.
     * @param list the list of nodes to test
     * @param useVisualBounds when set to {@code true}, only the boxes within the visual bounds are considered.
     *          Otherwise, all the nodes within the box content bounds are considered.
     */
    public void markNodesInside(List<BoxNode> list, boolean useVisualBounds)
    {
        for (Iterator<BoxNode> it = list.iterator(); it.hasNext();)
        {
            BoxNode node = it.next();
            if (!useVisualBounds) //use the content bounds instead
            {
                if (node != this 
                    && this.contentEncloses(node)
                    && (node.isRootNode() || !this.contentEncloses(node.nearestParent))) 
                {
                    node.nearestParent = this;
                }
            }
            else
            {
                if (node != this 
                        && this.visuallyEncloses(node)
                        && (node.isRootNode() || !this.visuallyEncloses(node.nearestParent))) 
                {
                    node.nearestParent = this;
                }
            }
        }
    }
    
    /**
     * Takes a list of nodes and selects the nodes whose parent box is identical to this node's box. 
     * The {@code nearestParent} of the selected boxes is set to this box node.
     * @param list the list of nodes to test
     */
    public void markChildNodes(List<BoxNode> list)
    {
        final Box thisBox = this.getBox(); 
        for (Iterator<BoxNode> it = list.iterator(); it.hasNext();)
        {
            BoxNode node = it.next();
            if (node != this && node.getBox().getParent() == thisBox)
                node.nearestParent = this;
        }        
    }
    
    /**
     * Goes through the parent's children, takes all the nodes that are inside of this node
     * and makes them the children of this node. Then, recursively calls the children to take
     * their nodes.
     */
	public void takeChildren(List<BoxNode> list)
    {
        for (Iterator<BoxNode> it = list.iterator(); it.hasNext();)
        {
            BoxNode node = it.next();
            if (node.nearestParent.equals(this))    
            {
                appendChild(node);
                it.remove();
            }
        }
        
        //let the children take their children
        for (int i = 0; i < getChildCount(); i++)
            ((BoxNode) getChildAt(i)).takeChildren(list);
    }
    
    /**
     * Removes the node from the tree. Clears the parent and removes all the child
     * node.
     */
    public void removeFromTree()
    {
        nearestParent = null;
        //setParent(null);
        removeAllChildren();
    }
    
    //==================================================================================
    
    /**
     * @return all the text contained in this box and its subboxes.
     * Contents of the individual text boxes are separated by spaces.
     */
    @Override
    public String getText()
    {
        return recursiveGetText(this);
    }
    
    private String recursiveGetText(BoxNode root)
    {
        Box box = root.getBox();
        if (box instanceof TextBox)
            return ((TextBox) box).getText();
        else
        {
            String ret = "";
            for (int i = 0; i < root.getChildCount(); i++)
            {
                if (ret.trim().length() > 0)
                    ret += " ";
                ret = ret + recursiveGetText((BoxNode) root.getChildAt(i)).trim();
            }
            return ret;
        }
    }

    @Override
    public String getOwnText()
    {
        final Box box = getBox();
        if (box instanceof TextBox)
            return ((TextBox) box).getText();
        else
            return null;
    }

    //==================================================================================
    
    @Override
    public Page getPage()
    {
        return page;
    }

    @Override
    public float getUnderline()
    {
        return getBox().getVisualContext().getTextDecoration().contains(CSSProperty.TextDecoration.UNDERLINE) ? 1.0f : 0.0f;
    }

    @Override
    public float getLineThrough()
    {
        return getBox().getVisualContext().getTextDecoration().contains(CSSProperty.TextDecoration.LINE_THROUGH) ? 1.0f : 0.0f;
    }

    @Override
    public float getFontSize()
    {
        return getBox().getVisualContext().getFont().getSize2D();
    }

    @Override
    public float getFontStyle()
    {
        return getBox().getVisualContext().getFont().isItalic() ? 1.0f : 0.0f;
    }

    @Override
    public float getFontWeight()
    {
        return getBox().getVisualContext().getFont().isBold() ? 1.0f : 0.0f;
    }

    @Override
    public int getX1()
    {
        return getVisualBounds().getX1();
    }

    @Override
    public int getY1()
    {
        return getVisualBounds().getY1();
    }

    @Override
    public int getX2()
    {
        return getVisualBounds().getX2();
    }

    @Override
    public int getY2()
    {
        return getVisualBounds().getY2();
    }

    @Override
    public int getWidth()
    {
        return getVisualBounds().getWidth();
    }

    @Override
    public int getHeight()
    {
        return getVisualBounds().getHeight();
    }

    @Override
    public void move(int xofs, int yofs)
    {
        getBounds().move(xofs, yofs);
        getContentBounds().move(xofs, yofs);
        getVisualBounds().move(xofs, yofs);
        for (cz.vutbr.fit.layout.model.Box child : getChildren())
        {
            ((BoxNode) child).move(xofs, yofs);
        }
    }
    
    @Override
    public Color getColor()
    {
        return Units.toColor(getBox().getVisualContext().getColor());
    }

    @Override
    public String getFontFamily()
    {
        return getBox().getVisualContext().getFont().getName();
    }

    @Override
    public ContentObject getContentObject()
    {
        if (getBox().isReplaced())
        {
            ReplacedContent content = ((ReplacedBox) getBox()).getContentObj();
            if (content instanceof ReplacedImage)
                return new ContentImageImpl((ReplacedImage) content);
            else
                return null;
        }
        else
            return null;
    }

    @Override
    public Type getType()
    {
        if (getBox().isReplaced())
            return Type.REPLACED_CONTENT;
        else if (getBox() instanceof TextBox)
            return Type.TEXT_CONTENT;
        else
            return Type.ELEMENT;
    }

    public Node getDOMNode()
    {
        return getBox().getNode();
    }
    
    @Override
    public int getSourceNodeId()
    {
        return System.identityHashCode(getBox().getNode());
    }

    @Override
    public String getTagName()
    {
        final Node node = getDOMNode();
        if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
            return ((Element) node).getTagName().toLowerCase();
        else
            return null;
    }

    @Override
    public String getAttribute(String name)
    {
        final Node node = getDOMNode();
        if (node != null)
        {
            if ("href".equals(name))
                return getAncestorAttribute(node, "a", name);
            else
                return getElementAttribute(node, name);
        }
        else
            return null;
    }

    @Override
    public Map<String, String> getAttributes()
    {
        final Node node = getDOMNode();
        NamedNodeMap map = null;
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            map = node.getAttributes();
        }
        else if (node.getNodeType() == Node.TEXT_NODE) //text nodes -- try parent //TODO how to propagate from ancestors correctly?
        {
            final Node pnode = node.getParentNode();
            if (pnode != null && pnode.getNodeType() == Node.ELEMENT_NODE)
            {
                map = pnode.getAttributes();
            }
        }
        
        Map<String, String> ret = new HashMap<>((map == null) ? 1 : (map.getLength() + 1));
        if (map != null) //store the attributes found
        {
            for (int i = 0; i < map.getLength(); i++)
            {
                final Node attr = map.item(i);
                ret.put(attr.getNodeName(), attr.getNodeValue());
            }
        }
        //eventually add the href value (which may be inherited from top)
        if (!ret.containsKey("href"))
        {
            String href = getAncestorAttribute(node, "a", "href");
            if (href != null)
                ret.put("href", href);
        }
        return ret;
    }

    protected String getElementAttribute(Node node, String attrName)
    {
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
            final Element el = (Element) node;
            if (el.hasAttribute(attrName))
                return el.getAttribute(attrName);
            else
                return null;
        }
        else if (node.getNodeType() == Node.TEXT_NODE) //text nodes -- try parent //TODO how to propagate from ancestors correctly?
        {
            final Node pnode = node.getParentNode();
            if (pnode != null && pnode.getNodeType() == Node.ELEMENT_NODE)
            {
                final Element parent = (Element) pnode;
                if (parent.hasAttribute(attrName))
                    return parent.getAttribute(attrName);
                else
                    return null;
            }
            else
                return null;
        }
        else
            return null;
    }
    
    protected String getAncestorAttribute(Node node, String elementName, String attrName)
    {
        Node cur = node;
        //find the parent with the given name
        while (cur.getNodeType() != Node.ELEMENT_NODE || !elementName.equals(cur.getNodeName()))
        {
            cur = cur.getParentNode();
            if (cur == null)
                return null;
        }
        //read the attribute
        final Element el = (Element) cur;
        if (el.hasAttribute(attrName))
            return el.getAttribute(attrName);
        else
            return null;
    }
    
    @Override
    public DisplayType getDisplayType()
    {
        Box box = getBox();
        if (box instanceof ElementBox)
        {
            CSSProperty.Display display = ((ElementBox) box).getDisplay();
            if (display == null)
                return DisplayType.BLOCK; //e.g. the viewport has no display value
            switch (display)
            {
                case BLOCK:
                    return DisplayType.BLOCK;
                case INLINE:
                    return DisplayType.INLINE;
                case INLINE_BLOCK:
                    return DisplayType.INLINE_BLOCK;
                case INLINE_TABLE:
                    return DisplayType.INLINE_TABLE;
                case LIST_ITEM:
                    return DisplayType.LIST_ITEM;
                case NONE:
                    return DisplayType.NONE;
                case RUN_IN:
                    return DisplayType.RUN_IN;
                case TABLE:
                    return DisplayType.TABLE;
                case TABLE_CAPTION:
                    return DisplayType.TABLE_CAPTION;
                case TABLE_CELL:
                    return DisplayType.TABLE_CELL;
                case TABLE_COLUMN:
                    return DisplayType.TABLE_COLUMN;
                case TABLE_COLUMN_GROUP:
                    return DisplayType.TABLE_COLUMN_GROUP;
                case TABLE_FOOTER_GROUP:
                    return DisplayType.TABLE_FOOTER_GROUP;
                case TABLE_HEADER_GROUP:
                    return DisplayType.TABLE_HEADER_GROUP;
                case TABLE_ROW:
                    return DisplayType.TABLE_ROW;
                case TABLE_ROW_GROUP:
                    return DisplayType.TABLE_ROW_GROUP;
                default:
                    return DisplayType.BLOCK; //this should not happen
            }
        }
        else
            return null;
    }

    @Override
    public Rectangular getSubstringBounds(int startPos, int endPos)
    {
        Box box = getBox();
        if (box instanceof TextBox)
        {
            Rectangular ret = new Rectangular(getVisualBounds());
            int origin = ret.getX1();
            int startOfs = ((TextBox) box).getCharOffsetX(startPos);
            int endOfs = ((TextBox) box).getCharOffsetX(endPos);
            ret.setX1(origin + startOfs);
            ret.setX2(origin + endOfs);
            return ret;
        }
        else
            return null;
    }

    private int zoom(int src)
    {
        return Math.round(src * zoom);
    }
    
}
