package cz.vutbr.fit.layout.rdf;

import java.util.Base64;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.rdf.model.RDFPage;


/**
 * Class extends the artifact info by page-specific properties.
 * 
 * @author burgetr
 */
public class PageInfo extends ArtifactInfo
{
	private String title;
    private String url;
	private byte[] pngImage;
	
	
    public PageInfo(Model model) 
    {
        super(model);
    }

    protected void processStatement(Statement st)
    {
        super.processStatement(st);
        
        if (st.getPredicate().equals(BOX.hasTitle))
        {
            title = st.getObject().stringValue();
        }
        else if (st.getPredicate().equals(BOX.sourceUrl))
        {
            url = st.getObject().stringValue();
        }
        else if (st.getPredicate().equals(BOX.pngImage))
        {
            String dataStr = st.getObject().stringValue();
            try {
                pngImage = Base64.getDecoder().decode(dataStr);
            } catch (IllegalArgumentException e) {
                pngImage = null;
            }
        }
    }
    
	public String getTitle() 
	{
	    return title;
	}
	
    public String getUrl() 
    {
        return url;
    }

    public byte[] getPngImage()
    {
        return pngImage;
    }

    public void applyToPage(RDFPage page)
    {
        applyToArtifact(page);
        page.setTitle(getTitle());
        if (getPngImage() != null)
            page.setPngImage(getPngImage());
    }
    
}
