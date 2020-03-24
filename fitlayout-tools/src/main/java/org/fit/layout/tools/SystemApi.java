/**
 * SystemApi.java
 *
 * Created on 16. 11. 2016, 9:24:20 by burgetr
 */
package org.fit.layout.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.fit.layout.api.ScriptObject;

/**
 * 
 * @author burgetr
 */
public class SystemApi implements ScriptObject
{
    @SuppressWarnings("unused")
    private BufferedReader rin;
    @SuppressWarnings("unused")
    private PrintWriter wout;
    private PrintWriter werr;

    @Override
    public String getVarName()
    {
        return "system";
    }

    @Override
    public void setIO(Reader in, Writer out, Writer err)
    {
        rin = new BufferedReader(in);
        wout = new PrintWriter(out);
        werr = new PrintWriter(err);
    }

    public void mkdir(String dir)
    {
        try {
            File f = new File(dir);
            f.mkdir();
        } catch (Exception e) {
            werr.println("Couldn't create " + dir + ": " + e.getMessage());
        }
    }
    
    public String getProperty(String name)
    {
        return System.getProperty(name, "");
    }
    
    public String[] readLines(String inputFile)
    {
        try {
            Path filePath = new File(inputFile).toPath();
            Charset charset = Charset.defaultCharset();        
            List<String> stringList = Files.readAllLines(filePath, charset);
            return stringList.toArray(new String[]{});
        } catch (IOException e) {
            werr.println("Couldn't read " + inputFile + ": " + e.getMessage());
            return new String[]{};
        }
    }
        
    
}
