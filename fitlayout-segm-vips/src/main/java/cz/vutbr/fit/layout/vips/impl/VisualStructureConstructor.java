/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VisualStructureConstructor.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Class that constructs final visual structure of page.
 * @author Tomas Popela
 * @author burgetr
 */
public class VisualStructureConstructor 
{
	//private int iteration;
    private List<VipsBlock> visualBlocks;
	private VisualStructure rootStructure;
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
	    //collect all leaf structures
	    List<VisualStructure> pool = extractLeafStructures();
	    //reconstruct the visual structure tree based on the separator weights
	    List<Separator> seps = new LinkedList<>(separators);
	    boolean change = true;
	    while (change && !seps.isEmpty())
	    {
	        change = false;
            //collect pairs of areas separated by a separator with the same weight
            final int w = seps.get(0).weight;
	        List<SepPair> pairs = new ArrayList<>();
	        while (!seps.isEmpty() && seps.get(0).weight == w)
	        {
	            SepPair pair = findPairForSeparator(seps.remove(0), pool);
	            if (pair.isComplete())
	            {
	                pairs.add(pair);
	                change = true;
	            }
	            else
	                System.err.println("Incomplete pair?!");
	        }
	        //group by pairs
	        List<VisualStructure> groups = groupByPairs(pairs, pool);
	        pool.addAll(groups);
	    }
	    
	    if (pool.size() >= 1)
	        rootStructure = pool.get(0);
	    if (pool.size() != 1)
	        System.err.println(pool.size() + " areas left in pool");
	}
	
	/**
	 * Find the groups of connectable pairs and creates joint visual structures
	 * for the groups. The joint visual structures are removed from the pool.
	 * @param pairs the pairs to group
	 * @param pool the pool of available visual structures
	 * @return
	 */
	private List<VisualStructure> groupByPairs(List<SepPair> pairs, List<VisualStructure> pool)
	{
	    List<VisualStructure> ret = new ArrayList<>();
	    while (!pairs.isEmpty())
	    {
    	    //get the first pair and join all connected pairs
    	    SepPair seed = pairs.remove(0);
    	    Set<VisualStructure> children = new HashSet<>();
    	    VisualStructure newstr = new VisualStructure(seed);
    	    children.add(seed.a);
            children.add(seed.b);
    	    ret.add(newstr);
    	    boolean change = true;
    	    while (change && !pairs.isEmpty())
    	    {
    	        change = false;
    	        for (Iterator<SepPair> it = pairs.iterator(); it.hasNext(); )
    	        {
    	            final SepPair cand = it.next();
    	            if (newstr.joinPair(cand, children))
    	            {
    	                it.remove();
    	                children.add(cand.a);
    	                children.add(cand.b);
    	                change = true;
    	            }
    	        }
    	    }
    	    newstr.getChildren().addAll(children);
    	    pool.removeAll(children);
	    }
	    return ret;
	}
	
	/**
	 * Finds a pair of areas separated by the given separator in the list of areas.
	 * @param sep
	 * @param list
	 * @return
	 */
	private SepPair findPairForSeparator(Separator sep, List<VisualStructure> list)
	{
	    SepPair pair = new SepPair();
	    pair.separator = sep;
	    for (VisualStructure vs : list)
	    {
	        if (sep.isVertical())
	        {
	            if (sep.equals(vs.getRight()))
	                pair.a = vs;
	            else if (sep.equals(vs.getLeft()))
	                pair.b = vs;
	        }
	        else
	        {
                if (sep.equals(vs.getBottom()))
                    pair.a = vs;
                else if (sep.equals(vs.getTop()))
                    pair.b = vs;
	        }
	        if (pair.a != null && pair.b != null)
	            break;
	    }
	    return pair;
	}
	
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
        VisualStructure top = new VisualStructure();
        top.setX1(current.getX1());
        top.setY1(current.getY1());
        top.setX2(current.getX2());
        top.setY2(separator.startPoint - 1);
        top.setTop(current.getTop());
        top.setBottom(separator);

        VisualStructure bottom = new VisualStructure();
        bottom.setX1(current.getX1());
        bottom.setY1(separator.endPoint+1);
        bottom.setX2(current.getX2());
        bottom.setY2(current.getY2());
        bottom.setTop(separator);
        bottom.setBottom(current.getBottom());
        
        List<VipsBlock> nestedBlocks = current.getBlockRoots();
        for (VipsBlock vipsBlock : nestedBlocks)
        {
            if (vipsBlock.getBox().getContentBounds().getY1() <= separator.startPoint)
                top.addBlock(vipsBlock);
            else
                bottom.addBlock(vipsBlock);
        }
        
        list.add(top);
        list.add(bottom);
	}
	
	private void splitVertically(VisualStructure current, Separator separator, List<VisualStructure> list)
	{
        VisualStructure left = new VisualStructure();
        left.setX1(current.getX1());
        left.setY1(current.getY1());
        left.setX2(separator.startPoint - 1);
        left.setY2(current.getY2());
        left.setLeft(current.getLeft());
        left.setRight(separator);

        VisualStructure right = new VisualStructure();
        right.setX1(separator.endPoint + 1);
        right.setY1(current.getY1());
        right.setX2(current.getX2());
        right.setY2(current.getY2());
        right.setLeft(separator);
        right.setRight(current.getRight());
	    
        List<VipsBlock> nestedBlocks = current.getBlockRoots();
        for (VipsBlock vipsBlock : nestedBlocks)
        {
            if (vipsBlock.getBox().getContentBounds().getX1() <= separator.startPoint)
                left.addBlock(vipsBlock);
            else
                right.addBlock(vipsBlock);
        }
        
        list.add(left);
        list.add(right);
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
		return rootStructure;
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
		return findMinimalDoC(rootStructure);
	}

}
