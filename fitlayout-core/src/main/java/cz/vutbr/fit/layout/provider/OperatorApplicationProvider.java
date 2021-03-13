/**
 * OperatorProvider.java
 *
 * Created on 29. 10. 2020, 11:46:23 by burgetr
 */
package cz.vutbr.fit.layout.provider;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultArea;
import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * An artifact provider that consumes an area tree, applies a list of operators and produces
 * a new area tree.
 * @author burgetr
 */
public class OperatorApplicationProvider extends BaseArtifactService
{
    private static Logger log = LoggerFactory.getLogger(OperatorApplicationProvider.class);
    
    /** A comma-separated list of operator IDs */
    String operatorList;
    
    /** Selected operators */
    private List<AreaTreeOperator> operators;
    
    
    public OperatorApplicationProvider()
    {
    }

    public OperatorApplicationProvider(String operatorList)
    {
        setOperatorList(operatorList);
    }

    public String getOperatorList()
    {
        return operatorList;
    }

    public void setOperatorList(String operatorList)
    {
        this.operatorList = operatorList;
        String[] ids = operatorList.split(",");
        for (String id : ids)
        {
            ParametrizedOperation serv = getServiceManager().findParmetrizedService(id.trim());
            if (serv != null && serv instanceof AreaTreeOperator)
            {
                operators.add((AreaTreeOperator) serv);
            }
            else
            {
                log.error("Couldn't find operator '{}'", id.trim());
            }
        }
    }

    public List<AreaTreeOperator> getOperators()
    {
        return operators;
    }

    public void setOperators(List<AreaTreeOperator> operators)
    {
        this.operators = operators;
    }

    @Override
    public String getId()
    {
        return "FitLayout.ApplyOperators";
    }

    @Override
    public String getName()
    {
        return "Operator application provider";
    }

    @Override
    public String getDescription()
    {
        return "Applies a list of operators on an area tree";
    }

    @Override
    public IRI getConsumes()
    {
        return SEGM.AreaTree;
    }

    @Override
    public IRI getProduces()
    {
        return SEGM.AreaTree;
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (input != null && input instanceof AreaTree)
            return createAreaTree((AreaTree) input);
        else
            throw new ServiceException("Source artifact not provider or not an area tree");
        
    }

    //===================================================================================
    
    private AreaTree createAreaTree(AreaTree input)
    {
        // make a deep copy of the tree
        DefaultAreaTree ret = new DefaultAreaTree(input);
        Area root = new DefaultArea(input.getRoot());
        recursiveCopyChildren(root, input.getRoot());
        ret.setParentIri(input.getIri()); //the new tree is the child artifact of the original tree
        ret.setLabel(getId());
        ret.setCreator(getId());
        ret.setCreatorParams(getOperatorDescription());
        
        // apply operators
        for (AreaTreeOperator op : getOperators())
        {
            op.apply(ret);
        }
        
        return ret;
    }
    
    private void recursiveCopyChildren(Area destArea, Area srcArea)
    {
        for (Area src : srcArea.getChildren())
        {
            Area dest = new DefaultArea(src);
            destArea.appendChild(dest);
            recursiveCopyChildren(dest, src);
        }
    }
    
    private String getOperatorDescription()
    {
        StringBuilder ret = new StringBuilder();
        for (AreaTreeOperator op : getOperators())
        {
            if (ret.length() != 0)
                ret.append(" + ");
            ret.append(op.getId());
            ret.append('(').append(op.getParamString()).append(')');
        }
        return ret.toString();
    }
    
}
