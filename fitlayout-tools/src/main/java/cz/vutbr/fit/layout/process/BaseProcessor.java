/**
 * BaseProcessor.java
 *
 * Created on 5. 2. 2015, 9:34:28 by burgetr
 */
package cz.vutbr.fit.layout.process;

import java.util.Map;

import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.LogicalAreaTree;
import cz.vutbr.fit.layout.model.Page;

/**
 * A base of a processor. It takes care about the existing providers and operators and their invocation.
 * 
 * @author burgetr
 */
public abstract class BaseProcessor
{
    private static Logger log = LoggerFactory.getLogger(BaseProcessor.class);
    
    private Page page;
    private AreaTree atree;
    private LogicalAreaTree ltree;
    

    public BaseProcessor()
    {
    }
    
    public Map<String, ArtifactService> getArtifactProviders(IRI artifactType)
    {
        return ServiceManager.findArtifactProviders(artifactType);
    }

    public Map<String, AreaTreeOperator> getOperators()
    {
        return ServiceManager.findAreaTreeOperators();
    }

    public Page getPage()
    {
        return page;
    }

    public void setPage(Page page)
    {
        this.page = page;
    }

    public AreaTree getAreaTree()
    {
        return atree;
    }

    public void setAreaTree(AreaTree atree)
    {
        this.atree = atree;
    }

    public LogicalAreaTree getLogicalAreaTree()
    {
        return ltree;
    }

    public void setLogicalAreaTree(LogicalAreaTree ltree)
    {
        this.ltree = ltree;
    }

    /**
     * Runs the default segmentation process with the default parameter values.
     * @return The resulting area tree or {@code null} for an unsuccessfull segmentation
     */
    public abstract AreaTree segmentPage();
    
    /**
     * Runs the default logical tree builder with the default parameter values.
     * @return The resulting logical area tree or {@code null} for an unsuccessfull build
     */
    public abstract LogicalAreaTree buildLogicalTree();
    
    
    //======================================================================================================
    
    public Artifact processArtifact(Artifact input, ArtifactService provider, Map<String, Object> params)
    {
        if (provider instanceof ParametrizedOperation)
            ServiceManager.setServiceParams((ParametrizedOperation) provider, params);
        return provider.process(input);
    }
    
    public void apply(AreaTreeOperator op, Map<String, Object> params)
    {
        if (atree != null)
        {
            ServiceManager.setServiceParams(op, params);
            op.apply(atree);
        }
        else
            log.error("Couldn't apply " + op.getId() + ": no area tree");
    }

    //======================================================================================================
    
    protected void treesCompleted()
    {
        //this is called when the tree creation is finished
    }
    

}
