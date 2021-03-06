module cz.vutbr.fit.layout.vips
{
    requires java.desktop;
    requires org.slf4j;

    requires transitive cz.vutbr.fit.layout.core;
    requires rdf4j.onejar;
    
    exports cz.vutbr.fit.layout.vips;

    provides cz.vutbr.fit.layout.api.ArtifactService
            with cz.vutbr.fit.layout.vips.VipsProvider;
}
