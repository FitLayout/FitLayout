/**
 * AreaTreeInfo.java
 *
 * Created on 30. 10. 2020, 13:12:28 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.rdf.model.RDFAreaTree;

/**
 * 
 * @author burgetr
 */
public class AreaTreeInfo extends RDFArtifactInfo
{

    public AreaTreeInfo(Model model, IRI areaTreeIri)
    {
        super(model, areaTreeIri);
    }

    public void applyToAreaTree(RDFAreaTree atree)
    {
        applyToArtifact(atree);
        atree.setAdditionalStatements(getAdditionalStatements());
    }
    
}
