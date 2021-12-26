/**
 * Relations.java
 *
 * Created on 26. 12. 2021, 22:02:21 by burgetr
 */
package cz.vutbr.fit.layout.patterns.model;

import cz.vutbr.fit.layout.model.Relation;

/**
 * 
 * @author burgetr
 */
public class Relations
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
}
