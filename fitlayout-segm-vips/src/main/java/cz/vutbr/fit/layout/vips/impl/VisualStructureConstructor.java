/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructureConstructor.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Constructs the final visual structure of page.
 * 
 * @author Tomas Popela
 * @author burgetr
 */
public class VisualStructureConstructor 
{
    private static Logger log = LoggerFactory.getLogger(VisualStructureConstructor.class);
    
    private List<VipsBlock> visualBlocks;
	private VisualStructure root;
	private List<Separator> separators;
	private Rectangular pageBounds;
	

	public VisualStructureConstructor(Rectangular pageBounds, List<VipsBlock> blocks, 
	        List<Separator> separators)
	{
	    this.pageBounds = new Rectangular(pageBounds);
	    this.visualBlocks = blocks;
		this.separators = separators;
	}

	/**
	 * Constructs the new visual structure.
	 */
	public void constructVisualStructure()
	{
	    root = new VisualStructure();
	    root.setBounds(pageBounds);
	    //collect all leaf structures
	    List<VisualStructure> pool = extractLeafStructures();
	    root.addChildren(pool);
	    //a set of parent structures (just above the leaves)
	    Set<VisualStructure> parents = new HashSet<>();
	    parents.add(root);
	    //reconstruct the visual structure tree based on the separator weights
	    List<Separator> seps = new LinkedList<>(separators);
	    while (!seps.isEmpty())
	    {
            //collect separators of the same weight and direction
            final int w = seps.get(0).weight;
            final boolean vertical = seps.get(0).vertical;
            List<Separator> equalSeps = new ArrayList<>();
	        while (!seps.isEmpty() && seps.get(0).vertical == vertical && seps.get(0).weight == w)
	        {
	            final Separator sep = seps.remove(0);
	            equalSeps.add(sep);
	        }
            sortSeparatorsByPosition(equalSeps);
	        parents = splitParents(parents, equalSeps);
	    }
	}
	
	/**
	 * Applies the given set of separators to given parents.
	 * @param parents the parents to split by the separators
	 * @param seps the separators
	 * @return a set of newly created parents
	 */
	private Set<VisualStructure> splitParents(Set<VisualStructure> parents, List<Separator> seps)
	{
	    Set<VisualStructure> newParents = new HashSet<>();
	    //distribute separators to parents
	    for (VisualStructure parent : parents)
	    {
	        //find separators for parent
	        List<Separator> plist = new ArrayList<>();
	        for (Separator sep : seps)
	        {
	            if (sep.isInside(parent))
	                plist.add(sep);
	        }
	        //find new parents
	        if (!plist.isEmpty()) //the parent has been split by a separator
	        {
    	        List<VisualStructure> subParents = splitParent(parent, plist);
    	        newParents.addAll(subParents);
	        }
	        else //not split, it remains among the parents
	        {
	            newParents.add(parent);
	        }
	    }
	    return newParents;
	}
	
	/**
	 * Splits a parent to sub-parents by the given separators.
	 * @param parent the parent area to split
	 * @param seps the list of separators
	 * @return a list of newly created sub-parents
	 */
	private List<VisualStructure> splitParent(VisualStructure parent, List<Separator> seps)
	{
	    //create n+1 new parents
	    List<VisualStructure> newParents = new ArrayList<>(seps.size() + 1);
	    Separator prevSep = null;
	    for (int i = 0; i < seps.size() + 1; i++)
	    {
	        final VisualStructure newParent = new VisualStructure(parent);
	        Separator nextSep = (i < seps.size()) ? seps.get(i) : null;
	        if (prevSep != null)
	        {
	            if (prevSep.vertical)
	                newParent.setX1(prevSep.endPoint + 1);
	            else
	                newParent.setY1(prevSep.endPoint + 1);
	        }
	        if (nextSep != null)
	        {
                if (nextSep.vertical)
                    newParent.setX2(nextSep.startPoint - 1);
                else
                    newParent.setY2(nextSep.startPoint - 1);
	        }
	        newParents.add(newParent);
	        prevSep = nextSep;
	    }
	    //distribute the children among the new parents
	    for (VisualStructure child : parent.getChildren())
	    {
	        final int pos = findSeparatorIndexAfter(child, seps);
	        newParents.get(pos).addChild(child);
	    }
	    //make the new parents the children of the current parent
        parent.getChildren().clear(); //children have been moved to sub-parents
        List<VisualStructure> subParents = new ArrayList<>(newParents.size());
        for (VisualStructure subParent : newParents)
        {
            if (subParent.getChildren().size() > 1)
                subParents.add(subParent);
            else if (subParent.getChildren().size() == 1)
                subParents.add(subParent.getChildren().get(0)); //only one child, no need for parent
        }
        parent.addChildren(subParents);
        parent.setSeparators(seps);
        return subParents;
	}
	
	/**
	 * Finds the index of the first separator after the given visual area in the separator list.
	 * The list must be ordered by positions.
	 * @param area the visual area
	 * @param seps the list of separators
	 * @return the index of the first separator after or the list size when there is no separator after
	 */
	private int findSeparatorIndexAfter(VisualStructure area, List<Separator> seps)
	{
	    for (int i = 0; i < seps.size(); i++)
	    {
	        final Separator sep = seps.get(i);
	        if ((sep.vertical && sep.startPoint > area.getX2())
	                || (!sep.vertical && sep.startPoint > area.getY2()))
	        {
	            return i;
	        }
	    }
	    return seps.size();
	}
	
	
	/**
	 * Extracts the smallest visual areas based on the separators.
	 * @return a list of extracted visual areas 
	 */
	private List<VisualStructure> extractLeafStructures()
	{
	    List<VisualStructure> list = new ArrayList<>();
	    
        VisualStructure initial = new VisualStructure();
        initial.setBlockRoots(visualBlocks);
        initial.setBounds(pageBounds);
        list.add(initial);

        for (Separator sep : separators)
        {
            List<VisualStructure> toAdd = new ArrayList<>();
            List<VisualStructure> toRemove = new ArrayList<>();
            for (VisualStructure area : list)
            {
                if (!sep.isVertical() &&
                        (sep.startPoint >= area.getY1() && sep.endPoint <= area.getY2()))
                {
                    splitHorizontally(area, sep, toAdd);
                    toRemove.add(area);
                }
                else if (sep.isVertical() &&
                        (sep.startPoint >= area.getX1() && sep.endPoint <= area.getX2()))
                {
                    splitVertically(area, sep, toAdd);
                    toRemove.add(area);
                }
            }
            list.removeAll(toRemove);
            list.addAll(toAdd);
        }
	    
	    return list;
	}

	private void splitHorizontally(VisualStructure current, Separator separator, List<VisualStructure> list)
	{
        VisualStructure top = new VisualStructure(current);
        top.setY2(separator.startPoint - 1);
        top.setBottom(separator);

        VisualStructure bottom = new VisualStructure(current);
        bottom.setY1(separator.endPoint + 1);
        bottom.setTop(separator);
        
        List<VipsBlock> nestedBlocks = current.getBlockRoots();
        for (VipsBlock vipsBlock : nestedBlocks)
        {
            if (vipsBlock.getBounds().getY1() <= separator.startPoint)
                top.addBlock(vipsBlock);
            else
                bottom.addBlock(vipsBlock);
        }
        
        if (!top.isEmpty())
            list.add(top);
        if (!bottom.isEmpty())
            list.add(bottom);
	}
	
	private void splitVertically(VisualStructure current, Separator separator, List<VisualStructure> list)
	{
        VisualStructure left = new VisualStructure(current);
        left.setX2(separator.startPoint - 1);
        left.setRight(separator);

        VisualStructure right = new VisualStructure(current);
        right.setX1(separator.endPoint + 1);
        right.setLeft(separator);
	    
        List<VipsBlock> nestedBlocks = current.getBlockRoots();
        for (VipsBlock vipsBlock : nestedBlocks)
        {
            if (vipsBlock.getBounds().getX1() <= separator.startPoint)
                left.addBlock(vipsBlock);
            else
                right.addBlock(vipsBlock);
        }
        
        if (!left.isEmpty())
            list.add(left);
        if (!right.isEmpty())
            list.add(right);
	}
	
	private void sortChildren(List<VisualStructure> children, boolean vertical)
	{
	    Comparator<VisualStructure> comp;
	    if (vertical)
	    {
	        comp = new Comparator<VisualStructure>()
            {
                @Override
                public int compare(VisualStructure o1, VisualStructure o2)
                {
                    return o1.getX1() - o2.getX1();
                }
            };
	    }
	    else
	    {
            comp = new Comparator<VisualStructure>()
            {
                @Override
                public int compare(VisualStructure o1, VisualStructure o2)
                {
                    return o1.getY1() - o2.getY1();
                }
            };
	    }
	    Collections.sort(children, comp);
	}
	
	private void sortSeparatorsByPosition(List<Separator> separators)
	{
	    Collections.sort(separators, new Comparator<Separator>()
        {
            @Override
            public int compare(Separator o1, Separator o2)
            {
                return o1.startPoint - o2.startPoint;
            }
        });
	}
	
	/**
	 * Sets page's size
	 * @param width Page's width
	 * @param height Page's height
	 */
	public void setPageSize(Rectangular bounds)
	{
	    this.pageBounds = new Rectangular(bounds);
	}

	/**
	 * @return Returns final visual structure
	 */
	public VisualStructure getVisualStructure()
	{
		return root;
	}

	/**
	 * Sets VipsBlock structure and also finds and saves all visual blocks from its
	 * @param vipsBlocks VipsBlock structure
	 */
	public void setVipsBlocks(List<VipsBlock> vipsBlocks)
	{
		visualBlocks = vipsBlocks;
	}

	/**
	 * Returns all visual blocks in page
	 * @return Visual Blocks
	 */
	public List<VipsBlock> getVisualBlocks()
	{
		return visualBlocks;
	}

	/**
	 * Finds minimal DoC in given structure
	 * @param visualStructure
	 */
	private int findMinimalDoC(VisualStructure visualStructure)
	{
		int min = Integer.MAX_VALUE;
		for (VisualStructure child : visualStructure.getChildren())
		{
			if (child.getDoC() < min)
			    min = child.getDoC();
		}
		return min;
	}

	/**
	 * Returns minimal DoC on page
	 * @return Minimal DoC
	 */
	public int getMinimalDoC()
	{
		return findMinimalDoC(root);
	}

}
