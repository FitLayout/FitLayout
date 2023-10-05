/**
 * Segmentator.java
 *
 * Created on 2. 12. 2020, 11:02:11 by burgetr
 */
package cz.vutbr.fit.layout.tools.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.tools.CliCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * A command-line interface to page segmentation.
 * 
 * @author burgetr
 */
@Command(name = "SEGMENT", sortOptions = false, abbreviateSynopsis = true,
    description = "Performs segmentation on a page")
public class Segmentator extends CliCommand implements Callable<Integer>
{
    public enum Method { simple, vips, bcs, cormier };
    
    @Option(order = 100, names = {"-h", "--help"}, usageHelp = true, description = "print help")
    protected boolean help;
    
    @Option(order = 1, names = {"-m", "--method"}, description = "Segmentation method: ${COMPLETION-CANDIDATES} (${DEFAULT-VALUE})")
    protected Method method = Method.vips;
    
    @Option(order = 2, names = {"-O", "--options"}, paramLabel = "KEY=VALUE", split = "\\,", splitSynopsisLabel = ",", description = "Segmentation method options")
    protected Map<String, String> sopts;

    @Override
    public Integer call() throws Exception
    {
        try {
            Page page = getCli().getPage();
            if (page == null) {
                errNoPage("segmentation");
                return 1;
            }
            
            AreaTree atree = segment(page, method, sopts);
            getCli().setAreaTree(atree);
            System.err.println("  Created: " + atree);
            
            return 0;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
        } catch (ServiceException e) {
            System.err.println("Rendering failed: " + e.getMessage());
        }
        
        return 1;
    }

    public AreaTree segment(Page page, Method method, Map<String, String> sopts)
    {
        String serviceId = "";
        switch (method)
        {
            case simple:
                serviceId = "FitLayout.BasicAreas";
                break;
            case bcs:
                serviceId = "FitLayout.BCS";
                break;
            case vips:
                serviceId = "FitLayout.VIPS";
                break;
            case cormier:
                serviceId = "FitLayout.Cormier";
                break;
        }
        
        ParametrizedOperation op = getCli().getServiceManager().findParmetrizedService(serviceId);
        System.err.println("Segmentation: " + op);
        if (op != null)
        {
            if (sopts != null)
            {
                Map<String, Object> sparams = new HashMap<>(sopts);
                ServiceManager.setServiceParams(op, sparams);
            }
            
            System.err.println("  Params: " + op.getParamString());
            Artifact atree = ((ArtifactService) op).process(page);
            return (AreaTree) atree;
        }
        else
        {
            return null;
        }
    }
    
}
