/**
 * Export.java
 *
 * Created on 15. 2. 2021, 19:44:24 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.io.HTMLOutputOperator;
import cz.vutbr.fit.layout.io.ImageOutputDisplay;
import cz.vutbr.fit.layout.io.XMLBoxOutput;
import cz.vutbr.fit.layout.io.XMLOutputOperator;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.AreaModelBuilder;
import cz.vutbr.fit.layout.rdf.BoxModelBuilder;
import cz.vutbr.fit.layout.rdf.Serialization;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * @author burgetr
 */
@Command(name = "EXPORT", sortOptions = false, abbreviateSynopsis = true, 
    description = "Exports the last created artifact (page or area tree)")
public class Export extends CliCommand implements Callable<Integer>
{
    public enum Format { xml, turtle, html, png };

    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-o", "--output-file"}, paramLabel = "path", description = "output file path")
    protected File outfile;

    @Option(order = 2, names = {"-f", "--format"}, paramLabel = "format", description = "Output format: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE})")
    protected Format format = Format.xml;
    
    @Override
    public Integer call() throws Exception
    {
        try {
            Artifact a = getCli().getLastArtifact();
            
            if (a == null)
            {
                errNoArtifact("EXPORT");
                return 1;
            }
            
            System.err.println("Exporting " + a);
            final OutputStream os;
            if (outfile == null)
                os = new BufferedOutputStream(System.out);
            else
                os = new FileOutputStream(outfile);
            
            if (a instanceof Page)
            {
                writeOutput((Page) a, os, format);
                if (outfile != null)
                    System.err.println("  Written to " + outfile);
            }
            else if (a instanceof AreaTree)
            {
                Page page = getCli().getPage();
                writeOutput((AreaTree) a, page, os, format);
                if (outfile != null)
                    System.err.println("  Written to " + outfile);
            }
            
            os.close();
            
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
   
    //=========================================================================================
    
    public void writeOutput(Page page, OutputStream os, Format format) throws IOException
    {
        switch (format)
        {
            case turtle:
                outputRDF(page, os, Serialization.TURTLE);
                break;
            case xml:
                outputXML(page, os);
                break;
            case html:
                outputHTML(page, os);
                break;
            case png:
                outputPNG(page, os);
        }
    }
    
    public void outputRDF(Page page, OutputStream os, String mimeType) throws IOException
    {
        BoxModelBuilder builder = new BoxModelBuilder();
        Model graph = builder.createGraph(page);
        Serialization.modelToStream(graph, os, mimeType);
        os.close();
    }
    
    public void outputXML(Page page, OutputStream os) throws IOException
    {
        PrintWriter out = new PrintWriter(os);
        XMLBoxOutput xml = new XMLBoxOutput(true);
        xml.dumpTo(page, out);
        out.close();
    }
    
    public void outputHTML(Page page, OutputStream os) throws IOException
    {
        PrintWriter out = new PrintWriter(os);
        HTMLOutputOperator html = new HTMLOutputOperator();
        html.dumpTo(page, out);
        out.close();
    }

    public void outputPNG(Page page, OutputStream os) throws IOException
    {
        ImageOutputDisplay disp = new ImageOutputDisplay(page.getWidth(), page.getHeight());
        if (page.getPngImage() != null)
            disp.drawPage(page, true);
        else
            disp.drawPage(page, false);
        disp.saveTo(os);
    }
    
    //=========================================================================================

    public void writeOutput(AreaTree atree, Page page, OutputStream os, Format format) throws IOException
    {
        switch (format)
        {
            case turtle:
                outputRDF(atree, os, Serialization.TURTLE);
                break;
            case xml:
                outputXML(atree, os);
                break;
            case html:
                outputHTML(atree, page, os);
                break;
            case png:
                outputPNG(atree, page, os);
                break;
        }
    }
    
    public void outputRDF(AreaTree atree, OutputStream os, String mimeType) throws IOException
    {
        AreaModelBuilder builder = new AreaModelBuilder();
        Model graph = builder.createGraph(atree);
        Serialization.modelToStream(graph, os, mimeType);
        os.close();
    }
    
    public void outputXML(AreaTree atree, OutputStream os) throws IOException
    {
        XMLOutputOperator out = new XMLOutputOperator(null, true);
        PrintWriter writer = new PrintWriter(os);
        out.dumpTo(atree, writer);
        writer.close();
    }
    
    public void outputHTML(AreaTree atree, Page page, OutputStream os) throws IOException
    {
        if (page == null)
            throw new IllegalArgumentException("HTML export requires a page available (use RENDER or LOAD).");
        PrintWriter out = new PrintWriter(os);
        HTMLOutputOperator html = new HTMLOutputOperator();
        html.dumpTo(atree, page, out);
        out.close();
    }

    public void outputPNG(AreaTree atree, Page page, OutputStream os) throws IOException
    {
        ImageOutputDisplay disp = new ImageOutputDisplay(page.getWidth(), page.getHeight());
        if (page.getPngImage() != null)
            disp.drawPage(page, true);
        else
            disp.drawPage(page, false);
        showAreas(disp, atree.getRoot(), null);
        disp.saveTo(os);
    }

    private void showAreas(OutputDisplay disp, Area root, String nameSubstring)
    {
        if (nameSubstring == null || root.toString().contains(nameSubstring))
            disp.drawExtent(root);
        for (int i = 0; i < root.getChildCount(); i++)
            showAreas(disp, root.getChildAt(i), nameSubstring);
    }

}
