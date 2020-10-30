/**
 * ArtifactInfo.java
 *
 * Created on 30. 10. 2020, 13:08:12 by burgetr
 */
package cz.vutbr.fit.layout.rdf;

import java.util.Date;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import cz.vutbr.fit.layout.impl.BaseArtifact;
import cz.vutbr.fit.layout.ontology.FL;

/**
 * Information about an artifact obtained from a RDF model. 
 * 
 * @author burgetr
 */
public class ArtifactInfo
{
    private IRI iri;
    private IRI parentIri;
    private Date createdOn;
    private String creator;
    private String creatorParams;
    
    
    public ArtifactInfo(Model model) 
    {
        for (Statement st : model) 
        {
            processStatement(st);
        }
    }

    protected void processStatement(Statement st)
    {
        if (st.getSubject() instanceof IRI)
        {
            iri = (IRI) st.getSubject();
            if (st.getPredicate().equals(FL.createdOn))
            {
                Value val = st.getObject();
                if (val instanceof Literal) createdOn = ((Literal) val)
                        .calendarValue().toGregorianCalendar().getTime();
            }
            else if (st.getPredicate().equals(FL.hasParentArtifact))
            {
                Value val = st.getObject();
                if (val instanceof IRI)
                    parentIri = (IRI) val;
            }
            else if (st.getPredicate().equals(FL.creator))
            {
                creator = st.getObject().stringValue();
            }
            else if (st.getPredicate().equals(FL.creatorParams))
            {
                creatorParams = st.getObject().stringValue();
            }
        }
    }
    
    public IRI getIri() {
        return iri;
    }
    
    public IRI getParentIri()
    {
        return parentIri;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public String getCreator()
    {
        return creator;
    }

    public String getCreatorParams()
    {
        return creatorParams;
    }
    
    public void applyToArtifact(BaseArtifact a)
    {
        a.setIri(getIri());
        a.setParentIri(getParentIri());
        a.setCreatedOn(getCreatedOn());
        a.setCreator(getCreator());
        a.setCreatorParams(getCreatorParams());
    }
    
}
