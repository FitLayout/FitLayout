module cz.vutbr.fit.layout.bcs
{
    requires java.desktop;
    requires org.slf4j;

    requires transitive cz.vutbr.fit.layout.core;
    requires rdf4j.onejar;
    requires jsi;
    requires trove4j;
    
    exports cz.vutbr.fit.layout.bcs;

    provides cz.vutbr.fit.layout.api.ArtifactService
            with cz.vutbr.fit.layout.bcs.BCSProvider;
}
