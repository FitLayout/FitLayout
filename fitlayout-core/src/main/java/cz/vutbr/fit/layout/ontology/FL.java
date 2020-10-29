package cz.vutbr.fit.layout.ontology;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * FITLayout system types and properties.
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
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#containsPage}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#containsPage">containsPage</a>
	 */
	public static final IRI containsPage;

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#createdOn}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#createdOn">createdOn</a>
	 */
	public static final IRI createdOn;

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#hasName}.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#hasName">hasName</a>
	 */
	public static final IRI hasName;

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#hasParentArtifact}.
	 * <p>
	 * Assigns a parent artifact to another artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#hasParentArtifact">hasParentArtifact</a>
	 */
	public static final IRI hasParentArtifact;

	/**
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#PageSet}.
	 * <p>
	 * A set of pages processed together.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#PageSet">PageSet</a>
	 */
	public static final IRI PageSet;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		Artifact = factory.createIRI(FL.NAMESPACE, "Artifact");
		containsPage = factory.createIRI(FL.NAMESPACE, "containsPage");
		createdOn = factory.createIRI(FL.NAMESPACE, "createdOn");
		hasName = factory.createIRI(FL.NAMESPACE, "hasName");
		hasParentArtifact = factory.createIRI(FL.NAMESPACE, "hasParentArtifact");
		PageSet = factory.createIRI(FL.NAMESPACE, "PageSet");
	}

	private FL() {
		//static access only
	}

}
