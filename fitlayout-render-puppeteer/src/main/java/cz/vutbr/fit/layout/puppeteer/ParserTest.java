/**
 * Test.java
 *
 * Created on 5. 11. 2020, 20:54:29 by burgetr
 */
package cz.vutbr.fit.layout.puppeteer;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import com.google.gson.Gson;

import cz.vutbr.fit.layout.puppeteer.parser.BoxInfo;
import cz.vutbr.fit.layout.puppeteer.parser.InputFile;
import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.css.Term;

/**
 * 
 * @author burgetr
 */
public class ParserTest
{

    /**
     * @param args
     * @throws IOException 
     * @throws CSSException 
     */
    public static void main(String[] args) throws IOException, CSSException
    {
        FileReader fin = new FileReader(System.getProperty("user.home") + "/tmp/fitlayout/boxes.json");

        Gson gson = new Gson();
        InputFile file = gson.fromJson(fin, InputFile.class);
        System.out.println("parsed ok.");
        fin.close();
        
        BoxInfo box = file.getBoxes()[0];
        System.out.println(box.getCss());
        
        String ssheet = "* { " + box.getCss() + "}";
        StyleSheet sheet = CSSFactory.parseString(ssheet, new URL("http://base.url"));
        System.out.println(sheet);
        
        NodeData style = CSSFactory.createNodeData();
        RuleSet rule = (RuleSet) sheet.get(0);
        for (Declaration d : rule)
        {
            style.push(d);
        }
        
        System.out.println(style);
        
        Term<?> ff = style.getValue("font-family", false);
        System.out.println(ff);

        
        
    }

}
