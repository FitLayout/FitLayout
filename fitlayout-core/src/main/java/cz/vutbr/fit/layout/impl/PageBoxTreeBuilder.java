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
import cz.vutbr.fit.layout.model.Page;

/**
 * A simple box tree builder that takes another page as its input, re-builds the box tree
 * according to the parametres and returns a new page.
 * 
 * @author burgetr
 */
public class PageBoxTreeBuilder extends BaseBoxTreeBuilder
{
    private Page page;
    

    public PageBoxTreeBuilder(boolean useVisualBounds, boolean preserveAux)
    {
        super(useVisualBounds, preserveAux);
    }

    public Page processPage(Page input)
    {
        page = createTree(input);
        return page;
    }
    
    @Override
    public Page getPage()
    {
        return page;
    }

    //===================================================================================
    
    private Page createTree(Page input)
    {
        DefaultPage page = new DefaultPage(input);
        //create the box list
        List<Box> boxes = new LinkedList<>();
        createBoxList(page.getRoot(), boxes);
        //create the new tree
        Box root = buildTree(boxes, Color.WHITE);
        page.setRoot(root);
        return page;
    }

    private void createBoxList(Box root, List<Box> target)
    {
        target.add(root);
        for (Box child : root.getChildren())
            createBoxList(child, target);
    }
}
