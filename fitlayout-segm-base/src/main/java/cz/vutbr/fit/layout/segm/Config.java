/**
 * Config.java
 *
 * Created on 19.6.2009, 12:32:47 by burgetr
 */
package cz.vutbr.fit.layout.segm;

import cz.vutbr.fit.layout.segm.op.GroupAnalyzer;
import cz.vutbr.fit.layout.segm.op.SeparatorSet;
import cz.vutbr.fit.layout.segm.op.SeparatorSetHVS;


/**
 * Segmenation algorithm configuration. This class allows to select the implementation of used algorithms.
 * 
 * @author burgetr
 */
public class Config
{
    //debugging
    public static final boolean DEBUG_AREAS = false;
    public static final int DEBUG_DELAY = 30;
    
    /** The maximal distance of two areas allowed within a single line (in 'em' units) */
    public static final float MAX_LINE_EM_SPACE = 1.5f;
    
    /** The maximal difference of separator weights that are considered to be 'the same' */
    public static final int SEPARATOR_WEIGHT_THRESHOLD = 0;
    
    /** Maximal difference between left and right margin to consider the area to be centered (percentage of the parent area width) */
    public static final double CENTERING_THRESHOLD = 0.1;

    /** The maximal visual difference that is consideres as 'the same style' */ 
    public static final double FONT_SIZE_THRESHOLD = 1;
    public static final double FONT_WEIGHT_THRESHOLD = 0.8;
    public static final double FONT_STYLE_THRESHOLD = 0.8;
    public static final double TEXT_LUMINOSITY_THRESHOLD = 0.005;
    
    /** Maintain the same style during the line detection */
    public static final boolean CONSISTENT_LINE_STYLE = false;
    
    /** Tag probability threshold for considering the tag. The tags with their probability below this threshold
     * won't be considered at all. */
    public static final double TAG_PROBABILITY_THRESHOLD = 0.3;
    
    /**
     * Creates a group analyzer for an area using the selected implementation.
     * @param root the root area for separator detection
     * @return the created group analyzer
     */
    public static GroupAnalyzer createGroupAnalyzer(AreaImpl root)
    {
        //return new org.fit.segm.grouping.op.GroupAnalyzerByGrouping(root);
        //return new org.fit.segm.grouping.op.GroupAnalyzerBySeparators(root);
        //return new org.fit.segm.grouping.op.GroupAnalyzerByGroupingAndSeparators(root);
        //return new org.fit.segm.grouping.op.GroupAnalyzerByFlooding(root);
        return new cz.vutbr.fit.layout.segm.op.GroupAnalyzerByStyles(root, 1, false);
    }
    
    /**
     * Creates the separators for an area using the selected algorithm
     * @param root the root area
     * @return the created separator set
     */
    public static SeparatorSet createSeparators(AreaImpl root)
    {
        SeparatorSet sset;
        //sset = new SeparatorSetHV(root);
        sset = new SeparatorSetHVS(root);
        //sset = new SeparatorSetColumns(root);
        //sset = new SeparatorSetSim(root);
        //sset = new SeparatorSetGrid(root);
        
        sset.applyFinalFilters();
        return sset;
    }
    

}
