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
	 * Artifact
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#Artifact}.
	 * <p>
	 * An artifact created during the page processing
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#Artifact">Artifact</a>
	 */
	public static final IRI Artifact;

	/**
	 * createdOn
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#createdOn}.
	 * <p>
	 * Creation date/time for an artifact or page set
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#createdOn">createdOn</a>
	 */
	public static final IRI createdOn;

	/**
	 * creator
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#creator}.
	 * <p>
	 * An identification of the service that created an artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#creator">creator</a>
	 */
	public static final IRI creator;

	/**
	 * creatorParams
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#creatorParams}.
	 * <p>
	 * Parametres of the service used for creating an artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#creatorParams">creatorParams</a>
	 */
	public static final IRI creatorParams;

	/**
	 * hasParentArtifact
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#hasParentArtifact}.
	 * <p>
	 * Assigns a parent artifact to another artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#hasParentArtifact">hasParentArtifact</a>
	 */
	public static final IRI hasParentArtifact;

	/**
	 * param
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#param}.
	 * <p>
	 * Service parameter name and value definition.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#param">param</a>
	 */
	public static final IRI param;

	/**
	 * paramName
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#paramName}.
	 * <p>
	 * Parameter name
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#paramName">paramName</a>
	 */
	public static final IRI paramName;

	/**
	 * paramValue
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#paramValue}.
	 * <p>
	 * Parameter value.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#paramValue">paramValue</a>
	 */
	public static final IRI paramValue;

	/**
	 * processedBy
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#processedBy}.
	 * <p>
	 * An identification of the service that (post-)processed the artifact.
	 * This is used to track the processing steps applied on the artifact.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#processedBy">processedBy</a>
	 */
	public static final IRI processedBy;

	/**
	 * SavedQuery
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#SavedQuery}.
	 * <p>
	 * A saved SPARQL query
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#SavedQuery">SavedQuery</a>
	 */
	public static final IRI SavedQuery;

	/**
	 * service
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#service}.
	 * <p>
	 * A service ID definition
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#service">service</a>
	 */
	public static final IRI service;

	/**
	 * Tagger
	 * <p>
	 * {@code http://fitlayout.github.io/ontology/fitlayout.owl#Tagger}.
	 * <p>
	 * A tagger that is able to assign tags to content rectangles.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/fitlayout.owl#Tagger">Tagger</a>
	 */
	public static final IRI Tagger;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		Artifact = factory.createIRI(FL.NAMESPACE, "Artifact");
		createdOn = factory.createIRI(FL.NAMESPACE, "createdOn");
		creator = factory.createIRI(FL.NAMESPACE, "creator");
		creatorParams = factory.createIRI(FL.NAMESPACE, "creatorParams");
		hasParentArtifact = factory.createIRI(FL.NAMESPACE, "hasParentArtifact");
		param = factory.createIRI(FL.NAMESPACE, "param");
		paramName = factory.createIRI(FL.NAMESPACE, "paramName");
		paramValue = factory.createIRI(FL.NAMESPACE, "paramValue");
		processedBy = factory.createIRI(FL.NAMESPACE, "processedBy");
		SavedQuery = factory.createIRI(FL.NAMESPACE, "SavedQuery");
		service = factory.createIRI(FL.NAMESPACE, "service");
		Tagger = factory.createIRI(FL.NAMESPACE, "Tagger");
	}

	private FL() {
		//static access only
	}

}
