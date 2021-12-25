/**
 * Connection.java
 *
 * Created on 28. 2. 2016, 19:45:42 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

/**
 * 
 * @author burgetr
 */
public class Connection<T>
{
    private T a1;
    private T a2;
    private Relation relation;
    private float weight;
    
    public Connection(T a1, T a2, Relation relation, float weight)
    {
        this.a1 = a1;
        this.a2 = a2;
        this.relation = relation;
        this.weight = weight;
    }

    public T getA1()
    {
        return a1;
    }

    public T getA2()
    {
        return a2;
    }

    public Relation getRelation()
    {
        return relation;
    }

    public float getWeight()
    {
        return weight;
    }
    
    @Override
    public String toString()
    {
        return a1.toString() + "-" + relation.getName() + "-" + a2.toString() + "(" + weight + ")"; 
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a1 == null) ? 0 : a1.hashCode());
        result = prime * result + ((a2 == null) ? 0 : a2.hashCode());
        result = prime * result + ((relation == null) ? 0 : relation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Connection<?> other = (Connection<?>) obj;
        if (a1 == null)
        {
            if (other.a1 != null) return false;
        }
        else if (!a1.equals(other.a1)) return false;
        if (a2 == null)
        {
            if (other.a2 != null) return false;
        }
        else if (!a2.equals(other.a2)) return false;
        if (relation == null)
        {
            if (other.relation != null) return false;
        }
        else if (!relation.equals(other.relation)) return false;
        return true;
    }
    
    

}
