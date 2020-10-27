/**
 * GUIProcessor.java
 *
 * Created on 5. 2. 2015, 10:59:17 by burgetr
 */
package cz.vutbr.fit.layout.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ArtifactRepository;
import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;

/**
 * 
 * @author burgetr
 */
public class GUIProcessor extends BaseProcessor
{
    //private static Logger log = LoggerFactory.getLogger(GUIProcessor.class);

    private List<AreaTreeOperator> selectedOperators;
    private List<Map<String, Object>> operatorParams;
    
    public GUIProcessor(ArtifactRepository repository)
    {
        super(repository);
        selectedOperators = new ArrayList<AreaTreeOperator>();
        operatorParams = new ArrayList<Map<String, Object>>();
        loadConfig();
    }
    
    /**
     * Creates a new artifact from the nearest applicable parent using the given provider
     * and adds the new artifact to the artifact tree.
     * @param selected the selected artifact to start with when looking for the nearest applicable parent
     * @param provider the provider to be used for creating the new artifact
     * @return
     */
    public Artifact createArtifact(Artifact selected, ArtifactService provider)
    {
        Artifact parent = null;
        if (provider.getConsumes() != null)
            parent = getNearestArtifact(selected, provider.getConsumes());
        if (parent != null)
        {
            Artifact result = provider.process(parent);
            return result;
        }
        else
            return null;
    }

    public Artifact getParentArtifact(Artifact artifact)
    {
        final IRI parentIri = artifact.getParentIri();
        if (parentIri != null)
            return getRepository().getArtifact(parentIri);
        else
            return null;
    }

    /**
     * Finds the nearest artifact of the given type in the artifact tree.
     * @param start the sartifact to start with
     * @param artifactType the required type
     * @return
     */
    public Artifact getNearestArtifact(Artifact start, IRI artifactType)
    {
        Artifact ret = start;
        while (ret != null && !artifactType.equals(ret.getArtifactType()))
            ret = getParentArtifact(ret);
        return ret;
    }
    
    //========================================================================================
    
    public List<AreaTreeOperator> getSelectedOperators()
    {
        return selectedOperators;
    }

    public List<Map<String, Object>> getOperatorParams()
    {
        return operatorParams;
    }
    
    public AreaTree applyOperators(AreaTree src)
    {
        for (int i = 0; i < selectedOperators.size(); i++)
        {
            var op = selectedOperators.get(i);
            var params = operatorParams.get(i);
            apply(src, op, params);
        }
        return src;
    }

    public void loadConfig()
    {
        //TODO
    }
    
    //========================================================================================
    
}
