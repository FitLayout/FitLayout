package cz.vutbr.fit.layout.bcs.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PageAreaRelation
{
    private PageArea a;
    private PageArea b;
    private double similarity;
    private int alignmentScore;
    private int absoluteDistance;

    private int direction;

    public static final int DIRECTION_VERTICAL = 0;
    public static final int DIRECTION_HORIZONTAL = 1;

    private int cardinality;

    public PageAreaRelation(PageArea a, PageArea b, double similarity, int direction)
    {
        this.a = a;
        this.b = b;
        this.similarity = similarity;
        this.setDirection(direction);
        this.setCardinality(1);
        this.setAlignmentScore(1);
    }

    public PageArea getA()
    {
        return a;
    }

    public void setA(PageArea a)
    {
        this.a = a;
    }

    public PageArea getB()
    {
        return b;
    }

    public void setB(PageArea b)
    {
        this.b = b;
    }

    public double getSimilarity()
    {
        return similarity;
    }

    public void setSimilarity(double similarity)
    {
        this.similarity = similarity;
    }

    public void addSimilarity(double similarity)
    {
        this.similarity += similarity;
    }

    public int getDirection()
    {
        return direction;
    }

    public void setDirection(int direction)
    {
        this.direction = direction;
    }

    public int getCardinality()
    {
        return cardinality;
    }

    public void setCardinality(int cardinality)
    {
        this.cardinality = cardinality;
    }

    public void addCardinality(int cardinality)
    {
        this.cardinality += cardinality;
    }

    public int getAbsoluteDistance()
    {
        return absoluteDistance;
    }

    public void setAbsoluteDistance(int absoluteDistance)
    {
        this.absoluteDistance = absoluteDistance;
    }

    @Override
    public String toString()
    {
        return "Relation: "+this.getAbsoluteDistance()+"-"+this.getSimilarity()+"-"+this.getAlignmentScore()+"-"+this.getCardinality()+"-"+this.a.toString()+"-"+this.b.toString();
    }

    public int getAlignmentScore()
    {
        return alignmentScore;
    }

    public void setAlignmentScore(int alignmentScore)
    {
        this.alignmentScore = alignmentScore;
    }

    public void addAlignmentScore(int alignmentScore)
    {
        this.alignmentScore += alignmentScore;
    }

    public int computeAlignmentScore()
    {
        int aligned = 1;
        int threshold;
        int alignment = PageArea.ALIGNMENT_NONE;

        ArrayList<PageArea> queue = new ArrayList<>();
        HashMap<PageArea, PageAreaRelation> neighbors;
        HashSet<PageArea> inspected = new HashSet<>();
        PageArea area, cur;
        PageAreaRelation relation;

        alignment = a.getSideAlignment(b);
        threshold = (int) Math.floor(Math.min(Math.min(this.a.getWidth(), this.a.getHeight()),
                                              Math.min(this.b.getWidth(), this.b.getHeight()))*1.5);
        if (alignment == PageArea.ALIGNMENT_NONE ||
            this.absoluteDistance > threshold)
        {
            return aligned;
        }

        aligned++;

        queue.add(this.a);
        queue.add(this.b);
        while (queue.size() > 0)
        {
            cur = queue.get(0);
            queue.remove(0);
            if (inspected.contains(cur) || queue.contains(cur)) continue;

            neighbors = cur.getNeighbors();
            for (Map.Entry<PageArea, PageAreaRelation> entry: neighbors.entrySet())
            {
                area = entry.getKey();
                relation = entry.getValue();

                if (relation.getDirection() != this.direction ||
                    inspected.contains(area) || queue.contains(area) ||
                    cur.getSideAlignment(area) != alignment ||
                    relation.getAbsoluteDistance() > threshold)
                {
                    continue;
                }

                aligned++;
                queue.add(area);
            }

            inspected.add(cur);
        }
        return aligned;
    }
}
