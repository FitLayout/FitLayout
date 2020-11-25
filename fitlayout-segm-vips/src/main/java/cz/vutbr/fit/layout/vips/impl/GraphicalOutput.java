/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorGraphicsDetector.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * A graphical output that allows to draws the blocks and separators into an image.
 * 
 * @author Tomas Popela
 * @author burgetr
 */
public class GraphicalOutput 
{
    private static final Color VSEP_COLOR = Color.RED;
    private static final Color HSEP_COLOR = Color.BLUE;
    private static final Color BLOCK_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    
    private Graphics2D displayPool;
    private BufferedImage image;
    private Rectangular bounds;

    private List<VipsBlock> visualBlocks;
    private List<Separator> horizontalSeparators;
    private List<Separator> verticalSeparators;


    public GraphicalOutput(Rectangular subPageBounds)
    {
        this.bounds = new Rectangular(subPageBounds);
        this.image = new BufferedImage(bounds.getWidth(), bounds.getHeight(), BufferedImage.TYPE_INT_BGR);
        createDisplayPool();
    }
    
    public List<VipsBlock> getVisualBlocks()
    {
        return visualBlocks;
    }

    public void setVisualBlocks(List<VipsBlock> visualBlocks)
    {
        this.visualBlocks = visualBlocks;
    }

    public List<Separator> getHorizontalSeparators()
    {
        return horizontalSeparators;
    }

    public void setHorizontalSeparators(List<Separator> horizontalSeparators)
    {
        this.horizontalSeparators = horizontalSeparators;
    }

    public List<Separator> getVerticalSeparators()
    {
        return verticalSeparators;
    }

    public void setVerticalSeparators(List<Separator> verticalSeparators)
    {
        this.verticalSeparators = verticalSeparators;
    }

    private void createDisplayPool()
    {
        displayPool = image.createGraphics();
        displayPool.setColor(Color.WHITE);
        displayPool.fillRect(0, 0, image.getWidth(), image.getHeight());
        displayPool.setColor(Color.BLACK);
        displayPool.setFont(new Font("Dialog", Font.BOLD, 11));
    }

    /**
     * Shows a visual block in the resulting image.
     */
    private void drawVisualBlock(VipsBlock vipsBlock)
    {
        Box elementBox = vipsBlock.getBox();

        Rectangular bb = elementBox.getContentBounds();
        Rectangle rect = new Rectangle(bb.getX1(), bb.getY1(), bb.getWidth(), bb.getHeight());

        displayPool.setColor(BLOCK_COLOR);
        displayPool.draw(rect);
        displayPool.fill(rect);
        displayPool.setColor(TEXT_COLOR);
        displayPool.drawString(String.valueOf(vipsBlock.getDoC()), bb.getX1() + 5, bb.getY1() + 15);
    }
    
    private void drawVisualBlocks()
    {
        for (VipsBlock block : getVisualBlocks())
        {
            drawVisualBlock(block);
        }
    }
    
    /**
     * Adds all detected horizontal separators to pool
     */
    private void drawHorizontalSeparators()
    {
        for (Separator separator : getHorizontalSeparators())
        {
            int tx = bounds.getX1();
            int ty = bounds.getY1() + separator.getStartPoint();
            Rectangle rect = new Rectangle(tx, ty, bounds.getWidth(), separator.getEndPoint() - separator.getStartPoint());

            displayPool.setColor(HSEP_COLOR);
            displayPool.draw(rect);
            displayPool.fill(rect);
            displayPool.setColor(TEXT_COLOR);
            displayPool.drawString(String.valueOf(separator.getWeight()), tx + 5, ty + 15);
        }
    }

    /**
     * Adds all detected vertical separators to pool
     */
    private void drawVerticalSeparators()
    {
        for (Separator separator : getVerticalSeparators())
        {
            int tx = bounds.getX1() + separator.getStartPoint();
            int ty = bounds.getY1();
            Rectangle rect = new Rectangle(tx, ty, separator.getEndPoint() - separator.getStartPoint(), bounds.getHeight());

            displayPool.setColor(VSEP_COLOR);
            displayPool.draw(rect);
            displayPool.fill(rect);
            displayPool.setColor(TEXT_COLOR);
            displayPool.drawString(String.valueOf(separator.getWeight()), tx + 5, ty + 25);
        }
    }

    private void drawAll()
    {
        drawVisualBlocks();
        drawVerticalSeparators();
        drawHorizontalSeparators();
    }

    /**
     * Saves everything (separators + block) to image.
     */
    public void exportAllToImage()
    {
        createDisplayPool();
        drawAll();
        saveToImage("all");
    }

    /**
     * Saves everything (separators + block) to image with given suffix.
     */
    public void exportAllToImage(int suffix)
    {
        createDisplayPool();
        drawAll();
        saveToImage("iteration" + suffix);
    }
    
    /**
     * Saves vertical separators to image.
     */
    public void exportVerticalSeparatorsToImage()
    {
        createDisplayPool();
        drawVerticalSeparators();
        saveToImage("verticalSeparators");
    }

    /**
     * Saves vertical separators to image.
     */
    public void exportVerticalSeparatorsToImage(int suffix)
    {
        createDisplayPool();
        drawVerticalSeparators();
        saveToImage("verticalSeparators" + suffix);
    }

    /**
     * Saves horizontal separators to image.
     */
    public void exportHorizontalSeparatorsToImage()
    {
        createDisplayPool();
        drawHorizontalSeparators();
        saveToImage("horizontalSeparators");
    }

    /**
     * Saves horizontal separators to image.
     */
    public void exportHorizontalSeparatorsToImage(int suffix)
    {
        createDisplayPool();
        drawHorizontalSeparators();
        saveToImage("horizontalSeparators" + suffix);
    }

    /**
     * Saves pool to image
     */
    private void saveToImage(String filename)
    {
        filename = System.getProperty("user.dir") + "/" + filename + ".png";
        try
        {
            ImageIO.write(image, "png", new File(filename));
        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
