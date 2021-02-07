/**
 * Renderer.java
 *
 * Created on 7. 2. 2021, 9:10:22 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.rdf4j.model.Model;

import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
import cz.vutbr.fit.layout.impl.DefaultArtifactRepository;
import cz.vutbr.fit.layout.io.XMLBoxOutput;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.puppeteer.PuppeteerTreeProvider;
import cz.vutbr.fit.layout.rdf.BoxModelBuilder;
import cz.vutbr.fit.layout.rdf.Serialization;

/**
 * A command-line interface to page rendering.
 * 
 * @author burgetr
 */
public class Renderer
{
    @Argument(alias = "h", description = "print help")
    private boolean help;
    
    @Argument(alias = "W", description = "Browser window width")
    private Integer width = 1200;
    
    @Argument(alias = "H", description = "Browser window height")
    private Integer height = 800;
    
    @Argument(alias = "b", description = "Rendering backend to be used")
    private String backend = "cssbox";
    
    @Argument(alias = "f", description = "Output format")
    private String format = "xml";
    

    public void invoke(String[] args)
    {
        try {
            List<String> params = Args.parse(this, args, true);
            
            if (help)
            {
                printHelp();
                return;
            }
            
            if (params.size() < 2)
            {
                System.err.println("Page URL and/or output file not provided");
                printHelp();
                return;
            }
            
            final URL url = new URL(params.get(0));
            final String outfile = params.get(1);
            Page page = render(url, backend, width, height);
            
            System.out.println(page);
            writeOutput(page, outfile, format);
            System.out.println("Written to " + outfile);
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("Invalid url: " + e.getMessage());
        } catch (ServiceException e) {
            System.err.println("Rendering failed: " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
    /**
     * Renders a page using the given backend and returns the page structure.
     * @param url
     * @param backend
     * @param width
     * @param height
     * @return The created page.
     */
    public Page render(URL url, String backend, int width, int height)
    {
        if ("cssbox".equalsIgnoreCase(backend))
        {
            CSSBoxTreeProvider provider = new CSSBoxTreeProvider(url, width, height, 1.0f);
            provider.setServiceManager(getServiceManager());
            Artifact page = provider.process(null);
            return (Page) page;
        }
        else if ("puppeteer".equalsIgnoreCase(backend))
        {
            PuppeteerTreeProvider provider = new PuppeteerTreeProvider(url, width, height, 1, false, false);
            provider.setServiceManager(getServiceManager());
            Artifact page = provider.process(null);
            return (Page) page;
        }
        else
            throw new IllegalArgumentException("Illegal backend name: " + backend + ". Legal values: [cssbox, puppeteer]");
    }
    
    public void writeOutput(Page page, String outfile, String format) throws IOException
    {
        if ("turtle".equals(format))
        {
            outputRDF(page, outfile, Serialization.TURTLE);
        }
        else if ("xml".equals(format))
        {
            outputXML(page, outfile);
        }
        else
            throw new IllegalArgumentException("Illegal format name: " + backend + ". Legal values: [xml, turtle]");
    }
    
    public void outputRDF(Page page, String outfile, String mimeType) throws IOException
    {
        BoxModelBuilder builder = new BoxModelBuilder();
        Model graph = builder.createGraph(page);
        FileOutputStream os = new FileOutputStream(outfile);
        Serialization.modelToStream(graph, os, mimeType);
        os.close();
    }
    
    public void outputXML(Page page, String outfile) throws IOException
    {
        FileOutputStream os = new FileOutputStream(outfile);
        PrintWriter out = new PrintWriter(os);
        XMLBoxOutput xml = new XMLBoxOutput(true);
        xml.dumpTo(page, out);
        out.close();
    }
    
    /**
     * Creates a basic service manager and repository for generating the artifacr IRIs
     * @return the service manager
     */
    protected ServiceManager getServiceManager()
    {
        ServiceManager sm = ServiceManager.create();
        sm.setArtifactRepository(new DefaultArtifactRepository());
        return sm;
    }
    
    public void printHelp()
    {
        Args.usage(System.err, this, Cli.class);
    }
    

}
