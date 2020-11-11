module cz.vutbr.fit.layout.puppeteer 
{
    requires java.xml;
    requires java.desktop;
    requires org.slf4j;
    requires net.sf.cssbox.jstyleparser;
    
    requires transitive cz.vutbr.fit.layout.core;
    requires rdf4j.onejar;
    requires com.google.gson;
    
    exports cz.vutbr.fit.layout.puppeteer;
    exports cz.vutbr.fit.layout.puppeteer.parser;
    
    provides cz.vutbr.fit.layout.api.ArtifactService with cz.vutbr.fit.layout.puppeteer.PuppeteerTreeProvider;
}
