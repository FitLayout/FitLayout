/**
 * CompleteTextProvider.java
 *
 * Created on 4. 12. 2023, 21:12:00 by burgetr
 */
package cz.vutbr.fit.layout.rdf.text;

import java.util.Collections;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.DynamicModelFactory;
import org.eclipse.rdf4j.model.util.Values;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.RDFArtifactRepository;
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
                // create a new model with the text content triples
                Model model = (new DynamicModelFactory()).createEmptyModel();
                recursiveAddText(((AreaTree) input).getRoot(), model);
                // store the model in the context
                var storage = ((RDFArtifactRepository) repo).getStorage();
                storage.insertGraph(model, input.getIri());
                return null;
            }
            else
                throw new ServiceException("RDFArtifactRepository is required for storing the connections");
        }
        else
            throw new ServiceException("Source artifact not specified or not an RDF area tree");    
    }
    
    private void recursiveAddText(Area root, Model model)
    {
        IRI subj = ((RDFArea) root).getIri();
        String text = root.getText();
        model.add(subj, SEGM.text, Values.literal(text));
        for (Area child : root.getChildren())
            recursiveAddText(child, model);
    }
    
}
