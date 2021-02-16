/**
 * Export.java
 *
 * Created on 15. 2. 2021, 19:44:24 by burgetr
 */
package cz.vutbr.fit.layout.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;

import org.eclipse.rdf4j.model.Model;

import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.io.HTMLOutputOperator;
import cz.vutbr.fit.layout.io.XMLBoxOutput;
import cz.vutbr.fit.layout.io.XMLOutputOperator;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.rdf.AreaModelBuilder;
import cz.vutbr.fit.layout.rdf.BoxModelBuilder;
import cz.vutbr.fit.layout.rdf.Serialization;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * @author burgetr
 */
@Command(name = "EXPORT", sortOptions = false, abbreviateSynopsis = true, description = "Exports the last created artifact")
public class Export extends CliCommand implements Callable<Integer>
{
    public enum Format { xml, turtle, html, png };

    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-o", "--output-file"}, paramLabel = "path", required = true, description = "output file path")
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
                System.err.println("Nothing to export.");
                return 1;
            }
            
            System.out.println("Exporting " + a);
            if (a instanceof Page)
            {
                writeOutput((Page) a, outfile, format);
                System.out.println("  Written to " + outfile);
            }
            else if (a instanceof AreaTree)
            {
                Page page = getCli().getPage();
                writeOutput((AreaTree) a, page, outfile, format);
                System.out.println("  Written to " + outfile);
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
   
    //=========================================================================================
    
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

    //=========================================================================================

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
        if (page == null)
            throw new IllegalArgumentException("HTML export requires a page available (use RENDER or LOAD).");
        FileOutputStream os = new FileOutputStream(outfile);
        PrintWriter out = new PrintWriter(os);
        HTMLOutputOperator html = new HTMLOutputOperator();
        html.dumpTo(atree, page, out);
        out.close();
    }

}
