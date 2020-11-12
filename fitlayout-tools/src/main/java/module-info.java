module cz.vutbr.fit.layout.tools 
{
    requires java.xml;
    requires java.scripting;
    requires java.desktop;
    requires org.slf4j;
    
    requires transitive cz.vutbr.fit.layout.core;
    requires cz.vutbr.fit.layout.cssbox;
    requires cz.vutbr.fit.layout.puppeteer;
    requires cz.vutbr.fit.layout.segm;
    
    exports cz.vutbr.fit.layout.process;
    exports cz.vutbr.fit.layout.process.js;
    exports cz.vutbr.fit.layout.tools;
    exports cz.vutbr.fit.layout.tools.io;
    
    requires jline;
    requires rdf4j.onejar;
}
