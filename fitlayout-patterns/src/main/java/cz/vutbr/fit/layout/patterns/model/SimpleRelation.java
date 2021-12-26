/**
 * SimpleRelation.java
 *
 * Created on 25. 12. 2021, 21:18:15 by burgetr
 */
package cz.vutbr.fit.layout.patterns.model;

import cz.vutbr.fit.layout.model.Relation;

/**
 * 
 * @author burgetr
 */
public class SimpleRelation implements Relation
{
    private String name;
    private boolean symmetric;
    private Relation inverse;

    public SimpleRelation(String name)
    {
        this.name = name;
        symmetric = false;
        inverse = null;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean isSymmetric()
    {
        return symmetric;
    }

    @Override
    public Relation getInverse()
    {
        return inverse;
    }

    public Relation setSymmetric(boolean symmetric)
    {
        this.symmetric = symmetric;
        return this;
    }

    public Relation setInverse(Relation inverse)
    {
        this.inverse = inverse;
        return this;
    }
    
}
