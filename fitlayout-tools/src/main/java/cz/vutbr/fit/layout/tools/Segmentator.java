/**
 * Segmentator.java
 *
 * Created on 2. 12. 2020, 11:02:11 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.io.HTMLOutputOperator;
import cz.vutbr.fit.layout.io.XMLOutputOperator;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.AreaModelBuilder;
import cz.vutbr.fit.layout.rdf.Serialization;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A command-line interface to page segmentation.
 * 
 * @author burgetr
 */
@Command(name = "segment")
public class Segmentator extends Renderer
{
    public enum Method { vips, bcs };
    
    @Option(order = 10, names = {"-m", "--method"}, description = "Segmentation method: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE})")
    protected Method method = Method.vips;
    
    @Option(order = 11, names = {"--sopts"}, paramLabel = "KEY=VALUE", split = "\\,", splitSynopsisLabel = ",", description = "Segmentation method options")
    protected Map<String, String> sopts;

    @Override
    public Integer call() throws Exception
    {
        try {
            Page page = render(url, backend, width, height, ropts);
            System.out.println("  Created: " + page);
            
            AreaTree atree = segment(page, method, sopts);
            System.out.println("  Created: " + atree);
            
            writeOutput(atree, page, outfile, format);
            System.out.println("Written to " + outfile);
            
            return 0;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (ServiceException e) {
            System.err.println("Rendering failed: " + e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        
        return 1;
    }

    public AreaTree segment(Page page, Method method, Map<String, String> sopts)
    {
        String serviceId = "";
        switch (method)
        {
            case bcs:
                serviceId = "FitLayout.BCS";
                break;
            case vips:
                serviceId = "FitLayout.VIPS";
                break;
        }
        
        ParametrizedOperation op = getServiceManager().findParmetrizedService(serviceId);
        System.out.println("Segmentation: " + op);
        if (op != null)
        {
            if (sopts != null)
            {
                Map<String, Object> sparams = new HashMap<>(sopts);
                ServiceManager.setServiceParams(op, sparams);
            }
            
            System.out.println("  Params: " + op.getParamString());
            Artifact atree = ((ArtifactService) op).process(page);
            return (AreaTree) atree;
        }
        else
        {
            return null;
        }
    }
    
    public void writeOutput(AreaTree atree, Page page, File outfile, Format format) throws IOException
    {
        switch (format)
        {
            case turtle:
                outputRDF(atree, outfile, Serialization.TURTLE);
                break;
            case xml:
                outputXML(atree, outfile);
                break;
            case html:
                outputHTML(atree, page, outfile);
                break;
        }
    }
    
    public void outputRDF(AreaTree atree, File outfile, String mimeType) throws IOException
    {
        AreaModelBuilder builder = new AreaModelBuilder();
        Model graph = builder.createGraph(atree);
        FileOutputStream os = new FileOutputStream(outfile);
        Serialization.modelToStream(graph, os, mimeType);
        os.close();
    }
    
    public void outputXML(AreaTree atree, File outfile) throws IOException
    {
        XMLOutputOperator out = new XMLOutputOperator(outfile.getAbsolutePath(), true);
        out.apply(atree);
    }
    
    public void outputHTML(AreaTree atree, Page page, File outfile) throws IOException
    {
        FileOutputStream os = new FileOutputStream(outfile);
        PrintWriter out = new PrintWriter(os);
        HTMLOutputOperator html = new HTMLOutputOperator();
        html.dumpTo(atree, page, out);
        out.close();
    }
    
}
