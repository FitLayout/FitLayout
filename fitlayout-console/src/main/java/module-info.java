module cz.vutbr.fit.layout.console 
{
    requires java.xml;
    requires java.scripting;
    requires org.slf4j;
    
    requires transitive cz.vutbr.fit.layout.core;
    requires cz.vutbr.fit.layout.io;
    requires cz.vutbr.fit.layout.cssbox;
    requires cz.vutbr.fit.layout.puppeteer;
    requires cz.vutbr.fit.layout.segm;
    
    exports cz.vutbr.fit.layout.console;
    exports cz.vutbr.fit.layout.console.process;
    
    requires jline;
    requires rdf4j.onejar;
}
