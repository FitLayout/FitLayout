module cz.vutbr.fit.layout.cssbox 
{
    requires java.xml;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires org.slf4j;
    requires net.sf.cssbox;
    requires net.sf.cssbox.jstyleparser;
    requires net.sf.cssbox.pdf2dom;
    
    requires cz.vutbr.fit.layout.core;
    
    provides cz.vutbr.fit.layout.api.BoxTreeProvider with cz.vutbr.fit.layout.cssbox.CSSBoxTreeProvider;
}
