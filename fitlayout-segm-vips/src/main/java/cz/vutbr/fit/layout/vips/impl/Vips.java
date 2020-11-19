/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - Vips.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
	private boolean _outputEscaping = true;
	private int _pDoC = 11;
	private String _filename = "";
	private	int sizeTresholdWidth = 350;
	private	int sizeTresholdHeight = 400;

	long startTime = 0;
	long endTime = 0;

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
	 * Enables or disables output XML character escaping.
	 * @param enable True for enable, otherwise false.
	 */
	public void enableOutputEscaping(boolean enable)
	{
		_outputEscaping = enable;
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
			_pDoC = value;
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
	    return new VipsTreeBuilder(_pDoC);
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

	/**
	 * Performs page segmentation.
	 */
	private void performSegmentation()
	{

		startTime = System.nanoTime();
		int numberOfIterations = 10;
		int pageWidth = page.getWidth();
		int pageHeight = page.getHeight();

		if (_graphicsOutput)
			exportPageToImage();

		VipsSeparatorGraphicsDetector detector;
		VipsParser vipsParser = new VipsParser(page);
		VisualStructureConstructor constructor = new VisualStructureConstructor(_pDoC);
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
		
		VipsOutput vipsOutput = new VipsOutput(_pDoC);
		vipsOutput.setEscapeOutput(_outputEscaping);
		vipsOutput.setOutputFileName(_filename);
		vipsOutput.writeXML(visualStructure, page);

		endTime = System.nanoTime();

		long diff = endTime - startTime;

		System.out.println("Execution time of VIPS: " + diff + " ns; " +
				(diff / 1000000.0) + " ms; " +
				(diff / 1000000000.0) + " sec");
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

	public void setOutputFileName(String filename)
	{
		if (!filename.equals(""))
		{
			_filename = filename;
		}
		else
		{
			System.out.println("Invalid filename!");
		}
	}
}
