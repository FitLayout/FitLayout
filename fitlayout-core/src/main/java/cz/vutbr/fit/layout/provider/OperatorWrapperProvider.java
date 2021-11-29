/**
 * OperatorWrapperProvider.java
 *
 * Created on 12. 3. 2021, 19:18:39 by burgetr
 */
package cz.vutbr.fit.layout.provider;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.SEGM;

/**
 * An area tree provider that wraps a single area tree operator. It consumes an area tree
 * applies the given operator with params and produces another area tree.
 * @author burgetr
 */
public class OperatorWrapperProvider extends BaseArtifactService
{
    private AreaTreeOperator operator;

    public OperatorWrapperProvider(AreaTreeOperator operator)
    {
        this.operator = operator;
    }

    public AreaTreeOperator getOperator()
    {
        return operator;
    }
    
    @Override
    public String getId()
    {
        return operator.getId();
    }

    @Override
    public String getName()
    {
        return operator.getName();
    }

    @Override
    public String getDescription()
    {
        return operator.getDescription();
    }

    @Override
    public String getCategory()
    {
        return operator.getCategory();
    }

    @Override
    public List<Parameter> defineParams()
    {
        return operator.getParams();
    }

    @Override
    public boolean setParam(String name, Object value)
    {
        return operator.setParam(name, value);
    }

    @Override
    public Object getParam(String name)
    {
        return operator.getParam(name);
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
        Area root = ret.createArea(input.getRoot());
        recursiveCopyChildren(ret, root, input.getRoot());
        ret.setRoot(root);
        ret.setParentIri(input.getIri()); //the new tree is the child artifact of the original tree
        ret.setLabel(getId());
        ret.setCreator(getId());
        ret.setCreatorParams(getParamString());
        
        // apply the operator
        operator.apply(ret);
        
        return ret;
    }
    
    private void recursiveCopyChildren(AreaTree atree, Area destArea, Area srcArea)
    {
        for (Area src : srcArea.getChildren())
        {
            Area dest = atree.createArea(src);
            destArea.appendChild(dest);
            recursiveCopyChildren(atree, dest, src);
        }
    }
    
}
