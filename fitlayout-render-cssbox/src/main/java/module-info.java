module cz.vutbr.fit.layout.cssbox 
{
    requires java.xml;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires org.slf4j;
    requires net.sf.cssbox;
    requires net.sf.cssbox.jstyleparser;
    requires net.sf.cssbox.pdf2dom;
    requires net.sf.cssbox.pdf2domcssbox;
    
    requires transitive cz.vutbr.fit.layout.core;
    requires rdf4j.onejar;
    
    exports cz.vutbr.fit.layout.cssbox;
    
    provides cz.vutbr.fit.layout.api.ArtifactService with cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
}
