module cz.vutbr.fit.layout.rdf 
{
    requires java.xml;
    requires java.desktop;
    requires org.slf4j;
    requires rdf4j.http.client;
    requires rdf4j.query;
    requires rdf4j.queryalgebra.evaluation;
    requires rdf4j.repository.api;
    requires rdf4j.repository.http;
    requires rdf4j.repository.sparql;
    requires rdf4j.rio.api;
    requires rdf4j.util;
    requires unbescape;
    
    requires transitive cz.vutbr.fit.layout.core;
    
    exports cz.vutbr.fit.layout.rdf;
    exports cz.vutbr.fit.layout.rdf.model;
    
    provides cz.vutbr.fit.layout.api.ArtifactService with cz.vutbr.fit.layout.rdf.RDFBoxTreeProvider;
}
