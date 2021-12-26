/**
 * Relation.java
 *
 * Created on 15. 2. 2018, 15:08:37 by burgetr
 */
package cz.vutbr.fit.layout.model;

/**
 * A basic relation interface.
 * 
 * @author burgetr
 */
public interface Relation
{
    /**
     * Gets the name of the relation.
     * @return the relation name
     */
    public String getName();

    /**
     * Is this relation symmetric? i.e. xRy => yRx
     * @return true when the relation is symmetric
     */
    public boolean isSymmetric();
    
    /**
     * The inverse relation to the given relation (if applicable).
     * @return the inverse relation or {@code null}
     */
    public Relation getInverse();
}
