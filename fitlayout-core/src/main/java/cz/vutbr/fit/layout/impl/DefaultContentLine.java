/**
 * DefaultContentLine.java
 *
 * Created on 8. 11. 2018, 13:53:21 by burgetr
 */
package cz.vutbr.fit.layout.impl;

import java.util.ArrayList;
import java.util.Collection;

import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.ContentLine;
import cz.vutbr.fit.layout.model.ContentRect;

/**
 * A default ContentLine implementation using a simple ArrayList. This is suitable
 * for shorter lines since the area lookup is always sequentional.
 * @author burgetr
 */
public class DefaultContentLine extends ArrayList<ContentRect> implements ContentLine 
{
    private static final long serialVersionUID = 1L;

    public DefaultContentLine()
    {
        super();
    }
    
    public DefaultContentLine(int size)
    {
        super(size);
    }
    
    public DefaultContentLine(Collection<? extends Area> src)
    {
        super(src);
        for (Area a : src)
            a.setLine(this);
    }
    
    @Override
    public ContentRect getAreaBefore(ContentRect area)
    {
        int i = indexOf(area);
        return (i > 0) ? get(i - 1) : null;
    }

    @Override
    public ContentRect getAreaAfter(ContentRect area)
    {
        int i = indexOf(area);
        return (i != -1 && i + 1 < size()) ? get(i + 1) : null;
    }

    @Override
    public boolean add(ContentRect area)
    {
        area.setLine(this);
        return super.add(area);
    }

    @Override
    public void add(int index, ContentRect area)
    {
        area.setLine(this);
        super.add(index, area);
    }

    @Override
    public boolean addAll(Collection<? extends ContentRect> c)
    {
        for (ContentRect a : c)
            a.setLine(this);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends ContentRect> c)
    {
        for (ContentRect a : c)
            a.setLine(this);
        return super.addAll(index, c);
    }
    
}
