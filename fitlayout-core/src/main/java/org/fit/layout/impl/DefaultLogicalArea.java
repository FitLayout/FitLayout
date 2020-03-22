/**
 * DefaultLogicalArea.java
 *
 * Created on 19. 3. 2015, 13:10:48 by burgetr
 */
package org.fit.layout.impl;

import java.util.List;
import java.util.Vector;

import org.fit.layout.model.Area;
import org.fit.layout.model.LogicalArea;
import org.fit.layout.model.Tag;

/**
 * Default LogicalArea implementation.
 * 
 * @author burgetr
 */
public class DefaultLogicalArea extends DefaultTreeNode<LogicalArea> implements LogicalArea
{
    private List<Area> areas;
    private String text;
    private Tag mainTag;
    
    public DefaultLogicalArea()
    {
        super(LogicalArea.class);
        areas = new Vector<Area>();
        text = "";
    }
    
    public DefaultLogicalArea(Area src)
    {
        super(LogicalArea.class);
        areas = new Vector<Area>();
        areas.add(src);
        text = src.getText();
    }
    
    public DefaultLogicalArea(Area src, String text)
    {
        super(LogicalArea.class);
        areas = new Vector<Area>();
        areas.add(src);
        this.text = text;
    }
    
    //==============================================================================
    
    @Override
    public void addArea(Area a)
    {
        areas.add(a);
    }

    @Override
    public List<Area> getAreas()
    {
        return areas;
    }

    @Override
    public Area getFirstArea()
    {
        return ((Vector<Area>) areas).firstElement();
    }
    
    @Override
    public int getAreaCount()
    {
        return areas.size();
    }

    @Override
    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public String getText()
    {
        return text;
    }
    
    @Override
    public void setMainTag(Tag mainTag)
    {
        this.mainTag = mainTag;
    }

    @Override
    public Tag getMainTag()
    {
        return mainTag;
    }

    @Override
    public String toString()
    {
        final String tagstr = (mainTag == null) ? "---" : mainTag.getValue();
        return "(" + tagstr + ") " + getText();
    }
    
    //==============================================================================

    @Override
    public LogicalArea findArea(Area area)
    {
        LogicalArea ret = null;
        //scan the subtree
        for (int i = 0; i < getChildCount() && ret == null; i++)
        {
            ret = getChildAt(i).findArea(area);
        }
        //not in the subtree -- is it this area?
        if (ret == null && getAreas().contains(area))
            ret = this; //in our area nodes
        return ret;
    }
    
}
