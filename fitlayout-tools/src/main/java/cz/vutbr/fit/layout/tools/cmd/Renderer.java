/**
 * Renderer.java
 *
 * Created on 7. 2. 2021, 9:10:22 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.io.HTMLOutputOperator;
import cz.vutbr.fit.layout.io.XMLBoxOutput;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.BoxModelBuilder;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * A command-line interface to page rendering.
 * 
 * @author burgetr
 */
@Command(name = "RENDER", sortOptions = false, abbreviateSynopsis = true)
public class Renderer extends CliCommand implements Callable<Integer>
{
    public enum Backend { cssbox, puppeteer };
    public enum Format { xml, turtle, html };
    
    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-W", "--width"}, paramLabel = "width", description = "Browser window width in pixels (${DEFAULT-VALUE})")
    protected int width = 1200;
    
    @Option(order = 2, names = {"-H", "--height"}, paramLabel = "height", description = "Browser window height in pixels (${DEFAULT-VALUE})")
    protected int height = 800;
    
    @Option(order = 3, names = {"-b", "--backend"}, paramLabel = "backend_name", description = "The backend to use: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE})")
    protected Backend backend = Backend.cssbox;
    
    @Option(order = 4, names = {"-O", "--options"}, paramLabel = "KEY=VALUE", split = "\\,", splitSynopsisLabel = ",", description = "Additional rendering backend options")
    protected Map<String, String> ropts;

    @Option(order = 5, names = {"-o", "--output-file"}, paramLabel = "path", description = "output file path")
    protected File outfile;

    @Option(order = 6, names = {"-f", "--format"}, paramLabel = "format", description = "Output format: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE})")
    protected Format format = Format.xml;
    
    @Parameters(arity = "1", index = "0", description = "Input page URL")
    protected URL url;

    @Override
    public Integer call() throws Exception
    {
        try {
            Page page = render(url, backend, width, height, ropts);
            getCli().setPage(page);
            System.out.println("  Created: " + page);
            
            if (outfile != null)
            {
                writeOutput(page, outfile, format);
                System.out.println("Written to " + outfile);
            }
            
            return 0;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (MalformedURLException e) {
            System.err.println("Invalid url: " + e.getMessage());
        } catch (ServiceException e) {
            System.err.println("Rendering failed: " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        
        return 1;
    }

    /**
     * Renders a page using the given backend and returns the page structure.
     * @param url
     * @param backend
     * @param width
     * @param height
     * @return The created page.
     */
    public Page render(URL url, Backend backend, int width, int height, Map<String, String> params)
    {
        String serviceId = "";
        switch (backend)
        {
            case cssbox:
                serviceId = "FitLayout.CSSBox";
                break;
            case puppeteer:
                serviceId = "FitLayout.Puppeteer";
                break;
        }
        
        ParametrizedOperation op = getCli().getServiceManager().findParmetrizedService(serviceId);
        System.out.println("Rendering: " + op);
        if (op != null)
        {
            Map<String, Object> sparams = new HashMap<>();
            sparams.put("url", url.toString());
            sparams.put("width", width);
            sparams.put("height", height);
            if (params != null)
                sparams.putAll(params);
            ServiceManager.setServiceParams(op, sparams);
            System.out.println("  Params: " + op.getParamString());
            
            Artifact page = ((ArtifactService) op).process(null);
            return (Page) page;
        }
        else
        {
            return null;
        }
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
            case html:
                outputHTML(page, outfile);
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
    
    public void outputHTML(Page page, File outfile) throws IOException
    {
        FileOutputStream os = new FileOutputStream(outfile);
        PrintWriter out = new PrintWriter(os);
        HTMLOutputOperator html = new HTMLOutputOperator();
        html.dumpTo(page, out);
        out.close();
    }
    
}
