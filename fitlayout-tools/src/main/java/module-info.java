module cz.vutbr.fit.layout.tools 
{
    requires org.slf4j;
    
    requires transitive cz.vutbr.fit.layout.core;
    requires cz.vutbr.fit.layout.cssbox;
    requires cz.vutbr.fit.layout.puppeteer;
    requires cz.vutbr.fit.layout.segm;
    requires cz.vutbr.fit.layout.rdf;
    requires cz.vutbr.fit.layout.io;
    
    exports cz.vutbr.fit.layout.tools;
    opens cz.vutbr.fit.layout.tools;
    
    requires rdf4j.onejar;
    requires info.picocli;
}
