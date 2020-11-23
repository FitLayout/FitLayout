/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorGraphicsDetector.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import cz.vutbr.fit.layout.model.Box;
import cz.vutbr.fit.layout.model.Rectangular;

/**
 * Separator detector with possibility of generating graphics output.
 * @author Tomas Popela
 * @author burgetr
 */
public class VipsSeparatorGraphicsDetector extends VipsSeparatorDetector 
{
    private static final Color VSEP_COLOR = Color.RED;
    private static final Color HSEP_COLOR = Color.BLUE;
    private static final Color BLOCK_COLOR = Color.BLACK;
    private static final Color TEXT_COLOR = Color.WHITE;
    
    private Graphics2D displayPool;
    private BufferedImage image;


    public VipsSeparatorGraphicsDetector(int width, int height)
    {
        super(width, height);
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        createDisplayPool();
    }
    
    protected void createDisplayPool()
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
            Rectangle rect;
            int tx, ty;
            if (!separator.isEmpty()) //position is known
            {
                rect = new Rectangle(new Point(separator.getX1(), separator.getY1()), 
                        new Dimension(separator.getWidth(), separator.getHeight()));
                tx = separator.getX1();
                ty = separator.getY1();
            }
            else //only start/end point is known
            {
                rect = new Rectangle(0, separator.getStartPoint(), image.getWidth(), separator.getHeight());
                tx = separator.getStartPoint();
                ty = 0;
            }

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
            Rectangle rect;
            int tx, ty;
            if (!separator.isEmpty()) //position is known
            {
                rect = new Rectangle(new Point(separator.getX1(), separator.getY1()), 
                        new Dimension(separator.getWidth(), separator.getHeight()));
                tx = separator.getX1();
                ty = separator.getY1();
            }
            else //only start/end point is known
            {
                rect = new Rectangle(separator.getStartPoint(), 0, separator.getWidth(), image.getHeight());
                tx = separator.getStartPoint();
                ty = 0;
            }

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
