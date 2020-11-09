/**
 * CSSBoxTreeBuilder.java
 *
 * Created on 24. 10. 2014, 23:52:25 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Box.Type;

/**
 * This class implements building the box tree using the default FitLayout box nesting algorithms.
 * 
 * @author burgetr
 */
public abstract class BaseBoxTreeBuilder
{
    private static Logger log = LoggerFactory.getLogger(BaseBoxTreeBuilder.class);

    /** Which percentage of the box area must be inside of another box in order
     * to consider it as a child box (from 0 to 1) */
    private static final double AREAP = 0.8;
    
    protected URL pageUrl;
    protected String pageTitle;
    
    /** Use real visual bounds instead of the element content bounds for building the box hierarchy */
    protected boolean useVisualBounds;
    
    /** Preserve auxiliary boxes that have no actual visual result */
    protected boolean preserveAux;
    
   
    public BaseBoxTreeBuilder(boolean useVisualBounds, boolean preserveAux)
    {
        this.useVisualBounds = useVisualBounds;
        this.preserveAux = preserveAux;
    }
    
    /**
     * The resulting page model.
     * @return the page
     */
    public abstract Page getPage();
    
    //===================================================================
    
    protected Box buildTree(List<Box> boxlist, Color bgColor)
    {
        //the first box should be the root
        Box rootNode = boxlist.remove(0);
        System.out.println("preserve: " + preserveAux);
        
        //create the tree
        if (useVisualBounds)
        {
            //two-phase algorithm considering the visual bounds
            log.trace("A1");
            recomputeVisualBounds(boxlist);
            Box root = createBoxTree(rootNode, boxlist, true, true, true); //create a nesting tree based on the content bounds
            log.trace("A2");
            Color bg = rootNode.getBackgroundColor();
            if (bg == null) bg = Color.WHITE;
            computeBackgrounds(root, bg); //compute the efficient background colors
            log.trace("A2.5");
            recomputeVisualBounds(root); //compute the visual bounds for the whole tree
            log.trace("A3");
            root = createBoxTree(rootNode, boxlist, true, true, preserveAux); //create the nesting tree based on the visual bounds or content bounds depending on the settings
            recomputeVisualBounds(root); //compute the visual bounds for the whole tree
            recomputeBounds(root); //compute the real bounds of each node
            log.trace("A4");
            return root;
        }
        else
        {
            //simplified algorihm - use the original box nesting
            Box root = createBoxTree(rootNode, boxlist, false, true, true);
            Color bg = bgColor;
            if (bg == null) bg = Color.WHITE;
            computeBackgrounds(root, bg); //compute the efficient background colors
            recomputeVisualBounds(root); //compute the visual bounds for the whole tree
            recomputeBounds(root); //compute the real bounds of each node
            return root;
        }
    }
    
    /**
     * Creates a tree of box nesting based on the content bounds of the boxes.
     * This tree is only used for determining the backgrounds.
     * 
     * @param boxlist the list of boxes to build the tree from
     * @param useBounds when set to {@code true}, the full or visual bounds are used for constructing the tree
     * depending on the {@code useVisualBounds} parameter. Otherwise, the original box hierarchy is used.
     * @param useVisualBounds when set to {@code true} the visual bounds are used for constructing the tree. Otherwise,
     * the content bounds are used. 
     * @param preserveAux when set to {@code true}, all boxes are preserved. Otherwise, only the visually
     * distinguished ones are preserved.
     */
    private Box createBoxTree(Box root, List<Box> boxlist, boolean useBounds, boolean useVisualBounds, boolean preserveAux)
    {
        //a working copy of the box list
        List<Box> list = new ArrayList<Box>(boxlist);

        //detach the nodes from any old trees
        root.removeAllChildren();
        for (Box node : list)
        {
            node.removeAllChildren();
        }
        
        //when working with visual bounds, remove the boxes that are not visually separated
        if (!preserveAux)
        {
            for (Iterator<Box> it = list.iterator(); it.hasNext(); )
            {
                Box node = it.next();
                if (!node.isVisuallySeparated() || !node.isVisible())
                    it.remove();
            }
        }
        
        //the list of parents for every box
        List<Box> parents = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++)
            parents.add(null);
        //choose the child nodes for every potential parents
        for (Box parent : list)
        {
            if (useBounds)
                markNodesInside(parent, list, useVisualBounds, parents);
            else
                markChildNodes(parent, list, parents);
        }
        
        //choose the roots
        for (int i = 0; i < list.size(); i++)
        {
            if (parents.get(i) == null)
            {
                root.appendChild(list.get(i));
            }
        }
        
        //recursively choose the children
        for (Box child : root.getChildren())
            takeChildren(child, list, parents);
        
        return root;
    }

    /**
     * Takes a list of nodes and selects the nodes whose parent box is identical to this node's box. 
     * The {@code nearestParent} of the selected boxes is set to this box node.
     * @param list the list of nodes to test
     */
    public void markChildNodes(Box parent, List<Box> list, List<Box> parents)
    {
        for (int i = 0; i < list.size(); i++)
        {
            Box child = list.get(i);
            if (child != parent && child.getIntrinsicParent().equals(parent))
                parents.set(i, parent);
        }        
    }
    
    /**
     * Takes a list of nodes and selects the nodes that are located directly inside 
     * of this node's box. The corresponding positions in the destination list of parents
     * is set to the parent box.
     *  
     * @param parent the parent node to consider
     * @param list the list of nodes to test
     * @param useVisualBounds when set to {@code true}, only the boxes within the visual bounds are considered.
     *          Otherwise, all the nodes within the box content bounds are considered.
     * @param parents the list of parent boxes where the parent nodes are marked. The list must have the same size
     *          as {@code list}.
     */
    protected void markNodesInside(Box parent, List<Box> list, boolean useVisualBounds, List<Box> parents)
    {
        for (int i = 0; i < list.size(); i++)
        {
            final Box child = list.get(i);
            if (!useVisualBounds) //use the content bounds instead
            {
                if (child != parent 
                    && contentEncloses(parent, child)
                    && (parents.get(i) == null || !contentEncloses(parent, parents.get(i)))) 
                {
                    parents.set(i, parent);
                }
            }
            else
            {
                if (child != this 
                        && visuallyEncloses(parent, child)
                        && (parents.get(i) == null || !visuallyEncloses(parent, parents.get(i)))) 
                {
                    parents.set(i, parent);
                }
            }
        }
    }
    
    /**
     * Goes through the parent's children, takes all the nodes that are inside of this node
     * and makes them the children of this node. Then, recursively calls the children to take
     * their nodes.
     */
    protected void takeChildren(Box parent, List<Box> list, List<Box> parents)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (parent.equals(parents.get(i)))    
            {
                parent.appendChild(list.get(i));
            }
        }
        //let the children take their children
        for (Box child : parent.getChildren())
            takeChildren(child, list, parents);
    }
    
    protected boolean contentEncloses(Box parent, Box child)
    {
        if (parent.getContentBounds().encloses(child.getContentBounds()))
            return true; 
        else if (parent.getContentBounds().equals(child.getContentBounds()))
            return parent.getOrder() < child.getOrder();
        else
            return false;
    }
    
    protected boolean visuallyEncloses(Box parent, Box child)
    {
        if (child.getVisualBounds().encloses(parent.getVisualBounds()))
        {
            return false; //a reverse relationship (child contains parent)
        }
        else
        {
            final int shared = parent.getVisualBounds().intersection(child.getVisualBounds()).getArea();
            final double sharedperc = (double) shared / child.getBounds().getArea();
            return parent.getOrder() < child.getOrder() && sharedperc >= AREAP;
        }
    }
    
    
    //===================================================================
    
    /**
     * Computes efficient background color for all the nodes in the tree
     */
    protected void computeBackgrounds(Box root, Color currentbg)
    {
        Color newbg = root.getBackgroundColor();
        if (newbg == null)
            newbg = currentbg;
        root.setBackgroundSeparated(!newbg.equals(currentbg));
        
        for (int i = 0; i < root.getChildCount(); i++)
            computeBackgrounds(root.getChildAt(i), newbg);
    }
    
    //===================================================================
    
    /**
     * Recomputes the total bounds of the whole subtree. The bounds of each box will
     * correspond to its visual bounds. If the child boxes exceed the parent box,
     * the parent box bounds will be expanded accordingly.
     * @param root the root node of the subtree
     */
    public void recomputeBounds(Box root)
    {
        root.setBounds(new Rectangular(root.getVisualBounds()));
        for (Box child : root.getChildren())
        {
            recomputeBounds(child);
            root.getBounds().expandToEnclose(child.getBounds());
        }
    }
    
    /**
     * Recomputes the visual bounds for a list of boxes.
     * @param boxes
     */
    protected void recomputeVisualBounds(List<Box> boxes)
    {
        for (Box box : boxes)
        {
            box.setVisualBounds(computeVisualBounds(box));
        }
    }
    
    /**
     * Recomputes the visual bounds of the whole subtree.
     * @param root the root node of the subtree
     */
    protected void recomputeVisualBounds(Box root)
    {
        for (Box child : root.getChildren())
            recomputeVisualBounds(child);
        root.setVisualBounds(computeVisualBounds(root));
    }
    
    protected Rectangular computeVisualBounds(Box box)
    {
        Rectangular ret = null;
        
        if (box.getType() == Type.ELEMENT) //TODO viewport?
        {
            //one border only -- the box represents the border only
            if (box.getBorderCount() == 1 && !box.isBackgroundSeparated())
            {
                Rectangular b = box.getIntrinsicBounds();
                if (box.hasTopBorder())
                    ret = new Rectangular(b.getX1(), b.getY1(), b.getX2(), b.getY1() + box.getTopBorder() - 1);
                else if (box.hasBottomBorder())
                    ret = new Rectangular(b.getX1(), b.getY2() - box.getBottomBorder() + 1, b.getX2(), b.getY2());
                else if (box.hasLeftBorder())
                    ret = new Rectangular(b.getX1(), b.getY1(), b.getX1() + box.getLeftBorder() - 1, b.getY2());
                else if (box.hasRightBorder())
                    ret = new Rectangular(b.getX2() - box.getRightBorder() + 1, b.getY1(), b.getX2(), b.getY2());
            }
            //at least two borders or a border and background - take the border bounds
            else if (box.getBorderCount() >= 2 || (box.getBorderCount() == 1 && box.isBackgroundSeparated()))
            {
                ret = new Rectangular(box.getIntrinsicBounds()); //intrinsic bounds should correspond include the border(s)
            }
            //no borders and visually separated
            else if (box.isBackgroundSeparated())
            {
                ret = new Rectangular(box.getIntrinsicBounds()); //intrinsic bounds should correspond to background bounds
            }
            //no visual separators, consider the contents
            else
            {
                ret = getMinimalVisualBounds(box);
            }
        }
        else //not an element
            ret = getMinimalVisualBounds(box);
        
        return ret;
    }

    /**
     * Returns the minimal bounds of the box for enclosing all the contained boxes.
     * @return the minimal visual bounds
     */
    protected Rectangular getMinimalVisualBounds(Box box)
    {
        if (box.getType() == Type.TEXT_CONTENT || box.getType() == Type.REPLACED_CONTENT)
        {
            return box.getIntrinsicBounds();
        }
        else
        {
            Rectangular ret = null;
            for (Box sub : box.getChildren())
            {
                Rectangular sb = sub.getVisualBounds();
                if (sub.isVisible() && sb.getWidth() > 0 && sb.getHeight() > 0)
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
                Rectangular b = box.getIntrinsicBounds();
                return new Rectangular(b.getX1(), b.getY1());
            }
            else
                return ret;
        }
    }

    
}
