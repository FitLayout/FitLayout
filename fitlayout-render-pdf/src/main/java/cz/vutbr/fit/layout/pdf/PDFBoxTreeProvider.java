/**
 * PDFBoxTreeProvider.java
 *
 * Created on 12. 10. 2022, 12:54:53 by burgetr
 */
package cz.vutbr.fit.layout.pdf;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.xml.sax.SAXException;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.pdf.impl.PDFBoxTreeBuilder;
import cz.vutbr.fit.layout.pdf.impl.PageImpl;

/**
 * 
 * @author burgetr
 */
public class PDFBoxTreeProvider extends BaseArtifactService
{
    private String urlstring;
    private boolean acquireImages; 
    private boolean includeScreenshot;
    
    private PDFBoxTreeBuilder builder;
    
    public PDFBoxTreeProvider()
    {
        urlstring = null;
        acquireImages = false;
        includeScreenshot = true;
    }
    
    public PDFBoxTreeProvider(URL url)
    {
        this.urlstring = url.toString();
        this.acquireImages = false;
        this.includeScreenshot = true;
    }
    
    public PDFBoxTreeProvider(URL url, boolean acquireImages, boolean includeScreenshot)
    {
        this.urlstring = url.toString();
        this.acquireImages = acquireImages;
        this.includeScreenshot = includeScreenshot;
    }

    @Override
    public String getId()
    {
        return "FitLayout.PDF";
    }

    @Override
    public String getName()
    {
        return "PDF document renderer";
    }

    @Override
    public String getDescription()
    {
        return "Renders PDF documents";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(6);
        ret.add(new ParameterString("url", 0, 64));
        ret.add(new ParameterBoolean("acquireImages"));
        ret.add(new ParameterBoolean("includeScreenshot"));
        return ret;
    }
    
    public String getUrl()
    {
        return urlstring;
    }

    public void setUrl(String url)
    {
        urlstring = new String(url);
    }

    public boolean getAcquireImages()
    {
        return acquireImages;
    }

    public void setAcquireImages(boolean acquireImages)
    {
        this.acquireImages = acquireImages;
    }

    public boolean getIncludeScreenshot()
    {
        return includeScreenshot;
    }

    public void setIncludeScreenshot(boolean includeScreenshot)
    {
        this.includeScreenshot = includeScreenshot;
    }

    @Override
    public IRI getConsumes()
    {
        return null;
    }

    @Override
    public IRI getProduces()
    {
        return BOX.Page;
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException
    {
        if (urlstring == null || urlstring.isBlank())
            throw new ServiceException("No URL provided");
        
        try {
            final Page page = getPage();
            final IRI pageIri = getServiceManager().getArtifactRepository().createArtifactIri(page);
            page.setIri(pageIri);
            return page;
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    public Page getPage() throws IOException, SAXException
    {
        builder = new PDFBoxTreeBuilder(false, true);
        builder.setAcquireImages(acquireImages);
        builder.setIncludeScreenshot(includeScreenshot);
        builder.parse(urlstring);
        PageImpl page = (PageImpl) builder.getPage();
        page.setCreator(getId());
        page.setCreatorParams(getParamString());
        return page;
    }
    

    
    
}
