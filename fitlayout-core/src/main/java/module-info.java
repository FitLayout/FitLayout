module cz.vutbr.fit.layout.core
{
    requires transitive java.desktop;
    requires transitive rdf4j.model;
    requires org.slf4j;
    
    exports cz.vutbr.fit.layout.api;
    exports cz.vutbr.fit.layout.impl;
    exports cz.vutbr.fit.layout.model;
    exports cz.vutbr.fit.layout.ontology;
    
    uses cz.vutbr.fit.layout.api.ArtifactService;
    uses cz.vutbr.fit.layout.api.AreaTreeOperator;
    uses cz.vutbr.fit.layout.api.PageStorage;
    uses cz.vutbr.fit.layout.api.ScriptObject;
}
