/**
 * VIPS - Visual Internet Page Segmentation for FitLayout
 * 
 * Tomas Popela, 2012
 * Radek Burget, 2020 
 */

package cz.vutbr.fit.layout.vips.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * The VIPS algorithm implementation.
 * 
 * @author Tomas Popela
 * @author burgetr
 */
public class Vips 
{
    /** Maximal number of iterations when the process does not seem to end in a normal way */
    private static final int MAX_ITERATIONS = 20;
    
    private static Logger log = LoggerFactory.getLogger(Vips.class);
    
	private Page page = null;
	private VisualArea rootArea;

	private boolean _graphicsOutput = false;
	private boolean _outputToFolder = false;
	private int pDoC = 11;
	private	int sizeTresholdWidth = 350;
	private	int sizeTresholdHeight = 400;
	
	/** Overall minimal separator weight for the whole segmentation */
	private int minSepWeight;
    /** Overall maximal separator weight for the whole segmentation */
	private int maxSepWeight;
	

	/**
	 * Default constructor
	 */
	public Vips()
	{
	}

	/**
	 * Enables or disables graphics output of VIPS algorithm.
	 * @param enable True for enable, otherwise false.
	 */
	public void enableGraphicsOutput(boolean enable)
	{
		_graphicsOutput = enable;
	}

	/**
	 * Enables or disables creation of new directory for every algorithm run.
	 * @param enable True for enable, otherwise false.
	 */
	public void enableOutputToFolder(boolean enable)
	{
		_outputToFolder = enable;
	}

	/**
	 * Sets permitted degree of coherence (pDoC) value.
	 * 
	 * @param value pDoC value (1 .. 11)
	 */
	public void setPredefinedDoC(int value)
	{
		if (value <= 0 || value > 11)
		{
			log.error("pDoC value must be between 1 and 11! ({} given)", value);
		}
		pDoC = value;
	}

	/**
	 * Sets the page to process
	 * @param page the new page
	 */
	public void setPage(Page page)
	{
	    this.page = page;
	}

	public VisualArea getVisualStructure()
	{
	    return rootArea; 
	}
	
	public VipsTreeBuilder getTreeBuilder()
	{
	    return new VipsTreeBuilder(pDoC);
	}
	
	/**
	 * Generates folder filename
	 * @return Folder filename
	 */
	private String generateFolderName()
	{
		String outputFolder = "";

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
		outputFolder += sdf.format(cal.getTime());
		outputFolder += "_";
		outputFolder += page.getSourceURL().getHost().replaceAll("\\.", "_").replaceAll("/", "_");

		return outputFolder;
	}

    /**
     * Performs the VIPS segmentation itself.
     */
	private void performSegmentation()
    {
        final int pageWidth = page.getWidth();
        final int pageHeight = page.getHeight();
        final Rectangular pageBounds = new Rectangular(0, 0, pageWidth - 1, pageHeight - 1);
        
        minSepWeight = Integer.MAX_VALUE;
        maxSepWeight = 1;
        
        //create the root visual area
        VisualBlock rootBlock = new VisualBlock();
        rootBlock.setBox(page.getRoot());
        rootArea = new VisualArea();
        rootArea.setBounds(pageBounds);
        rootArea.addBlock(rootBlock);
        
        //perform the iterations until there are no changes or we reach the maximal number of iterations
        int iteration = 0;
        boolean change = true;
        while (change && iteration < MAX_ITERATIONS)
        {
            change = iteration(iteration, rootArea);
            recursiveComputeDoC(rootArea); //recompute the DoC for all the areas
            iteration++;
        }
        log.debug("Segmentation finished after {} iterations", iteration);
        
    }

    /**
     * Runs a single iteration on the visual structure tree. Chooses the leaf nodes
     * of the tree and tries to perform segmentation on them.
     * @param index the iteration index (for reporting only)
     * @param root the root of the visual area tree
     * @return {@code true} when some leaf areas have been segmented, {@code false} when no
     * changes have been made to the tree.
     */
    private boolean iteration(int index, VisualArea root)
    {
        List<VisualArea> leaves = new ArrayList<>();
        getLeafAreas(root, leaves);
        int li = 1;
        boolean changed = false;
        for (VisualArea leaf : leaves)
        {
            if (!leaf.isFinished())
                changed |= segmentArea(index, li++, leaf);
        }
        return changed;
    }
    
    /**
     * Applies the VIPS algorihm on a visual area that represents a (sub-)page. Detects a tree
     * of descendant areas in the given area.
     * @param iterationIndex current iteration index (for reporting only)
     * @param leafIndex leaf area index in this iteration (for reporting only)
     * @param area the area to perform segmentation on. The detected sub-areas will be
     * added to this area as its child areas.
     * @return {@code true} when some sub-areas have been detected, {@code false} when no changes
     * have been made to the area structure (no structure detected)
     */
    private boolean segmentArea(int iterationIndex, int leafIndex, VisualArea area)
    {
        //extract the blocks
        VisualBlockDetector vipsParser = new VisualBlockDetector(area);
        vipsParser.setSizeTresholdHeight(sizeTresholdHeight);
        vipsParser.setSizeTresholdWidth(sizeTresholdWidth);
        vipsParser.parse();
        List<VisualBlock> vipsBlocks = vipsParser.getVisualBlocks();
        
        //find separators
        SeparatorDetector detector = new SeparatorDetector(vipsBlocks, area.getBounds());
        List<Separator> hsep = detector.detectHorizontalSeparators();
        List<Separator> vsep = detector.detectVerticalSeparators();
        List<Separator> asep = detector.getAllSeparators();
        updateSeparatorStats(asep);
        if (_graphicsOutput)
        {
            String suffix = "-" + iterationIndex + "-" + leafIndex;
            exportSeparators(suffix, rootArea.getBounds(), vipsBlocks, hsep, vsep);
        }
        
        if (!asep.isEmpty()) //some separators detected
        {
            // visual structure construction
            VisualStructureConstructor constructor = new VisualStructureConstructor(area.getBounds(), vipsBlocks, asep);
            constructor.constructVisualStructure();
            VisualArea resultRoot = constructor.getVisualStructure();
            // connect the discovered structure to the processed area
            area.addChildren(resultRoot.getChildren());
            area.setSeparators(resultRoot.getSeparators());
            return true;
        }
        else
        {
            area.setFinished(true); //do not try to segment this in future iterations
            return false; //no separators detected, nothing has been changed
        }
    }   
    
    private void getLeafAreas(VisualArea root, List<VisualArea> dest)
    {
        if (root.getChildren().isEmpty())
        {
            dest.add(root);
        }
        else
        {
            for (VisualArea child : root.getChildren())
                getLeafAreas(child, dest);
        }
    }
    

	/**
	 * Starts segmentation of a page.
	 * 
	 * @param page The page to perform segmentation on.
	 */
	public void startSegmentation(Page page)
	{
		setPage(page);
		startSegmentation();
	}

	/**
	 * Starts visual segmentation of the current page.
	 */
	public void startSegmentation()
	{
		try
		{
			String outputFolder = "";
			String oldWorkingDirectory = "";
			String newWorkingDirectory = "";

			if (_outputToFolder)
			{
				outputFolder = generateFolderName();

				if (!new File(outputFolder).mkdir())
				{
					System.err.println("Something goes wrong during directory creation!");
				}
				else
				{
					oldWorkingDirectory = System.getProperty("user.dir");
					newWorkingDirectory += oldWorkingDirectory + "/" + outputFolder + "/";
					System.setProperty("user.dir", newWorkingDirectory);
				}
			}

			performSegmentation();

			if (_outputToFolder)
				System.setProperty("user.dir", oldWorkingDirectory);
		}
		catch (Exception e)
		{
			System.err.println("Something's wrong!");
			e.printStackTrace();
		}
	}

	/**
	 * Updates the values of maximal/minimal separator weights according to sorted list of separators.
	 * @param separators a list of separators sorted by weights from the highest to the lowest weight
	 */
	private void updateSeparatorStats(List<Separator> separators)
	{
	    if (!separators.isEmpty())
	    {
            final int maxWeight = separators.get(0).weight;
            final int minWeight = separators.get(separators.size() - 1).weight;
            
            if (minWeight < minSepWeight)
                minSepWeight = minWeight;
            if (maxWeight > maxSepWeight)
                maxSepWeight = maxWeight;
	    }
	}
	
	private void recursiveComputeDoC(VisualArea root)
	{
	    for (VisualArea child : root.getChildren())
	        recursiveComputeDoC(child);
	    root.setDoC(computeDoC(root));
	}
	
	private int computeDoC(VisualArea area)
	{
	    int maxsep = area.getMaxSeparator();
	    if (maxsep == 0)
	    {
	        return 11; //no separators - maximal DoC
	    }
	    else
	    {
	        //some separators found, return 1..10 based on the relative separator weight
    	    final double minWeight = minSepWeight;
    	    final double maxWeight = maxSepWeight;
            double normalizedValue = (maxsep - minWeight) / (maxWeight - minWeight) * (10 - 1) + 1;
            return 11 - (int) Math.ceil(normalizedValue);
	    }
	}
	
    /**
     * Exports all separators to output images
     */
    private void exportSeparators(String suffix, Rectangular bounds,
            List<VisualBlock> blocks, List<Separator> hsep, List<Separator> vsep)
    {
        GraphicalOutput out = new GraphicalOutput(bounds);
        out.setHorizontalSeparators(hsep);
        out.exportHorizontalSeparatorsToImage(suffix);

        out.setVerticalSeparators(vsep);
        out.exportVerticalSeparatorsToImage(suffix);

        out.setVisualBlocks(blocks);
        out.exportAllToImage(suffix);
    }

	
}
