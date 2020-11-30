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
    private static Logger log = LoggerFactory.getLogger(Vips.class);
    
	private Page page = null;
	private VisualArea rootArea;

	private boolean _graphicsOutput = false;
	private boolean _outputToFolder = false;
	private int pDoC = 11;
	private	int sizeTresholdWidth = 350;
	private	int sizeTresholdHeight = 400;

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
	    return new VipsTreeBuilder();
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

    private void performSegmentation()
    {
        final int pageWidth = page.getWidth();
        final int pageHeight = page.getHeight();
        final Rectangular pageBounds = new Rectangular(0, 0, pageWidth - 1, pageHeight - 1);
        
        //create the root visual area
        VisualBlock rootBlock = new VisualBlock();
        rootBlock.setBox(page.getRoot());
        rootArea = new VisualArea();
        rootArea.setBounds(pageBounds);
        rootArea.addBlock(rootBlock);
        
        iteration(1, rootArea);
        iteration(2, rootArea);
        iteration(3, rootArea);
        
        System.out.println("done");
        
    }

    private void iteration(int index, VisualArea root)
    {
        List<VisualArea> leaves = new ArrayList<>();
        getLeafAreas(root, leaves);
        int li = 1;
        for (VisualArea leaf : leaves)
        {
            segmentArea(index, li++, leaf);
        }
    }
    
    private void segmentArea(int iterationIndex, int leafIndex, VisualArea area)
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
        if (_graphicsOutput)
        {
            String suffix = "-" + iterationIndex + "-" + leafIndex;
            exportSeparators(suffix, rootArea.getBounds(), vipsBlocks, hsep, vsep);
        }
        
        // visual structure construction
        VisualStructureConstructor constructor = new VisualStructureConstructor(area.getBounds(), vipsBlocks, asep);
        constructor.constructVisualStructure();
        VisualArea resultRoot = constructor.getVisualStructure();
        // connect the discovered structure to the processed area
        area.addChildren(resultRoot.getChildren());
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
	 * Starts segmentation on given address
	 * @param url
	 */
	public void startSegmentation(Page page)
	{
		setPage(page);
		startSegmentation();
	}

	/**
	 * Starts visual segmentation of page
	 * @throws Exception
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
