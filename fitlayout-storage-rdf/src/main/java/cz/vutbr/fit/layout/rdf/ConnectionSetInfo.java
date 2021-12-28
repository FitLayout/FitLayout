/**
 * ConnectionSetInfo.java
 *
 * Created on 28. 12. 2021, 10:08:26 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import cz.vutbr.fit.layout.ontology.SEGM;
import cz.vutbr.fit.layout.rdf.model.RDFConnectionSet;

/**
 * 
 * @author burgetr
 */
public class ConnectionSetInfo extends RDFArtifactInfo
{
    private IRI sourceIri;
    private IRI pageIri;

    public ConnectionSetInfo(Model model, IRI areaTreeIri)
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
                sourceIri = (IRI) st.getObject();
            }
        }
        else if (st.getPredicate().equals(SEGM.hasSourcePage))
        {
            if (st.getObject() instanceof IRI)
            {
                pageIri = (IRI) st.getObject();
            }
        }
        else
            ret = false;
        
        return sret || ret;
    }
    
    public IRI getSourceIri()
    {
        return sourceIri;
    }

    public void setSourceIri(IRI areaTreeIri)
    {
        this.sourceIri = areaTreeIri;
    }

    public IRI getPageIri()
    {
        return pageIri;
    }

    public void setPageIri(IRI pageIri)
    {
        this.pageIri = pageIri;
    }

    public void applyToConnectionSet(RDFConnectionSet cset)
    {
        applyToArtifact(cset);
        cset.setSourceIri(sourceIri);
        cset.setPageIri(pageIri);
        cset.setAdditionalStatements(getAdditionalStatements());
    }


}
