module cz.vutbr.fit.layout.tools 
{
    requires java.xml;
    requires java.desktop;
    requires java.scripting;
    requires org.slf4j;
    
    requires cz.vutbr.fit.layout.core;
    requires cz.vutbr.fit.layout.cssbox;
    requires cz.vutbr.fit.layout.segm;
    
    requires net.sf.cssbox; //TODO get rid of the cssbox dependency
    requires jline;
}
