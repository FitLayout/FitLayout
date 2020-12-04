package cz.vutbr.fit.layout.bcs.impl;

import java.util.HashSet;
import java.util.List;

public class PatternElement
{
    private int alignment;
    private PageArea wrapper;
    private final HashSet<PatternElement> subpatterns;

    public static final int ALIGNMENT_NONE = 0;
    public static final int ALIGNMENT_VERTICAL = 1;
    public static final int ALIGNMENT_HORIZONTAL = 1;

    public PatternElement()
    {
        this.alignment = Pattern.ALIGNMENT_NONE;
        this.subpatterns = new HashSet<>();
        this.wrapper = null;
    }

    public int getAlignment()
    {
        return alignment;
    }

    public void setAlignment(int alignment)
    {
        this.alignment = alignment;
    }

    public List<PageArea> getAreas()
    {
        return this.wrapper.getChildren();
    }

    public void addArea(PageArea area)
    {
        if (this.wrapper == null)
        {
            this.wrapper = new PageArea(area);
        }

        this.wrapper.addChild(area);
    }

    public void delArea(PageArea a)
    {
        this.wrapper.getChildren().remove(a);
    }

    public HashSet<PatternElement> getSubpatterns()
    {
        return subpatterns;
    }

    public void addSubpattern(PatternElement subpattern)
    {
        this.subpatterns.add(subpattern);
    }

    public boolean contains(PageArea a)
    {
        return this.wrapper.contains(a);
    }

    public static int transformAlignment(int patternAlignment)
    {
        if (patternAlignment == Pattern.ALIGNMENT_LEFT ||
            patternAlignment == Pattern.ALIGNMENT_RIGHT)
        {
            return ALIGNMENT_VERTICAL;
        }
        else if (patternAlignment == Pattern.ALIGNMENT_TOP ||
                patternAlignment == Pattern.ALIGNMENT_BOTTOM)
        {
            return ALIGNMENT_HORIZONTAL;
        }
        else
        {
            return ALIGNMENT_NONE;
        }
    }
}
