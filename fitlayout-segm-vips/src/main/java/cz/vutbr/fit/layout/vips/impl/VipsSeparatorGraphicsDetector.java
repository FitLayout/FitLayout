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
 *
 */
public class VipsSeparatorGraphicsDetector extends VipsSeparatorDetector 
{
    private Graphics2D displayPool = null;
    private BufferedImage image = null;


    public VipsSeparatorGraphicsDetector(int width, int height)
    {
        super(width, height);
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        createDisplayPool();
    }
    
    /**
     * Adds visual block to pool.
     * 
     * @param vipsBlock
     *            Visual block
     */
    @Override
    public void showVisualBlock(VipsBlock vipsBlock)
    {
        Box elementBox = vipsBlock.getBox();

        Rectangular bb = elementBox.getContentBounds();
        Rectangle rect = new Rectangle(bb.getX1(), bb.getY1(), bb.getWidth(), bb.getHeight());

        displayPool.draw(rect);
        displayPool.fill(rect);
    }
    
    @Override
    protected void createDisplayPool()
    {
        displayPool = image.createGraphics();
        displayPool.setColor(Color.white);
        displayPool.fillRect(0, 0, image.getWidth(), image.getHeight());
        displayPool.setColor(Color.black);
        displayPool.setFont(new Font("Dialog", Font.BOLD, 11));
    }

    public void fillPool()
    {
        for (VipsBlock block : getVisualBlocks())
        {
            showVisualBlock(block);
        }
    }
    
    /**
     * Saves everything (separators + block) to image.
     */
    public void exportAllToImage()
    {
        createDisplayPool();
        fillPool();
        drawVerticalSeparators();
        drawHorizontalSeparators();
        saveToImage("all");
    }

    /**
     * Saves everything (separators + block) to image with given suffix.
     */
    public void exportAllToImage(int suffix)
    {
        createDisplayPool();
        fillPool();
        drawVerticalSeparators();
        drawHorizontalSeparators();
        saveToImage("iteration" + suffix);
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

            displayPool.setColor(Color.red);
            displayPool.draw(rect);
            displayPool.fill(rect);
            displayPool.setColor(Color.white);
            displayPool.drawString(String.valueOf(separator.getWeight()), tx + 5, ty + 25);
        }
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

            displayPool.setColor(Color.blue);
            displayPool.draw(rect);
            displayPool.fill(rect);
            displayPool.setColor(Color.white);
            displayPool.drawString(String.valueOf(separator.getWeight()), tx + 5, ty + 15);
        }
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
    public void saveToImage(String filename)
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

    /**
     * Saves pool to image
     */
    public void saveToImage(String filename, String folder)
    {
        if (folder.equals(""))
            return;

        filename = folder + "/" + filename + ".png";

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
