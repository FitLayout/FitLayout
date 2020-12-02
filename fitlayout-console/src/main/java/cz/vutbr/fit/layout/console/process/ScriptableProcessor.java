/**
 * ScriptableProcessor.java
 *
 * Created on 14. 1. 2015, 14:52:04 by burgetr
 */
package cz.vutbr.fit.layout.console.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.api.AreaTreeOperator;
import cz.vutbr.fit.layout.api.ArtifactService;
import cz.vutbr.fit.layout.api.OutputDisplay;
import cz.vutbr.fit.layout.api.ParametrizedOperation;
import cz.vutbr.fit.layout.api.ScriptObject;
import cz.vutbr.fit.layout.api.ServiceManager;
import cz.vutbr.fit.layout.impl.BaseProcessor;
import cz.vutbr.fit.layout.io.ImageOutputDisplay;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;

/**
 * 
 * @author burgetr
 */
public class ScriptableProcessor extends BaseProcessor
{
    private static Logger log = LoggerFactory.getLogger(ScriptableProcessor.class);

    private BufferedReader rin;
    private PrintWriter wout;
    private PrintWriter werr;
    private ScriptEngine engine;
    
    
    public ScriptableProcessor()
    {
        super();
        rin = new BufferedReader(new InputStreamReader(System.in));
        wout = new PrintWriter(System.out);
        werr = new PrintWriter(System.err);
    }

    //======================================================================================================
    // scripting interface
    
    public List<String> getOperatorIds()
    {
        return new ArrayList<String>(getOperators().keySet());
    }
    
    public List<String> getArtifactProviderIds(String artifactTypeIRI)
    {
        ValueFactory vf = SimpleValueFactory.getInstance();
        IRI artifactType = vf.createIRI(artifactTypeIRI);
        return new ArrayList<String>(getArtifactProviders(artifactType).keySet());
    }
    
    public void setServiceParams(String serviceName, Map<String, Object> params)
    {
        ParametrizedOperation op = getServiceManager().findParmetrizedService(serviceName);
        if (op != null)
            ServiceManager.setServiceParams(op, params);
        else
            log.error("setServiceParams: Unknown service: {}", serviceName);
    }
    
    public Artifact processArtifact(Artifact input, String providerName, Map<String, Object> params)
    {
        ArtifactService provider = getArtifactServices().get(providerName);
        if (provider != null)
        {
            return processArtifact(input, provider, params);
        }
        else
        {
            log.error("Unknown box tree provider: " + providerName);
            return null;
        }
    }
    
    public void apply(AreaTree atree, String operatorName, Map<String, Object> params)
    {
        /*System.out.println("Apply: " + operatorName + " : " + params);
        System.out.println(params.keySet());
        Object o1 = params.get("useConsistentStyle");
        Object o2 = params.get("maxLineEmSpace");*/

        AreaTreeOperator op = getOperators().get(operatorName);
        if (op != null)
        {
            apply(atree, op, params);
        }
        else
            log.error("Unknown operator " + operatorName);
        
    }
    
    //========================================================================
    
    /**
     * Draws the current page to an image file.
     * @param page the page to draw
     * @param path The path to the destination image file.
     */
    public void drawToImage(Page page, String path)
    {
        try
        {
            ImageOutputDisplay disp = new ImageOutputDisplay(page.getWidth(), page.getHeight());
            disp.drawPage(page);
            disp.saveTo(path);
        } catch (IOException e) {
            log.error("Couldn't write to " + path + ": " + e.getMessage());
        }
    }
    
    /**
     * Draws the page to an image file and marks selected areas in the image.
     * @param path The path to the destination image file.
     * @param areaNames A substring of the names of areas that should be marked in the image. When set to {@code null}, all the areas are marked.
     */
    public void drawToImageWithAreas(Page page, AreaTree areaTree, String path, String areaNames)
    {
        try
        {
            ImageOutputDisplay disp = new ImageOutputDisplay(page.getWidth(), page.getHeight());
            disp.drawPage(page);
            showAreas(disp, areaTree.getRoot(), areaNames);
            disp.saveTo(path);
        } catch (IOException e) {
            log.error("Couldn't write to " + path + ": " + e.getMessage());
        }
    }
    
    private void showAreas(OutputDisplay disp, Area root, String nameSubstring)
    {
        if (nameSubstring == null || root.toString().contains(nameSubstring))
            disp.drawExtent(root);
        for (int i = 0; i < root.getChildCount(); i++)
            showAreas(disp, root.getChildAt(i), nameSubstring);
    }
    
    //======================================================================================================
    // Script invocation

    protected ScriptEngine getEngine()
    {
        if (engine == null)
        {
            ScriptEngineManager factory = new ScriptEngineManager();
            engine = factory.getEngineByName("JavaScript");
            engine.put("proc", this);
            
            //rhino compatibility workaround, println should be replaced by print in all occurences
            try
            {
                engine.eval("if (typeof println == 'undefined') this.println = print;");
            } catch (ScriptException e) {
                e.printStackTrace();
            }
            
            Map<String, ScriptObject> scriptObjects = getServiceManager().findScriptObjects();
            for (Map.Entry<String, ScriptObject> obj : scriptObjects.entrySet())
            {
                engine.put(obj.getKey(), obj.getValue());
            }
        }
        return engine;
    }
    
    public void setIO(Reader in, Writer out, Writer err)
    {
        rin = new BufferedReader(in);
        wout = new PrintWriter(out);
        werr = new PrintWriter(err);
        
        ScriptContext ctx = getEngine().getContext();
        ctx.setReader(rin);
        ctx.setWriter(wout);
        ctx.setErrorWriter(werr);
        
        for (ScriptObject obj : getServiceManager().findScriptObjects().values())
            obj.setIO(in, out, err);
    }
    
    public void flushIO()
    {
        wout.flush();
        werr.flush();
    }
    
    public void put(String var, Object obj)
    {
        getEngine().put(var, obj);
    }
    
    public boolean execInternal(String scriptName) throws ScriptException
    {
        if (!scriptName.startsWith("/"))
            scriptName = "/" + scriptName;
        InputStream is = ScriptableProcessor.class.getResourceAsStream(scriptName);
        if (is != null)
        {
            getEngine().eval(new InputStreamReader(is));
            return true;
        }
        else
        {
            log.error("Couldn't access internal script " + scriptName);
            return false;
        }
    }

    public Object execCommand(String command) throws ScriptException
    {
        return getEngine().eval(command);
    }

}
