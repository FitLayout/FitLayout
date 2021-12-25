/**
 * Relation.java
 *
 * Created on 15. 2. 2018, 15:08:37 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

/**
 * A basic relation interface.
 * 
 * @author burgetr
 */
public interface Relation
{
    public static final Relation ONRIGHT = new SimpleRelation("onRight");
    public static final Relation ONLEFT = new SimpleRelation("onLeft").setInverse(ONRIGHT);
    public static final Relation AFTER = new SimpleRelation("after");
    public static final Relation BEFORE = new SimpleRelation("before").setInverse(AFTER);
    public static final Relation SAMELINE = new SimpleRelation("sameLine").setSymmetric(true);
    public static final Relation UNDER = new SimpleRelation("under");
    public static final Relation UNDERHEADING = new SimpleRelation("underHeading");
    public static final Relation BELOW = new SimpleRelation("below");
    public static final Relation ABOVE = new SimpleRelation("above").setInverse(BELOW);
    public static final Relation LINEBELOW = new SimpleRelation("lineBelow");
    
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
