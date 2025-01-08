/**
 * Relations.java
 *
 * Created on 26. 12. 2021, 22:02:21 by burgetr
 */
package cz.vutbr.fit.layout.patterns;

import cz.vutbr.fit.layout.impl.DefaultRelation;
import cz.vutbr.fit.layout.model.Relation;

/**
 * 
 * @author burgetr
 */
public class Relations
{
    // relations with a specific meaning
    public static final Relation RIGHTOF = new DefaultRelation("rightOf");
    public static final Relation LEFTOF = new DefaultRelation("leftOf").setInverse(RIGHTOF);
    public static final Relation AFTER = new DefaultRelation("after");
    public static final Relation BEFORE = new DefaultRelation("before").setInverse(AFTER);
    public static final Relation SAMELINE = new DefaultRelation("sameLine").setSymmetric(true);
    public static final Relation UNDER = new DefaultRelation("under");
    public static final Relation UNDERHEADING = new DefaultRelation("underHeading");
    public static final Relation BELOW = new DefaultRelation("below");
    public static final Relation ABOVE = new DefaultRelation("above").setInverse(BELOW);
    public static final Relation LINEBELOW = new DefaultRelation("lineBelow");
    
    // generic relations
    public static final Relation HASNEIGHBOR = new DefaultRelation("hasNeighbor");
    public static final Relation HASCONT = new DefaultRelation("hasCont");
}
