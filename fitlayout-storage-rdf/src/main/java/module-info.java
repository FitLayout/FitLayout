module cz.vutbr.fit.layout.rdf 
{
    requires java.xml;
    requires java.desktop;
    requires org.slf4j;
    requires rdf4j.onejar;
    requires unbescape;
    
    requires transitive cz.vutbr.fit.layout.core;
    
    exports cz.vutbr.fit.layout.rdf;
    exports cz.vutbr.fit.layout.rdf.model;
}
