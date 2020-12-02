/**
 * Console.java
 *
 * Created on 27. 1. 2015, 13:40:51 by burgetr
 */
package cz.vutbr.fit.layout.console;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.script.ScriptException;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.vutbr.fit.layout.console.process.ScriptableProcessor;

/**
 * 
 * @author burgetr
 */
public class Console
{
    private static Logger log = LoggerFactory.getLogger(Console.class);
    
    private ScriptableProcessor proc;
    
    public Console()
    {
        proc = new ScriptableProcessor();
        proc.put("console", this);
        init();
    }
    
    public void interactiveSession(InputStream in, PrintStream out, PrintStream err) throws IOException
    {
        BufferedReader rin = new BufferedReader(new InputStreamReader(in));
        Writer wout = new OutputStreamWriter(out);
        Writer werr = new OutputStreamWriter(err);
        proc.setIO(rin, wout, werr);
        
        LineReader reader = LineReaderBuilder.builder().build();
        String histfile = System.getProperty("user.home") + "/.fitlayout/console_history";
        reader.setVariable(LineReader.HISTORY_FILE, histfile);
        
        try
        {
            initSession();
        } catch (ScriptException e) {
            log.error(e.getMessage());
        }
        
        while (true)
        {
            proc.flushIO();
            out.println();
            try
            {
                String cmd = reader.readLine(prompt());
                Object result = proc.execCommand(cmd);
                if (result != null)
                    out.println(result.toString());
                else
                    out.println("undefined");
            } catch (ScriptException e) {
                err.println(e.getMessage());
            } catch (UserInterruptException e) {
                // Ignore
            } catch (EndOfFileException e) {
                break;
            } catch (Exception e) {
                err.println("Internal exception: " + e.getMessage());
                e.printStackTrace(err);
            }
        }
        out.println();
    }
    
    protected String prompt()
    {
        return "FitLayout> ";
    }
    
    protected ScriptableProcessor getProcessor()
    {
        return proc;
    }
    
    /**
     * This is called when the console is created. Usable for adding custom object to the script engine.
     */
    protected void init()
    {
        //nothing just now
    }
    
    /**
     * This function is called at the beginning of the interactive session. Usable for executing
     * various init scripts.  
     * @throws ScriptException
     */
    protected void initSession() throws ScriptException
    {
        proc.execInternal("init.js");
    }
    
    //=============================================================================================
    
    public void exit()
    {
        System.exit(0);
    }
    
    public void logToFile(String path, String text)
    {
        try(FileWriter fw = new FileWriter(path, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(text);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }        
    }
    
    public void truncateFile(String path)
    {
        try(FileWriter fw = new FileWriter(path, false))
            {
                
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }        
    }
    
    //=============================================================================================
    
    public static void main(String[] args)
    {
        System.out.println("FitLayout interactive console");
        Console con = new Console();
        try
        {
            con.interactiveSession(System.in, System.out, System.err);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
