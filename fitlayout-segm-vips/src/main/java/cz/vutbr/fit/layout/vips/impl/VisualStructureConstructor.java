/**
 * VIPS - Visual Internet Page Segmentation for FitLayout
 * 
 * Tomas Popela, 2012
 * Radek Burget, 2020 
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
    
    private List<VisualBlock> visualBlocks;
	private VisualArea root;
	private List<Separator> separators;
	private Rectangular pageBounds;
	

	public VisualStructureConstructor(Rectangular pageBounds, List<VisualBlock> blocks, 
	        List<Separator> separators)
	{
	    this.pageBounds = new Rectangular(pageBounds);
	    this.visualBlocks = blocks;
		this.separators = separators;
	}

    /**
     * Sets the page bounds.
     * @param bounds the new page bounds
     */
    public void setPageSize(Rectangular bounds)
    {
        this.pageBounds = new Rectangular(bounds);
    }

    /**
     * @return Returns final visual structure
     */
    public VisualArea getVisualStructure()
    {
        return root;
    }

    /**
     * Sets VipsBlock structure and also finds and saves all visual blocks from its
     * @param vipsBlocks VipsBlock structure
     */
    public void setVipsBlocks(List<VisualBlock> vipsBlocks)
    {
        visualBlocks = vipsBlocks;
    }

    /**
     * Returns all visual blocks in page
     * @return Visual Blocks
     */
    public List<VisualBlock> getVisualBlocks()
    {
        return visualBlocks;
    }
    
	/**
	 * Constructs the new visual structure.
	 */
	public void constructVisualStructure()
	{
	    root = new VisualArea();
	    root.setBounds(pageBounds);
	    //collect all leaf structures
	    List<VisualArea> pool = extractLeafStructures();
	    root.addChildren(pool);
	    //a set of parent structures (just above the leaves)
	    Set<VisualArea> parents = new HashSet<>();
	    parents.add(root);
	    //reconstruct the visual structure tree based on the separator weights
	    List<Separator> seps = new LinkedList<>(separators);
	    normalizeSeparators(seps);
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
	private Set<VisualArea> splitParents(Set<VisualArea> parents, List<Separator> seps)
	{
	    Set<VisualArea> newParents = new HashSet<>();
	    //distribute separators to parents
	    for (VisualArea parent : parents)
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
    	        List<VisualArea> subParents = splitParent(parent, plist);
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
	private List<VisualArea> splitParent(VisualArea parent, List<Separator> seps)
	{
	    //create n+1 new parents
	    List<VisualArea> newParents = new ArrayList<>(seps.size() + 1);
	    Separator prevSep = null;
	    for (int i = 0; i < seps.size() + 1; i++)
	    {
	        final VisualArea newParent = new VisualArea(parent);
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
	    for (VisualArea child : parent.getChildren())
	    {
	        final int pos = findSeparatorIndexAfter(child, seps);
	        newParents.get(pos).addChild(child);
	    }
	    //make the new parents the children of the current parent
        parent.getChildren().clear(); //children have been moved to sub-parents
        List<VisualArea> subParents = new ArrayList<>(newParents.size());
        for (VisualArea subParent : newParents)
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
	private int findSeparatorIndexAfter(VisualArea area, List<Separator> seps)
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
	private List<VisualArea> extractLeafStructures()
	{
	    List<VisualArea> list = new ArrayList<>();
	    
        VisualArea initial = new VisualArea();
        initial.setBlockRoots(visualBlocks);
        initial.setBounds(pageBounds);
        list.add(initial);

        for (Separator sep : separators)
        {
            List<VisualArea> toAdd = new ArrayList<>();
            List<VisualArea> toRemove = new ArrayList<>();
            for (VisualArea area : list)
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

	private void splitHorizontally(VisualArea current, Separator separator, List<VisualArea> list)
	{
        VisualArea top = new VisualArea(current);
        top.setY2(separator.startPoint - 1);

        VisualArea bottom = new VisualArea(current);
        bottom.setY1(separator.endPoint + 1);
        
        List<VisualBlock> nestedBlocks = current.getBlockRoots();
        for (VisualBlock vipsBlock : nestedBlocks)
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
	
	private void splitVertically(VisualArea current, Separator separator, List<VisualArea> list)
	{
        VisualArea left = new VisualArea(current);
        left.setX2(separator.startPoint - 1);

        VisualArea right = new VisualArea(current);
        right.setX1(separator.endPoint + 1);
	    
        List<VisualBlock> nestedBlocks = current.getBlockRoots();
        for (VisualBlock vipsBlock : nestedBlocks)
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
	
	private void sortChildren(List<VisualArea> children, boolean vertical)
	{
	    Comparator<VisualArea> comp;
	    if (vertical)
	    {
	        comp = new Comparator<VisualArea>()
            {
                @Override
                public int compare(VisualArea o1, VisualArea o2)
                {
                    return o1.getX1() - o2.getX1();
                }
            };
	    }
	    else
	    {
            comp = new Comparator<VisualArea>()
            {
                @Override
                public int compare(VisualArea o1, VisualArea o2)
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
     * Computes the normalized weights of the separators in the interval (1..11)
     * @param separators a sorted list of separators to normalize.
     */
	public void normalizeSeparators(List<Separator> separators)
    {
	    if (!separators.isEmpty())
	    {
            final double maxWeight = separators.get(0).weight;
            final double minWeight = separators.get(separators.size() - 1).weight;
    
            for (Separator separator : separators)
            {
                double normalizedValue = (separator.weight - minWeight) / (maxWeight - minWeight) * (11 - 1) + 1;
                separator.setNormalizedWeight((int) Math.ceil(normalizedValue));
            }
	    }
    }

	/**
	 * Finds minimal DoC in given structure
	 * @param visualStructure
	 */
	private int findMinimalDoC(VisualArea visualStructure)
	{
		int min = Integer.MAX_VALUE;
		for (VisualArea child : visualStructure.getChildren())
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
