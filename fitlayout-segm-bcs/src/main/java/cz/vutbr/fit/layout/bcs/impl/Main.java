package cz.vutbr.fit.layout.bcs.impl;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.fit.cssbox.layout.Viewport;
import org.fit.pis.cssbox.PageLoader;
import org.fit.pis.in.FileLoader;
import org.fit.pis.out.ImageOutput;
import org.fit.pis.out.TextOutput;
import org.xml.sax.SAXException;

public class Main
{
    public static final String home = "./";
    public static double threshold = -1;


    public static void process(Rectangle view, ArrayList<PageArea> areas, String imageString, Boolean debug) throws Exception {
        ArrayList<PageArea> groups;
        ArrayList<PageArea> ungrouped;
        AreaProcessor2 h;
        ImageOutput out;
        TextOutput textOut;

//         textOut = new TextOutput(areas);
//         textOut.save(home+imageString+"-boxes.txt");

        h = new AreaProcessor2(areas, (int)view.getWidth(), (int)view.getHeight());
        if (threshold > 0) h.setThreshold(threshold);
        if (debug != null) h.setDebug(debug);

        groups = h.extractGroups(h.getAreas());
        ungrouped = h.getUngrouped();

        /* For the sake of the right name */
        threshold = h.getThreshold();

        out = new ImageOutput(view, groups, ungrouped);
        out.save(home+imageString+"-boxes-"+threshold+".png");

        textOut = new TextOutput(groups);
        textOut.save(home+imageString+"-boxes-"+threshold+".txt");
    }


    public static void main(String []args) throws Exception, IOException, SAXException
    {
        String urlString;
        String imageString;
        Viewport view;
        Boolean debug = null;
        PageLoader pl;
        AreaCreator c;
        Rectangle r;
        ArrayList<PageArea> areas;
        FileLoader fl;


        if (args.length < 1)
        {
            System.out.println("./run.sh <address>[ <threshold>[ debug]]");
            return;
        } else if (args.length == 1) {
            threshold = 0.3;
        } else {
            threshold = new Double(args[1]);
            if (args.length > 2) {
                debug = new Boolean(args[2]);
            }
        }

        urlString = args[0];
        if (urlString.startsWith("http")) {
            imageString = urlString.replaceFirst("https?://", "").replaceFirst("/$", "").replaceAll("/", "-").replaceAll("\\?.*", "");

            pl = new PageLoader(new URL(urlString));
            view = pl.getViewport(new java.awt.Dimension(1000, 600));
            pl.save(home+imageString+".png");
            c = new AreaCreator(view.getWidth(), view.getHeight());
            r = new Rectangle(view.getWidth(), view.getHeight());
            areas = c.getAreas(view.getRootBox());
        } else {
            imageString = urlString.substring(urlString.lastIndexOf('/'), urlString.lastIndexOf("-boxes.txt"));

            fl = new FileLoader(urlString);
            fl.save(home+imageString+".png");

            r = fl.getPageDimensions();
            areas = fl.getAreas();
        }

        if (imageString.length() > 128) imageString = imageString.substring(0, 128);
        process(r, areas, imageString, debug);

        System.exit(0); /* Can't just return, as the AWT Thread was created */
    }
}
