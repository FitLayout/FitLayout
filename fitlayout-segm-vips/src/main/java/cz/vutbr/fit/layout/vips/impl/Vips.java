/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Vips.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import cz.vutbr.fit.layout.model.Page;

/**
 * Vision-based Page Segmentation algorithm
 * @author Tomas Popela
 *
 */
public class Vips {
	private Page page = null;
	private VisualStructure visualStructure;

	private boolean _graphicsOutput = false;
	private boolean _outputToFolder = false;
	private int pDoC = 11;
	private	int sizeTresholdWidth = 350;
	private	int sizeTresholdHeight = 400;

	private long startTime = 0;
	private long endTime = 0;

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
	 * @param value pDoC value.
	 */
	public void setPredefinedDoC(int value)
	{
		if (value <= 0 || value > 11)
		{
			System.err.println("pDoC value must be between 1 and 11! Not " + value + "!");
			return;
		}
		else
		{
			pDoC = value;
		}
	}

	/**
	 * Sets the page to process
	 * @param page the new page
	 */
	public void setPage(Page page)
	{
	    this.page = page;
	}

	public VisualStructure getVisualStructure()
	{
	    return visualStructure; 
	}
	
	public VipsTreeBuilder getTreeBuilder()
	{
	    return new VipsTreeBuilder(pDoC);
	}
	
	/**
	 * Exports rendered page to image.
	 */
	private void exportPageToImage()
	{
	    //TODO is this used?
		/*try
		{
			BufferedImage page = _browserCanvas.getImage();
			String filename = System.getProperty("user.dir") + "/page.png";
			ImageIO.write(page, "png", new File(filename));
		} catch (Exception e)
		{
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}*/
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
        
        //extract the blocks
        VipsParser vipsParser = new VipsParser(page, page.getRoot());
        vipsParser.setSizeTresholdHeight(sizeTresholdHeight);
        vipsParser.setSizeTresholdWidth(sizeTresholdWidth);
        vipsParser.parse();
        //VipsBlock vipsBlocks = vipsParser.getVipsBlocks();
        List<VipsBlock> vipsBlocks = vipsParser.getVisualBlocks();
        
        
        VisualStructureConstructor constructor = new VisualStructureConstructor(pDoC);
        constructor.setGraphicsOutput(_graphicsOutput);

        // visual structure construction
        constructor.setVipsBlocks(vipsBlocks);
        constructor.setPageSize(pageWidth, pageHeight);
        constructor.constructVisualStructure();
        constructor.normalizeSeparatorsMinMax();
        visualStructure = constructor.getVisualStructure();

        System.out.println("done");
        
    }	
	
	/**
	 * Performs page segmentation.
	 */
	/*private void xperformSegmentation()
	{

		startTime = System.nanoTime();
		int numberOfIterations = 1;
		int pageWidth = page.getWidth();
		int pageHeight = page.getHeight();

		if (_graphicsOutput)
			exportPageToImage();

		VipsSeparatorGraphicsDetector detector;
		VipsParser vipsParser = new VipsParser(page, page.getRoot());
		VisualStructureConstructor constructor = new VisualStructureConstructor(pDoC);
		constructor.setGraphicsOutput(_graphicsOutput);

		for (int iterationNumber = 1; iterationNumber < numberOfIterations+1; iterationNumber++)
		{
			detector = new VipsSeparatorGraphicsDetector(pageWidth, pageHeight);

			//visual blocks detection
			vipsParser.setSizeTresholdHeight(sizeTresholdHeight);
			vipsParser.setSizeTresholdWidth(sizeTresholdWidth);

			vipsParser.parse();

			VipsBlock vipsBlocks = vipsParser.getVipsBlocks();

			if (iterationNumber == 1)
			{
				if (_graphicsOutput)
				{
					// in first round we'll export global separators
					detector.setVipsBlock(vipsBlocks);
					detector.fillPool();
					detector.saveToImage("blocks" + iterationNumber);
					detector.setCleanUpSeparators(0);
					detector.detectHorizontalSeparators();
					detector.detectVerticalSeparators();
					detector.exportHorizontalSeparatorsToImage();
					detector.exportVerticalSeparatorsToImage();
					detector.exportAllToImage();
				}

				// visual structure construction
				constructor.setVipsBlocks(vipsBlocks);
				constructor.setPageSize(pageWidth, pageHeight);
			}
			else
			{
				vipsBlocks = vipsParser.getVipsBlocks();
				constructor.updateVipsBlocks(vipsBlocks);

				if (_graphicsOutput)
				{
					detector.setVisualBlocks(constructor.getVisualBlocks());
					detector.fillPool();
					detector.saveToImage("blocks" + iterationNumber);
				}
			}

			// visual structure construction
			constructor.constructVisualStructure();

			// prepare tresholds for next iteration
			if (iterationNumber <= 5 )
			{
				sizeTresholdHeight -= 50;
				sizeTresholdWidth -= 50;

			}
			if (iterationNumber == 6)
			{
				sizeTresholdHeight = 100;
				sizeTresholdWidth = 100;
			}
			if (iterationNumber == 7)
			{
				sizeTresholdHeight = 80;
				sizeTresholdWidth = 80;
			}
			if (iterationNumber == 8)
			{
				sizeTresholdHeight = 40;
				sizeTresholdWidth = 10;
			}
			if (iterationNumber == 9)
			{
				sizeTresholdHeight = 1;
				sizeTresholdWidth = 1;
			}

		}

		//		constructor.normalizeSeparatorsSoftMax();
		constructor.normalizeSeparatorsMinMax();

		visualStructure = constructor.getVisualStructure();
		
		endTime = System.nanoTime();

		long diff = endTime - startTime;

		System.out.println("Execution time of VIPS: " + diff + " ns; " +
				(diff / 1000000.0) + " ms; " +
				(diff / 1000000000.0) + " sec");
	}*/

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
			startTime = System.nanoTime();

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

}
