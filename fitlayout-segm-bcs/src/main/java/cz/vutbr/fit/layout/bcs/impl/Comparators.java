package cz.vutbr.fit.layout.bcs.impl;

import java.util.Comparator;

class AreaSizeComparator implements Comparator<PageArea> {
    @Override
    public int compare(PageArea a, PageArea b) {
        int sizeA, sizeB;
        int diff;

        sizeA = a.getWidth()*a.getHeight();
        sizeB = b.getWidth()*b.getHeight();
        diff = sizeA-sizeB;
        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }
}

class AreaTopComparator implements Comparator<PageArea> {
    @Override
    public int compare(PageArea a, PageArea b) {
        int diff = a.getTop()-b.getTop();

        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }
}

class RelationComparator implements Comparator<PageAreaRelation> {
    @Override
    public int compare(PageAreaRelation a, PageAreaRelation b) {
        int diff;

        diff = this.compareSimilarity(a, b);
        if (diff != 0) return diff;

        diff = this.compareSize(a, b);
        if (diff != 0) return diff;

        diff = this.compareATop(a, b);
        if (diff != 0) return diff;

        diff = this.compareBTop(a, b);
        if (diff != 0) return diff;

        diff = this.compareALeft(a, b);
        if (diff != 0) return diff;

        diff = this.compareBLeft(a, b);
        if (diff != 0) return diff;

        return 0;
    }

    protected int compareSimilarity(PageAreaRelation a, PageAreaRelation b)
    {
        double diff = a.getSimilarity() - b.getSimilarity();

        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }

    protected int compareSize(PageAreaRelation a, PageAreaRelation b)
    {
        int sizeA;
        int sizeB;
        sizeA = a.getA().getWidth()*a.getA().getHeight();
        sizeA += a.getB().getWidth()*a.getB().getHeight();
        sizeB = b.getA().getWidth()*b.getA().getHeight();
        sizeB += b.getB().getWidth()*b.getB().getHeight();

        int diff = sizeA-sizeB;

        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }

    protected int compareATop(PageAreaRelation a, PageAreaRelation b)
    {
        int diff = a.getA().getTop()-b.getA().getTop();
        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }

    protected int compareBTop(PageAreaRelation a, PageAreaRelation b)
    {
        int diff = a.getB().getTop()-b.getB().getTop();
        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }

    protected int compareALeft(PageAreaRelation a, PageAreaRelation b)
    {
        int diff = a.getA().getLeft()-b.getA().getLeft();
        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }

    protected int compareBLeft(PageAreaRelation a, PageAreaRelation b)
    {
        int diff = a.getB().getLeft()-b.getB().getLeft();
        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }
}

class AreaProximityComparator implements Comparator<PageAreaRelation> {
    @Override
    public int compare(PageAreaRelation a, PageAreaRelation b) {
        double diff = a.getAbsoluteDistance()-b.getAbsoluteDistance();

        if (diff > 0) return 1;
        else if (diff < 0) return -1;
        else return 0;
    }
}