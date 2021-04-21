/**
 * Renderer.java
 *
 * Created on 7. 2. 2021, 9:10:22 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * A command-line interface to page rendering.
 * 
 * @author burgetr
 */
@Command(name = "RENDER", sortOptions = false, abbreviateSynopsis = true,
    description = "Renders a page")
public class Renderer extends CliCommand implements Callable<Integer>
{
    public enum Backend { cssbox, puppeteer };
    
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

    @Parameters(arity = "1", index = "0", description = "Input page URL")
    protected String url;

    @Override
    public Integer call() throws Exception
    {
        try {
            Page page = render(url, backend, width, height, ropts);
            getCli().setPage(page);
            System.err.println("  Created: " + page);
            
            return 0;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (ServiceException e) {
            System.err.println("Rendering failed: " + e.getMessage());
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
    public Page render(String url, Backend backend, int width, int height, Map<String, String> params)
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
        System.err.println("Rendering: " + op);
        if (op != null)
        {
            Map<String, Object> sparams = new HashMap<>();
            sparams.put("url", url);
            sparams.put("width", width);
            sparams.put("height", height);
            if (params != null)
                sparams.putAll(params);
            ServiceManager.setServiceParams(op, sparams);
            System.err.println("  Params: " + op.getParamString());
            
            Artifact page = ((ArtifactService) op).process(null);
            return (Page) page;
        }
        else
        {
            return null;
        }
    }
    
}
