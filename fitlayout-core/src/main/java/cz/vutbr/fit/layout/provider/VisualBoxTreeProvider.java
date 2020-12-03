/**
 * VisualBoxTreeProvider.java
 *
 * Created on 3. 12. 2020, 11:22:52 by burgetr
 */
package cz.vutbr.fit.layout.provider;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.PageBoxTreeBuilder;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;

/**
 * An artifact provider that creates a visual box tree from another box tree.
 * 
 * @author burgetr
 */
public class VisualBoxTreeProvider extends BaseArtifactService
{

    public VisualBoxTreeProvider()
    {
    }

    @Override
    public String getId()
    {
        return "FitLayout.VisualBoxTree";
    }

    @Override
    public String getName()
    {
        return "Visual box tree provider";
    }

    @Override
    public String getDescription()
    {
        return "Creates a visual box tree from another box tree";
    }

    @Override
    public IRI getConsumes()
    {
        return BOX.Page;
    }

    @Override
    public IRI getProduces()
    {
        return BOX.Page;
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        return createVisualTree((Page) input);
    }

    //===================================================================================
    
    private Page createVisualTree(Page input)
    {
        PageBoxTreeBuilder builder = new PageBoxTreeBuilder(true, false);
        return builder.processPage(input);
    }

}
