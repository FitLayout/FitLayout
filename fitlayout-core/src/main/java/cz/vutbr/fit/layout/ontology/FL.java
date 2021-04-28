package cz.vutbr.fit.layout.ontology;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * FitLayout Core Ontology.
 * <p>
 * FITLayout shared types and properties..
 * <p>
 * Namespace FL.
 * Prefix: {@code <http://fitlayout.github.io/ontology/fitlayout.owl#>}
 */
public class FL {

	/** {@code http://fitlayout.github.io/ontology/fitlayout.owl#} **/
	public static final String NAMESPACE = "http://fitlayout.github.io/ontology/fitlayout.owl#";

	/** {@code fl} **/
	public static final String PREFIX = "fl";

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#Artifact}.
	 * <p>
	 * An artifact created during the page processing
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#Artifact">Artifact</a>
	 */
	public static final IRI Artifact;

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#createdOn}.
	 * <p>
	 * Creation date/time for an artifact or page set
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#createdOn">createdOn</a>
	 */
	public static final IRI createdOn;

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#creator}.
	 * <p>
	 * An identification of the service that created an artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#creator">creator</a>
	 */
	public static final IRI creator;

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#creatorParams}.
	 * <p>
	 * Parametres of the service used for creating an artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#creatorParams">creatorParams</a>
	 */
	public static final IRI creatorParams;

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#hasParentArtifact}.
	 * <p>
	 * Assigns a parent artifact to another artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#hasParentArtifact">hasParentArtifact</a>
	 */
	public static final IRI hasParentArtifact;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		Artifact = factory.createIRI(FL.NAMESPACE, "Artifact");
		createdOn = factory.createIRI(FL.NAMESPACE, "createdOn");
		creator = factory.createIRI(FL.NAMESPACE, "creator");
		creatorParams = factory.createIRI(FL.NAMESPACE, "creatorParams");
		hasParentArtifact = factory.createIRI(FL.NAMESPACE, "hasParentArtifact");
	}

	private FL() {
		//static access only
	}

}
