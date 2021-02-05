package cz.vutbr.fit.layout.rdf;

import java.util.Base64;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;

import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.rdf.model.RDFPage;


/**
 * Class extends the artifact info by page-specific properties.
 * 
 * @author burgetr
 */
public class PageInfo extends RDFArtifactInfo
{
	private int width;
	private int height;
    private String title;
    private String url;
	private byte[] pngImage;
	
	
    public PageInfo(Model model, IRI pageIri) 
    {
        super(model, pageIri);
        width = height = -1; //-1 means not set
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
    
	public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
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
        page.setWidth(width);
        page.setHeight(height);
        page.setTitle(getTitle());
        if (getPngImage() != null)
            page.setPngImage(getPngImage());
    }
    
}
