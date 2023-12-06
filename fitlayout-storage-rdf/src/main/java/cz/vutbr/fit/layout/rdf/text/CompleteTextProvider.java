/**
 * CompleteTextProvider.java
 *
 * Created on 4. 12. 2023, 21:12:00 by burgetr
 */
package cz.vutbr.fit.layout.rdf.text;

import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
import cz.vutbr.fit.layout.rdf.RDFStorage;
import cz.vutbr.fit.layout.rdf.model.RDFArea;
import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;

/**
 * A service that adds complete text to every area in the RDF area tree.
 * 
 * @author burgetr
 */
public class CompleteTextProvider extends BaseArtifactService
{

    public CompleteTextProvider()
    {
    }
    
    @Override
    public String getId()
    {
        return "FitLayout.CompleteText";
    }

    @Override
    public String getName()
    {
        return "Add Complete Text to Areas";
    }

    @Override
    public String getDescription()
    {
        return "Adds the a:textContent property to every area in the RDF area tree.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        return Collections.emptyList();
    }

    @Override
    public IRI getConsumes()
    {
        return SEGM.AreaTree;
    }

    @Override
    public IRI getProduces()
    {
        return null;
    }

    @Override
    public String getCategory()
    {
        return "Text";
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (input != null && input instanceof RDFAreaTree)
        {
            var repo = getServiceManager().getArtifactRepository();
            if (repo instanceof RDFArtifactRepository)
            {
                var storage = ((RDFArtifactRepository) repo).getStorage();
                recursiveAddText(((AreaTree) input).getRoot(), storage, input.getIri());
                return null;
            }
            else
                throw new ServiceException("RDFArtifactRepository is required for storing the connections");
        }
        else
            throw new ServiceException("Source artifact not specified or not an RDF area tree");    
    }
    
    private void recursiveAddText(Area root, RDFStorage storage, IRI context)
    {
        IRI subj = ((RDFArea) root).getIri();
        String text = root.getText();
        storage.addValue(subj, SEGM.text, text, context);
    }
    
}
