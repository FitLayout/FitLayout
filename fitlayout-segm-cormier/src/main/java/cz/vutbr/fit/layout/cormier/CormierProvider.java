package cz.vutbr.fit.layout.cormier;

import cz.vutbr.fit.layout.api.Parameter;
import cz.vutbr.fit.layout.api.ServiceException;
import cz.vutbr.fit.layout.cormier.impl.CormierSegmentation;
import cz.vutbr.fit.layout.cormier.impl.EdgeDetector;
import cz.vutbr.fit.layout.cormier.impl.LineDetector;
import cz.vutbr.fit.layout.impl.BaseArtifactService;
import cz.vutbr.fit.layout.impl.ParameterFloat;
import cz.vutbr.fit.layout.impl.ParameterInt;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Artifact;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.ontology.BOX;
import cz.vutbr.fit.layout.ontology.SEGM;
import org.eclipse.rdf4j.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provider of the Cormier web segmentation method implemented within {@link CormierSegmentation}.
 * @see <a href="https://uwspace.uwaterloo.ca/handle/10012/13523">Michael Cormier (2018). Computer Vision on Web Pages:
 * A Study of Man-Made Images. UWSpace.</a>
 * @author <a href="mailto:xmaste02@stud.fit.vutbr.cz">František Maštera</a>
 */
public class CormierProvider extends BaseArtifactService {

    private static final Logger logger = LoggerFactory.getLogger(CormierProvider.class);

    int halfWindowWidth = 45;
    float standardDeviation = 0.1f;
    float priorEdgeProbability = 0.01f;
    int pyramidLevels = 1;
    int maxLineLength = 256;
    float edgeProbabilityThreshold = 0.3f;
    int monteCarloTrials = 100;
    int minSegmentLength = 45;
    float signLineProbThreshold = 0.5f;

    @Override
    public String getId() {
        return "FitLayout.Cormier";
    }

    @Override
    public String getName() {
        return "Cormier et al.";
    }

    @Override
    public String getDescription() {
        return "Cormier: Purely visual segmentation algorithm";
    }

    /**
     * Parameters of the algorithm provided by this service. Their values can be set via {@link #setParam(String, Object)},
     * where name can be obtained from {@link #data}'s {@link Parameter#getName()}, or by using standard setter methods
     * such as {@link #setHalfWindowWidth(int)}.
     */
    public enum CormierParameter {
        HALF_WINDOW_WIDTH(new ParameterInt(
            "halfWindowWidth",
            "Controls the size of the neighborhood (in number of pixels from the center pixel). " +
                "Higher values dramatically impact performance.",
            0,
            1_000
        )),
        STANDARD_DEVIATION(new ParameterFloat(
            "standardDeviation",
            "Standard deviation for the normal distribution used in kernel density estimation for determining " +
                "the probability of an edge in each pixel.",
            0.0f,
            1_000.0f
        )),
        PRIOR_EDGE_PROBABILITY(new ParameterFloat(
            "priorEdgeProbability",
            "Prior probability of an edge in each pixel.",
            0.0f,
            1.0f
        )),
        PYRAMID_LEVELS(new ParameterInt(
            "pyramidLevels",
            "Number of levels for the Gaussian pyramid used for the multiscale edge detection. Set to 1 to " +
                "effectively turn off the multiscale edge detection.",
            1,
            10
        )),
        MAX_LINE_LENGTH(new ParameterInt(
            "maxLineLength",
            "Maximum length of a segmentation line (in pixels) before the algorithm splits it in half and " +
                "processes each half separately.",
            1,
            1_000
        )),
        EDGE_PROBABILITY_THRESHOLD(new ParameterFloat(
            "edgeProbabilityThreshold",
            "Minimum probability of an edge in each pixel required in order for it to be considered significant.",
            0.0f,
            1.0f
        )),
        MONTE_CARLO_TRIALS(new ParameterInt(
            "monteCarloTrials",
            "Number of Monte Carlo trials for determining the probability that line is semantically significant.",
            1,
            5_000
        )),
        MIN_SEGMENT_LENGTH(new ParameterInt(
            "minSegmentLength",
            "Minimum length of any side of each segment.",
            0,
            1_000
        )),
        SIGN_LINE_PROB_THRESHOLD(new ParameterFloat(
            "signLineProbThreshold",
            "Minimum probability that line is significant for it to be used for segmentation.",
            0.0f,
            1.0f
        ));

        /**
         * Data about the parameter used by FitLayout.
         */
        public final Parameter data;

        CormierParameter(Parameter data) {
            this.data = data;
        }
    }

    @Override
    public List<Parameter> defineParams() {
        return Arrays.stream(CormierParameter.values())
            .map(cormierParameter -> cormierParameter.data)
            .collect(Collectors.toList());
    }

    /**
     * @see EdgeDetector#getHalfWindowWidth()
     */
    public int getHalfWindowWidth() {
        return halfWindowWidth;
    }

    /**
     * @see EdgeDetector#setHalfWindowWidth(int)
     */
    public void setHalfWindowWidth(int halfWindowWidth) {
        this.halfWindowWidth = halfWindowWidth;
    }

    /**
     * @see EdgeDetector#getStandardDeviation()
     */
    public float getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * @see EdgeDetector#setStandardDeviation(float)
     */
    public void setStandardDeviation(float standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    /**
     * @see EdgeDetector#getPriorProbability()
     */
    public float getPriorEdgeProbability() {
        return priorEdgeProbability;
    }

    /**
     * @see EdgeDetector#setPriorProbability(float)
     */
    public void setPriorEdgeProbability(float priorEdgeProbability) {
        this.priorEdgeProbability = priorEdgeProbability;
    }

    /**
     * @see EdgeDetector#setPyramidLevels(int)
     */
    public int getPyramidLevels() {
        return pyramidLevels;
    }

    /**
     * @see EdgeDetector#setPyramidLevels(int)
     */
    public void setPyramidLevels(int pyramidLevels) {
        this.pyramidLevels = pyramidLevels;
    }

    /**
     * @see LineDetector#getMaxLineLength()
     */
    public int getMaxLineLength() {
        return maxLineLength;
    }

    /**
     * @see LineDetector#setMaxLineLength(int)
     */
    public void setMaxLineLength(int maxLineLength) {
        this.maxLineLength = maxLineLength;
    }

    /**
     * @see LineDetector#getEdgeProbabilityThreshold()
     */
    public float getEdgeProbabilityThreshold() {
        return edgeProbabilityThreshold;
    }

    /**
     * @see LineDetector#setEdgeProbabilityThreshold(float)
     */
    public void setEdgeProbabilityThreshold(float edgeProbabilityThreshold) {
        this.edgeProbabilityThreshold = edgeProbabilityThreshold;
    }

    /**
     * @see LineDetector#getMonteCarloTrials()
     */
    public int getMonteCarloTrials() {
        return monteCarloTrials;
    }

    /**
     * @see LineDetector#setMonteCarloTrials(int)
     */
    public void setMonteCarloTrials(int monteCarloTrials) {
        this.monteCarloTrials = monteCarloTrials;
    }

    /**
     * @return Minimum length of any side of each segment.
     */
    public int getMinSegmentLength() {
        return minSegmentLength;
    }

    /**
     * @param minSegmentLength Minimum length of any side of each segment.
     */
    public void setMinSegmentLength(int minSegmentLength) {
        this.minSegmentLength = minSegmentLength;
    }

    /**
     * @return Minimum probability that line is significant for it to be used for segmentation.
     */
    public float getSignLineProbThreshold() {
        return signLineProbThreshold;
    }

    /**
     * @param signLineProbThreshold Minimum probability that line is significant for it to be used for segmentation.
     */
    public void setSignLineProbThreshold(float signLineProbThreshold) {
        this.signLineProbThreshold = signLineProbThreshold;
    }

    @Override
    public IRI getConsumes() {
        return BOX.Page;
    }

    /**
     * X-Y tree represented via the {@link AreaTree} interface.
     */
    @Override
    public IRI getProduces() {
        return SEGM.AreaTree;
    }

    @Override
    public Artifact process(Artifact input) throws ServiceException {

        if (!(input instanceof Page)) {
            throw new ServiceException("Source artifact not specified or not a page.");
        }

        AreaTree atree;
        try {
            atree = createAreaTree((Page) input);
        } catch (IOException e) {
            throw new ServiceException("Failed to process the screenshot of the source page.", e);
        }

        IRI atreeIri = getServiceManager().getArtifactRepository().createArtifactIri(atree);
        atree.setIri(atreeIri);
        return atree;
    }

    /**
     * @param page Page on which the segmentation will be applied. Make sure it's possible to get a screenshot of it
     *             using {@link Page#getPngImage()}.
     * @return X-Y tree of segmentations created by the {@link CormierSegmentation} with this instance's
     * {@link CormierParameter}s' values.
     * @throws IOException If failed to process the screenshot of the given page.
     */
    public AreaTree createAreaTree(Page page) throws IOException {
        return createAreaTree(page.getPngImage(), page.getIri());
    }

    /**
     * @param imageData Screenshot of a page on which the segmentation will be applied.
     * @param iri IRI which will be assigned to the resulting {@link AreaTree}.
     * @return X-Y tree of segmentations created by the {@link CormierSegmentation} with this instance's
     * {@link CormierParameter}s' values.
     * @throws IOException If failed to process the provided image data.
     */
    public AreaTree createAreaTree(byte[] imageData, IRI iri) throws IOException {

        for (CormierParameter param : CormierParameter.values()) {
            logger.debug("Parameter {} = {}", param.data.getName(), getParam(param.data.getName()));
        }

        return new CormierSegmentation(
            new EdgeDetector(
                (int) getParam(CormierParameter.HALF_WINDOW_WIDTH.data.getName()),
                (float) getParam(CormierParameter.STANDARD_DEVIATION.data.getName()),
                (float) getParam(CormierParameter.PRIOR_EDGE_PROBABILITY.data.getName()),
                (int) getParam(CormierParameter.PYRAMID_LEVELS.data.getName())
            ),
            new LineDetector(
                (int) getParam(CormierParameter.MAX_LINE_LENGTH.data.getName()),
                (float) getParam(CormierParameter.EDGE_PROBABILITY_THRESHOLD.data.getName()),
                (int) getParam(CormierParameter.MONTE_CARLO_TRIALS.data.getName())
            ),
            (int) getParam(CormierParameter.MIN_SEGMENT_LENGTH.data.getName()),
            (float) getParam(CormierParameter.SIGN_LINE_PROB_THRESHOLD.data.getName())
        ).run(imageData, iri);
    }
}
