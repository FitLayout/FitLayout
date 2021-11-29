/**
 * PageBoxTreeBuilder.java
 *
 * Created on 3. 12. 2020, 11:38:02 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.LinkedList;
import java.util.List;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Color;
import cz.vutbr.fit.layout.model.ContentRect;
import cz.vutbr.fit.layout.model.Page;

/**
 * A simple box tree builder that takes another page as its input, re-builds the box tree
 * according to the parametres and returns a new page.
 * 
 * @author burgetr
 */
public class PageBoxTreeBuilder extends BaseBoxTreeBuilder
{
    private DefaultPage page;
    

    public PageBoxTreeBuilder(boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
    }

    /**
     * Creates the new page from an input page.
     * @param input the input page
     * @param label the label to be set for the new page
     * @param creator the creator ID to be set for the new page
     * @param creatorParams the creator parametres to be set for the new page
     * @return the new page
     */
    public Page processPage(Page input, String label, String creator, String creatorParams)
    {
        page = createTree(input);
        page.setLabel(label);
        page.setCreator(creator);
        page.setCreatorParams(creatorParams);
        return page;
    }
    
    @Override
    public Page getPage()
    {
        return page;
    }

    //===================================================================================
    
    private DefaultPage createTree(Page input)
    {
        DefaultPage page = new DefaultPage(input);
        page.setParentIri(input.getIri());
        //create a copy of the tree and the box list
        List<Box> boxes = new LinkedList<>();
        createBoxTree(page.getRoot(), null, boxes);
        //use the current parent as the intrinsic one
        for (Box box : boxes)
        {
            ((DefaultBox) box).setIntrinsicParent(box.getParent());
        }
        //create the new tree
        Box root = buildTree(boxes, Color.WHITE);
        page.setRoot(root);
        return page;
    }

    /**
     * Copies a box tree to a new tree. Moreover, the created boxes are stored in a target list.
     * @param root the subtree root
     * @param treeRoot the whole tree root. {@code null} means that the newly created box should be used as the new root.
     * @param target the taget box list
     */
    private Box createBoxTree(Box root, Box treeRoot, List<Box> target)
    {
        final DefaultBox ret = new DefaultBox(root);
        ret.setId(ret.getOrder()); //use the order as ID for the visual tree boxes
        ret.addUserAttribute(ContentRect.ATTR_SAME_AS, root);
        target.add(ret);
        // hadle the root node of the whole tree
        if (treeRoot == null)
        {
            ret.setRoot(ret);
            treeRoot = ret;
        }
        // continue recursively
        for (Box child : root.getChildren())
            ret.appendChild(createBoxTree(child, treeRoot, target));
        return ret;
    }
}
