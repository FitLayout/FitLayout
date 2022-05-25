package cz.vutbr.fit.layout.ontology;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Visual area to entity mapping ontology.
 * <p>
 * An ontology for mapping visual areas to ontological instances and
 * properties..
 * <p>
 * Namespace MAPPING.
 * Prefix: {@code <http://fitlayout.github.io/ontology/mapping.owl>}
 */
public class MAPPING {

	/** {@code http://fitlayout.github.io/ontology/mapping.owl} **/
	public static final String NAMESPACE = "http://fitlayout.github.io/ontology/mapping.owl";

	/** {@code mapping} **/
	public static final String PREFIX = "mapping";

	/**
	 * {@code http://fitlayout.github.io/ontology/mapping.owl#describesInstance}.
	 * <p>
	 * Assigns an individual to the visual area that the area describes.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/mapping.owl#describesInstance">#describesInstance</a>
	 */
	public static final IRI describesInstance;

	/**
	 * {@code http://fitlayout.github.io/ontology/mapping.owl#isValueOf}.
	 * <p>
	 * Assigns an ontological property to the area whose value the area
	 * represents.
	 *
	 * @see <a href="http://fitlayout.github.io/ontology/mapping.owl#isValueOf">#isValueOf</a>
	 */
	public static final IRI isValueOf;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();

		describesInstance = factory.createIRI(MAPPING.NAMESPACE, "#describesInstance");
		isValueOf = factory.createIRI(MAPPING.NAMESPACE, "#isValueOf");
	}

	private MAPPING() {
		//static access only
	}

}
