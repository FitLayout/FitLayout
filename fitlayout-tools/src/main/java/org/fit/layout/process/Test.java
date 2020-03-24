/**
 * Test.java
 *
 * Created on 14. 1. 2015, 11:08:12 by burgetr
 */
package org.fit.layout.process;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 
 * @author burgetr
 */
public class Test
{
    private static ScriptEngine engine;
    
    private static boolean runInternalScript(String name) throws ScriptException
    {
        InputStream is = ClassLoader.getSystemResourceAsStream(name);
        if (is != null)
        {
            engine.eval(new InputStreamReader(is));
            return true;
        }
        else
            return false;
    }
    
    public static void printInfo()
    {
        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (ScriptEngineFactory factory : factories)
        {
            System.out.println("ScriptEngineFactory Info");
            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();
            System.out.printf("\tScript Engine: %s (%s)\n", engName, engVersion);
            List<String> engNames = factory.getNames();
            for (String name : engNames)
            {
                System.out.printf("\tEngine Alias: %s\n", name);
            }
            System.out.printf("\tLanguage: %s (%s)\n", langName, langVersion);
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        //printInfo();
        try
        {
            // create a script engine manager
            ScriptEngineManager factory = new ScriptEngineManager();
            // create a JavaScript engine
            engine = factory.getEngineByName("JavaScript");
            // evaluate JavaScript code from String
            
            ScriptableProcessor proc = new ScriptableProcessor();
            engine.put("proc", proc);
            
            Bindings b1 = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
            Bindings b2 = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            
            //engine.eval("for (prop in this) { val = this[prop]; println(prop + ' = ' + val); }");
            //engine.eval("for (prop in context) { val = this[prop]; println(prop + ' = ' + val); }");
            runInternalScript("init.js");
        } catch (ScriptException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
