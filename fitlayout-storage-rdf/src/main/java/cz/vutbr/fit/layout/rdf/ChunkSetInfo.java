/**
 * ChunkSetInfo.java
 *
 * Created on 10. 4. 2021, 21:18:32 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFChunkSet;

/**
 * 
 * @author burgetr
 */
public class ChunkSetInfo extends RDFArtifactInfo
{
    private IRI areaTreeIri;

    public ChunkSetInfo(Model model, IRI areaTreeIri)
    {
        super(model, areaTreeIri);
    }

    @Override
    protected boolean processStatement(Statement st)
    {
        boolean sret = super.processStatement(st);
        boolean ret = true;
        
        if (st.getPredicate().equals(SEGM.hasAreaTree))
        {
            if (st.getObject() instanceof IRI)
            {
                areaTreeIri = (IRI) st.getObject();
            }
        }
        else
            ret = false;
        
        return sret || ret;
    }
    
    public IRI getAreaTreeIri()
    {
        return areaTreeIri;
    }

    public void setAreaTreeIri(IRI areaTreeIri)
    {
        this.areaTreeIri = areaTreeIri;
    }

    public void applyToChunkSet(RDFChunkSet cset)
    {
        applyToArtifact(cset);
        cset.setAreaTreeIri(areaTreeIri);
        cset.setAdditionalStatements(getAdditionalStatements());
    }

}
