/**
 * Renderer.java
 *
 * Created on 7. 2. 2021, 9:10:22 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.model.Model;

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
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * A command-line interface to page rendering.
 * 
 * @author burgetr
 */
@Command(name = "Renderer")
public class Renderer implements Callable<Integer>
{
    public enum Backend { cssbox, puppeteer };
    public enum Format { xml, turtle };
    
    @Option(names = {"-h", "--help"}, usageHelp = true, description = "print help")
    private boolean help;
    
    @Option(names = {"-W", "--width"}, paramLabel = "width", description = "Browser window width in pixels (${DEFAULT-VALUE})")
    private int width = 1200;
    
    @Option(names = {"-H", "--height"}, paramLabel = "height", description = "Browser window height in pixels (${DEFAULT-VALUE})")
    private int height = 800;
    
    @Option(names = {"-b", "--backend"}, paramLabel = "backend_name", description = "The backend to use: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE})")
    private Backend backend = Backend.cssbox;
    
    @Option(names = {"-f", "--format"}, paramLabel = "format", description = "Output format: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE})")
    private Format format = Format.xml;

    @Parameters(arity = "1", index = "0", description = "Input page URL")
    private URL url;

    @Parameters(arity = "1", index = "1", description = "Output file path")
    private File outfile;

    @Override
    public Integer call() throws Exception
    {
        try {
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
        
        return 0;
    }

    public void invoke(String[] args)
    {
    }
    
    /**
     * Renders a page using the given backend and returns the page structure.
     * @param url
     * @param backend
     * @param width
     * @param height
     * @return The created page.
     */
    public Page render(URL url, Backend backend, int width, int height)
    {
        switch (backend) 
        {
            case cssbox:
                CSSBoxTreeProvider cprovider = new CSSBoxTreeProvider(url, width, height, 1.0f);
                cprovider.setServiceManager(getServiceManager());
                Artifact cpage = cprovider.process(null);
                return (Page) cpage;
            case puppeteer:
                PuppeteerTreeProvider pprovider = new PuppeteerTreeProvider(url, width, height, 1, false, false);
                pprovider.setServiceManager(getServiceManager());
                Artifact ppage = pprovider.process(null);
                return (Page) ppage;
        }
        return null;
    }
    
    public void writeOutput(Page page, File outfile, Format format) throws IOException
    {
        switch (format)
        {
            case turtle:
                outputRDF(page, outfile, Serialization.TURTLE);
                break;
            case xml:
                outputXML(page, outfile);
                break;
        }
    }
    
    public void outputRDF(Page page, File outfile, String mimeType) throws IOException
    {
        BoxModelBuilder builder = new BoxModelBuilder();
        Model graph = builder.createGraph(page);
        FileOutputStream os = new FileOutputStream(outfile);
        Serialization.modelToStream(graph, os, mimeType);
        os.close();
    }
    
    public void outputXML(Page page, File outfile) throws IOException
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
    
}
