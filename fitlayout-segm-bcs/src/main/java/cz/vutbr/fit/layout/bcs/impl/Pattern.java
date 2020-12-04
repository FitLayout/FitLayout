package cz.vutbr.fit.layout.bcs.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class Pattern
{
    /* Different levels of pattern consistency
     * - menu type (one line/column); high pattern consistency:
     * -- all elements aligned to the same edge
     * -- all have the same color
     * -- spaces between them consistent (?)
     *
     * - article type; medium pattern consistency:
     * -- elements aligned to columns / lines; these columns/lines then aligned to each other (probably in another direction)
     * -- edge coordinates don't overlap (obj1.left isn't between obj2.left and obj2.right -> they are in a column ; similar for lines)
     * -- color coherence makes the consistency even stronger
     *
     * - table type; low pattern consistency:
     * -- elements aligned to lines/columns, these may or may not be aligned to each other
     * -- edge coordinates don't overlap (obj1.left isn't between obj2.left and obj2.right -> they are in a column ; similar for lines)
     */

    // TODO: if an inspected box supports the Pattern within group
    // - it should be automatically accepted for highly consistent patterns
    // - for medium consistent patterns, it should be added to the similarity formula, with high weight (it influences the mean higher than others)
    // - for low consistent patterns, it should be added with no special weight

    // TODO: alignment in lines and columns
    /* Alignment both in lines and columns will be given by mutual position
     * in terms of coordinate overlap
     *
     */
    private final PageArea area;

    /**
     * This variable designates the level of consistency of the pattern
     *
     * 0 = high
     * 1 = medium
     * 2 = low
     * 3 = none
     */
    private int consistency;

    public static final int CONSISTENCY_HIGH = 0;
    public static final int CONSISTENCY_MEDIUM = 1;
    public static final int CONSISTENCY_LOW = 2;
    public static final int CONSISTENCY_NONE = 3;

    public static final int ALIGNMENT_NONE = 0;
    public static final int ALIGNMENT_TOP = 1;
    public static final int ALIGNMENT_RIGHT = 2;
    public static final int ALIGNMENT_BOTTOM = 3;
    public static final int ALIGNMENT_LEFT = 4;

    /**
     * These two are temporary results given by findAlignment()
     */
    private HashSet<PageArea> aligned;
    private HashSet<PageArea> unaligned;

    public Pattern(PageArea a)
    {
        this.area = a;
    }

    public int calculateConsistency()
    {
        int consistency = CONSISTENCY_HIGH;
        int alignment = ALIGNMENT_NONE;
        int newAlignment;

        PageArea firstChild = null;
        List<PageArea> children = this.area.getChildren();

        if (children == null) return CONSISTENCY_NONE;

        /* We start by testing for high consistency */
        for (PageArea child: children)
        {
            if (firstChild == null)
            {
                firstChild = child;
                continue;
            }

            newAlignment = this.getAlignment(firstChild, child);
            // TODO: add color check
            if (alignment == ALIGNMENT_NONE)
            {
                alignment = newAlignment;
            }
            if (newAlignment == ALIGNMENT_NONE || alignment != newAlignment)
            {
                consistency = CONSISTENCY_MEDIUM;
                break;
            }
        }

        if (consistency == CONSISTENCY_HIGH)
        {
            return CONSISTENCY_HIGH;
        }

        /* Ok, not high, try medium consistency */
        PatternElement el;

        alignment = this.findAlignment(children);
        el = new PatternElement();
        el.setAlignment(PatternElement.transformAlignment(alignment));
        for (PageArea a: this.aligned)
        {
            el.addArea(a);
        }

        this.filterContained(el);




        return CONSISTENCY_NONE;
    }

    private int getAlignment(PageArea a, PageArea b)
    {
        // TODO: partial alignment (two areas are not aligned per se but their border lines are overlapping in terms of "expanding the line or column")
        //       - probably in different function, as we want to apply it only after the first stage of processing is finished
        if (a.getTop() == b.getTop()) return ALIGNMENT_TOP;
        else if (a.getRight() == b.getRight()) return ALIGNMENT_RIGHT;
        else if (a.getBottom() == b.getBottom()) return ALIGNMENT_BOTTOM;
        else if (a.getLeft() == b.getLeft()) return ALIGNMENT_LEFT;
        else return ALIGNMENT_NONE;
    }

    private int findAlignment(List<PageArea> areas)
    {
        /* These variables store the best result*/
        int alignment;
        int tmpAlignment;
        int bestAlignment = ALIGNMENT_NONE;
        HashSet<PageArea> bestAlignedSet = new HashSet<>();
        HashSet<PageArea> alignedSet = new HashSet<>();
        HashSet<PageArea> unAlignedSet = new HashSet<>();

        PageArea area;
        PageArea tmpArea;
        int i = 0;
        int j, k;

        for (PageArea a: areas)
        {
            area = null;
            alignment = ALIGNMENT_NONE;
            for (j = i+1; j < areas.size(); j++)
            {
                area = areas.get(j);
                /* First find some two elements that are aligned in some way */
                alignment = this.getAlignment(a, area);
                if (alignment == ALIGNMENT_NONE)
                {
                    continue;
                }

                alignedSet.clear();
                alignedSet.add(a);
                alignedSet.add(area);
                /* Now walk through the list and try to find all other elements that are aligned to one of them in the same way */
                for (k = j+1; k < areas.size(); k++)
                {
                    tmpArea = areas.get(k);
                    if (tmpArea == a || tmpArea == area) continue;

                    tmpAlignment = this.getAlignment(a, tmpArea);
                    if (tmpAlignment == alignment)
                    {
                        alignedSet.add(tmpArea);
                    }
                }

                /* Don't forget to store the area-area-alignment triplet with the best result so it can be then returned */
                if (alignedSet.size() > bestAlignedSet.size())
                {
                    bestAlignment = alignment;
                    bestAlignedSet = alignedSet;
                    alignedSet = new HashSet<>();
                }
            }

            i++;
        }

        if (bestAlignment != ALIGNMENT_NONE)
        {
            for (PageArea a: areas)
            {
                if (!bestAlignedSet.contains(a))
                {
                    unAlignedSet.add(a);
                }
            }
        }

        this.aligned = bestAlignedSet;
        this.unaligned = unAlignedSet;
        return bestAlignment;
    }

    public void filterContained(PatternElement el)
    {
        int alignment;
        int patAlignment;

        PatternElement subEl;
        ArrayList<PageArea> deleteList = new ArrayList<>();
        HashSet<PageArea> nextScan = new HashSet<>();

        for (PageArea a: this.unaligned)
        {
            /* deleteList can already contain this area in case it was
             * previously aligned with some other group */
            if (!el.contains(a) || deleteList.contains(a)) continue;

            // TODO: what if the area is not fully contained but overlaps?
            // - it basically means this pattern is not a real pattern
            // -- if adding the new area can create a subpattern with any of areas within the pattern -> ok
            // -- if no subpattern can be achieved -> failed pattern, we have to find another one
            // --- probably find the area which is causing the pattern to overlap with "a" and try to remove it
            //     (wouldn't it be better to construct completely new pattern? We'd probably need some list of candidates)

            subEl = null;
            for (PageArea elArea: el.getAreas())
            {
                alignment = this.getAlignment(a, elArea);
                if (alignment != ALIGNMENT_NONE)
                {
                    patAlignment = PatternElement.transformAlignment(alignment);

                    /* If the el is column, we are looking for rows and vice versa */
                    if (patAlignment != el.getAlignment())
                    {
                        /* this won't cause any issues, we are breaking from
                         * the loop below */
                        el.delArea(elArea);

                        subEl = new PatternElement();
                        subEl.addArea(elArea);
                        subEl.addArea(a);
                        deleteList.add(a);
                        el.addSubpattern(subEl);

                        /* Now find all other areas in unaligned list
                         * that might be aligned with these two */
                        for (PageArea tmpArea: this.unaligned)
                        {
                            if (!el.contains(tmpArea) || deleteList.contains(tmpArea))
                            {
                                alignment = this.getAlignment(tmpArea, a);
                                if (PatternElement.transformAlignment(alignment) == patAlignment)
                                {
                                    subEl.addArea(tmpArea);
                                    deleteList.add(tmpArea);
                                }
                            }
                        }

                        break;
                    }
                }
            }
            if (subEl == null)
            {
                /* The area "a" is contained but doesn't align with any boxes within,
                 * perhaps it will align with some sub pattern.
                 */
                nextScan.add(a);
            }
        }

        for (PageArea a: deleteList)
        {
            this.unaligned.remove(a);
        }

        if (nextScan.size() > 0)
        {
            // TODO: now this means that consistency of the pattern is either low or none
        }
    }


    /**
     * This function creates a testing pattern which would be a result
     * if a new child was added
     *
     * @param newChild the child object which is to be added and the new pattern computed
     * @return returns a new Pattern object which can be then compared to this one
     * to determine if the change is acceptable
     */
    public Pattern getUpdatedPattern(PageArea newChild)
    {
        List<PageArea> children = this.area.getChildren();

        if (children == null || children.size() == 0) return null;

        return null;
    }

    public int getConsistency()
    {
        return consistency;
    }
}
