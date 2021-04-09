module cz.vutbr.fit.layout.core
{
    requires transitive java.desktop;
    requires transitive rdf4j.onejar;
    requires org.slf4j;
    
    exports cz.vutbr.fit.layout.api;
    exports cz.vutbr.fit.layout.impl;
    exports cz.vutbr.fit.layout.model;
    exports cz.vutbr.fit.layout.ontology;
    exports cz.vutbr.fit.layout.provider;
    
    uses cz.vutbr.fit.layout.api.ArtifactService;
    uses cz.vutbr.fit.layout.api.AreaTreeOperator;
    uses cz.vutbr.fit.layout.api.ScriptObject;
    
    provides cz.vutbr.fit.layout.api.ArtifactService with
        cz.vutbr.fit.layout.provider.OperatorApplicationProvider,
        cz.vutbr.fit.layout.provider.VisualBoxTreeProvider;
}
