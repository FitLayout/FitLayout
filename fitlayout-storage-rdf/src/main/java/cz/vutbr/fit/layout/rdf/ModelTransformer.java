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
    
    public ModelTransformer()
    {
        iriFactory = new DefaultIRIFactory();
    }

    public IRIFactory getIriFactory()
    {
        return iriFactory;
    }

    public void setIriFactory(IRIFactory iriFactory)
    {
        this.iriFactory = iriFactory;
    }
}
