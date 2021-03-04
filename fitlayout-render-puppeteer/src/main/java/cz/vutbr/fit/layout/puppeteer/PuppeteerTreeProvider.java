/**
 * PuppeteerTreeProvider.java
 *
 * Created on 5. 11. 2020, 20:43:10 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.ParameterBoolean;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.impl.ParameterString;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.puppeteer.impl.BoxTreeBuilder;
import cz.vutbr.fit.layout.puppeteer.impl.PageImpl;

/**
 * 
 * @author burgetr
 */
public class PuppeteerTreeProvider extends BaseArtifactService
{
    private String urlstring;
    private int width;
    private int height;
    private int persist;
    private boolean acquireImages;
    private boolean includeScreenshot;
    private boolean replaceImagesWithAlt; //not published as a parameter now
    
    private BoxTreeBuilder builder;
    
    public PuppeteerTreeProvider()
    {
        urlstring = null;
        width = 1200;
        height = 800;
        persist = 1;
        acquireImages = false;
        includeScreenshot = true;
    }
    
    public PuppeteerTreeProvider(URL url, int width, int height, int persist, boolean acquireImages, boolean includeScreenshot)
    {
        this.urlstring = url.toString();
        this.width = width;
        this.height = height;
        this.persist = persist;
        this.acquireImages = acquireImages;
        this.includeScreenshot = includeScreenshot;
    }

    @Override
    public String getId()
    {
        return "FitLayout.Puppeteer";
    }

    @Override
    public String getName()
    {
        return "Puppeteer-based HTML renderer";
    }

    @Override
    public String getDescription()
    {
        return "Uses the CSSBox rendering engine for obtaining the box tree.";
    }

    @Override
    public List<Parameter> defineParams()
    {
        List<Parameter> ret = new ArrayList<>(6);
        ret.add(new ParameterString("url", 0, 64));
        ret.add(new ParameterInt("width", 10, 9999));
        ret.add(new ParameterInt("height", 10, 9999));
        ret.add(new ParameterInt("persist", 0, 3));
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
    
    public int getPersist()
    {
        return persist;
    }

    public void setPersist(int persist)
    {
        this.persist = persist;
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

    public boolean getReplaceImagesWithAlt()
    {
        return replaceImagesWithAlt;
    }

    public void setReplaceImagesWithAlt(boolean replaceImagesWithAlt)
    {
        this.replaceImagesWithAlt = replaceImagesWithAlt;
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
            return getPage();
        } catch (IOException e) {
            throw new ServiceException(e);
        } catch (InterruptedException e) {
            throw new ServiceException(e);
        }
    }

    public Page getPage() throws IOException, InterruptedException
    {
        builder = new BoxTreeBuilder(width, height, false, true);
        builder.setPersist(persist);
        builder.setAcquireImages(acquireImages);
        builder.setIncludeScreenshot(includeScreenshot);
        builder.parse(urlstring);
        PageImpl page = (PageImpl) builder.getPage();
        page.setCreator(getId());
        page.setCreatorParams(getParamString());
        IRI pageIri = getServiceManager().getArtifactRepository().createArtifactIri(page);
        page.setIri(pageIri);
        return page;
    }

}
