/**
 * ModelTransformer.java
 *
 * Created on 30. 3. 2021, 18:38:17 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

/**
 * A base class for all model creators and loaders.
 * 
 * @author burgetr
 */
public class ModelTransformer
{
    private IRIFactory iriFactory;
    
    
    public ModelTransformer(IRIFactory iriFactory)
    {
        this.iriFactory = iriFactory;
    }

    /**
     * Gets the IRI factory used for creating the IRIs when building a RDF graph.
     * @param iriFactory
     */
    public IRIFactory getIriFactory()
    {
        return iriFactory;
    }

    /**
     * Configures the IRI factory used for creating the IRIs when building a RDF graph.
     * @param iriFactory
     */
    public void setIriFactory(IRIFactory iriFactory)
    {
        this.iriFactory = iriFactory;
    }
}
