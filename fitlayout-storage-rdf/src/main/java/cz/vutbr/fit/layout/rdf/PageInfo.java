package cz.vutbr.fit.layout.rdf;

import java.util.Date;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;

import cz.vutbr.fit.layout.ontology.BOX;


/**
 * Class represents a specific launch info
 * 
 * @author milicka
 *
 */
public class PageInfo {

	private Date date;
	private IRI id;
	private String url;
	private String title;
	
	
    public PageInfo(Model model) 
    {
        for (Statement st : model) 
        {
            if (st.getSubject() instanceof IRI)
            {
                id = (IRI) st.getSubject();
                if (st.getPredicate().equals(BOX.sourceUrl)) {
                    url = st.getObject().stringValue();
                } else if (st.getPredicate().equals(BOX.launchDatetime)) {
                    Value val = st.getObject();
                    if (val instanceof Literal)
                        date = ((Literal) val).calendarValue().toGregorianCalendar().getTime();
                } else if (st.getPredicate().equals(BOX.hasTitle)) {
                    title = st.getObject().stringValue();
                }
            }
        }
        
    }
    
	public IRI getId() {
		return id;
	}
	
	public Date getDate() {
		return date;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getTitle() {
	    return title;
	}
	
}
