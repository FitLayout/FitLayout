/**
 * SuperAreaOperator.java
 *
 * Created on 24. 10. 2013, 14:40:09 by burgetr
 */
package cz.vutbr.fit.layout.segm.op;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.impl.BaseOperator;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.segm.Config;
import cz.vutbr.fit.layout.segm.Separators;

/**
 * Detects the larger visual areas and creates the artificial area nodes.
 * 
 * @author burgetr
 */
public class SuperAreaOperator extends BaseOperator
{
    /** Recursion depth limit while detecting the sub-areas */
    protected int depthLimit;

    /**
     * Creates the deparator with default parameter values.
     */
    public SuperAreaOperator()
    {
        depthLimit = 2;
    }
    
    /**
     * Creates the operator.
     * @param depthLimit Recursion depth limit while detecting the sub-areas
     */
    public SuperAreaOperator(int depthLimit)
    {
        this.depthLimit = depthLimit;
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.Segm.SuperAreas";
    }
    
    @Override
    public String getName()
    {
        return "Super areas";
    }

    @Override
    public String getDescription()
    {
        return "Detects larger visual areas and creates the artificial area nodes";
    }

    @Override
    public String getCategory()
    {
        return "restructure";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(1);
        ret.add(new ParameterInt("depthLimit"));
        return ret;
    }

    public int getDepthLimit()
    {
        return depthLimit;
    }

    public void setDepthLimit(int depthLimit)
    {
        this.depthLimit = depthLimit;
    }

    //==============================================================================
    
    @Override
    public void apply(AreaTree atree)
    {
        recursiveFindSuperAreas(atree.getRoot());
    }

    @Override
    public void apply(AreaTree atree, Area root)
    {
        recursiveFindSuperAreas(root);
    }

    //==============================================================================

    protected GroupAnalyzer createGroupAnalyzer(Area root)
    {
        return Config.createGroupAnalyzer(root);
    }
    
    //==============================================================================
    
    /**
     * Goes through all the areas in the tree and tries to join their sub-areas into single
     * areas.
     */
    private void recursiveFindSuperAreas(Area root)
    {
        for (int i = 0; i < root.getChildCount(); i++)
            recursiveFindSuperAreas(root.getChildAt(i));
        findSuperAreas(root, depthLimit);
    }
    
    /**
     * Creates syntetic super areas by grouping the subareas of the given area.
     * @param the root area to be processed
     * @param passlimit the maximal number of passes while some changes occur 
     */ 
    public void findSuperAreas(Area root, int passlimit)
    {
        if (root.getChildCount() > 0)
        {
            boolean changed = true;
            int pass = 0;
            Separators.createSeparatorsForArea(root);
            while (changed && pass < passlimit)
            {
                changed = false;
                
                GroupAnalyzer groups = createGroupAnalyzer(root);
                
                Vector<Area> chld = new Vector<Area>();
                chld.addAll(root.getChildren());
                while (chld.size() > 1) //we're not going to group a single element
                {
                    //get the super area
                    List<Area> selected = new ArrayList<>();
                    int index = root.getIndex(chld.firstElement());
                    Area grp = null;
                    if (chld.firstElement().isLeaf())
                        grp = groups.findSuperArea(chld.firstElement(), selected);
                    if (selected.size() == root.getChildCount())
                    {
                        //everything grouped into one group - it makes no sense to create a new one
                        //System.out.println("(contains all)");
                        break;
                    }
                    else
                    {
                        //add a new area
                        if (selected.size() > 1)
                        {
                            root.insertChild(grp, index);
                            //add(grp); //add the new group to the end of children (so that it is processed again later)
                            for (Area a : selected)
                                grp.appendChild(a);
                            chld.removeAll(selected);
                            grp.updateTopologies();;
                            findSuperAreas(grp, passlimit - 1); //in the next level, we use smaller pass limit to stop the recursion
                            changed = true;
                        }
                        else
                        {
                             //couldn't group the first element -- remove it and go on
                            chld.removeElementAt(0);
                        }
                    }
                }
                root.updateTopologies();
                Separators.removeSimpleSeparators(root);
                //System.out.println("Pass: " + pass + " changed: " + changed);
                pass++;
            }
        }
    }

}
