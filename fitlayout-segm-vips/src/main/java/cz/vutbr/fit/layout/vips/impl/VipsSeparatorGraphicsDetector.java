/*
 * Tomas Popela, 2012
 * VIPS - Visual Internet Page Segmentation
 * Module - VipsSeparatorGraphicsDetector.java
 */

package cz.vutbr.fit.layout.vips.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
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
    Graphics2D _pool = null;
    BufferedImage _image = null;


    public VipsSeparatorGraphicsDetector(int width, int height)
    {
        super(width, height);
        this._image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
    }
    
    /**
     * Adds visual block to pool.
     * 
     * @param vipsBlock
     *            Visual block
     */
    @Override
    public void addVisualBlock(VipsBlock vipsBlock)
    {
        Box elementBox = vipsBlock.getBox();

        Rectangular bb = elementBox.getContentBounds();
        Rectangle rect = new Rectangle(bb.getX1(), bb.getY1(), bb.getWidth(), bb.getHeight());

        _pool.draw(rect);
        _pool.fill(rect);
    }
    
    @Override
    protected void createPool()
    {
        // set black as pool background color
        _pool = _image.createGraphics();
        _pool.setColor(Color.white);
        _pool.fillRect(0, 0, _image.getWidth(), _image.getHeight());
        // set drawing color back to white
        _pool.setColor(Color.black);
    }

    /**
     * Saves everything (separators + block) to image.
     */
    public void exportAllToImage()
    {
        createPool();
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
        createPool();
        fillPoolWithBlocks(_visualBlocks);
        drawVerticalSeparators();
        drawHorizontalSeparators();
        saveToImage("iteration" + suffix);
    }
    
    /**
     * Adds all detected vertical separators to pool
     */
    private void drawVerticalSeparators()
    {
        _pool.setColor(Color.red);
        for (Separator separator : getVerticalSeparators())
        {
            Rectangle rect;
            if (separator.leftUp != null)
                rect = new Rectangle(separator.leftUp, new Dimension(
                        (int) (separator.rightDown.getX() - separator.leftUp.getX()),
                        (int) (separator.rightDown.getY() - separator.leftUp.getY())));
            else
                rect = new Rectangle(separator.startPoint, 0, separator.endPoint - separator.startPoint, _image.getHeight());

            _pool.draw(rect);
            _pool.fill(rect);
        }
    }

    /**
     * Saves vertical separators to image.
     */
    public void exportVerticalSeparatorsToImage()
    {
        createPool();
        drawVerticalSeparators();
        saveToImage("verticalSeparators");
    }

    /**
     * Saves vertical separators to image.
     */
    public void exportVerticalSeparatorsToImage(int suffix)
    {
        createPool();
        drawVerticalSeparators();
        saveToImage("verticalSeparators" + suffix);
    }

    /**
     * Adds all detected horizontal separators to pool
     */
    private void drawHorizontalSeparators()
    {
        _pool.setColor(Color.blue);
        for (Separator separator : getHorizontalSeparators())
        {
            Rectangle rect;
            if (separator.leftUp != null)
                rect = new Rectangle(separator.leftUp, new Dimension(
                        (int) (separator.rightDown.getX() - separator.leftUp.getX()),
                        (int) (separator.rightDown.getY() - separator.leftUp.getY())));
            else
                rect = new Rectangle(0, separator.startPoint, _image.getWidth(), separator.endPoint - separator.startPoint);

            _pool.draw(rect);
            _pool.fill(rect);
        }
    }

    /**
     * Saves horizontal separators to image.
     */
    public void exportHorizontalSeparatorsToImage()
    {
        createPool();
        drawHorizontalSeparators();
        saveToImage("horizontalSeparators");
    }

    /**
     * Saves horizontal separators to image.
     */
    public void exportHorizontalSeparatorsToImage(int suffix)
    {
        createPool();
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
            ImageIO.write(_image, "png", new File(filename));
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
            ImageIO.write(_image, "png", new File(filename));
        } catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    
}
