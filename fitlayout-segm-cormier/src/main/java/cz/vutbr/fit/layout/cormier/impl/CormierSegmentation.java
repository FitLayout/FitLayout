package cz.vutbr.fit.layout.cormier.impl;

import cz.vutbr.fit.layout.impl.DefaultArea;
import cz.vutbr.fit.layout.impl.DefaultAreaTree;
import cz.vutbr.fit.layout.model.Area;
import cz.vutbr.fit.layout.model.AreaTree;
import cz.vutbr.fit.layout.model.Page;
import cz.vutbr.fit.layout.model.Rectangular;
import org.eclipse.rdf4j.model.IRI;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Implementation of the Cormier web segmentation method.
 * @see <a href="https://uwspace.uwaterloo.ca/handle/10012/13523">Michael Cormier (2018). Computer Vision on Web Pages:
 * A Study of Man-Made Images. UWSpace.</a>
 * @author <a href="mailto:xmaste02@stud.fit.vutbr.cz">František Maštera</a>
 */
public class CormierSegmentation {

    private static final Logger logger = LoggerFactory.getLogger(CormierSegmentation.class);

    enum SegmDirection {
        HORIZONTAL,
        VERTICAL
    }

    private EdgeDetector edgeDetector;
    private LineDetector lineDetector;
    private int minSegmentLength;
    private float signLineProbThreshold;

    private boolean parallelEnabled = true;

    /**
     * @param edgeDetector Edge detector instance used for getting probabilities of locally significant edges.
     * @param lineDetector Line detector instance used for gettine probabilities of semantically significant lines.
     * @param minSegmentLength Minimum length of any side of each segment.
     * @param signLineProbThreshold Minimum probability that line is significant for it to be used for segmentation.
     */
    public CormierSegmentation(EdgeDetector edgeDetector, LineDetector lineDetector, int minSegmentLength,
                               float signLineProbThreshold) {
        this.edgeDetector = edgeDetector;
        this.lineDetector = lineDetector;
        this.minSegmentLength = minSegmentLength;
        this.signLineProbThreshold = signLineProbThreshold;
    }

    /**
     * @param parallelEnabled If the {@link #segment(Area, Pair, Pair)} parallelization will be enabled. Disabling
     *                        this will cause significant slow down on multithreaded systems.
     * @see #CormierSegmentation(EdgeDetector, LineDetector, int, float)
     */
    public CormierSegmentation(EdgeDetector edgeDetector, LineDetector lineDetector, int minSegmentLength,
                               float signLineProbThreshold, boolean parallelEnabled) {
        this.edgeDetector = edgeDetector;
        this.lineDetector = lineDetector;
        this.minSegmentLength = minSegmentLength;
        this.signLineProbThreshold = signLineProbThreshold;
        this.parallelEnabled = parallelEnabled;
    }

    /**
     * @return Edge detector instance used for getting probabilities of locally significant edges.
     */
    public EdgeDetector getEdgeDetector() {
        return edgeDetector;
    }

    /**
     * @param edgeDetector Edge detector instance used for getting probabilities of locally significant edges.
     */
    public void setEdgeDetector(EdgeDetector edgeDetector) {
        this.edgeDetector = edgeDetector;
    }

    /**
     * @return Line detector instance used for gettine probabilities of semantically significant lines.
     */
    public LineDetector getLineDetector() {
        return lineDetector;
    }

    /**
     * @param lineDetector Line detector instance used for gettine probabilities of semantically significant lines.
     */
    public void setLineDetector(LineDetector lineDetector) {
        this.lineDetector = lineDetector;
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

    /**
     * @param page Target page which will be segmented. Make sure it's possible to get a screenshot of it using
     *             {@link Page#getPngImage()}.
     * @return X-Y tree of rectangular, non-overlapping segments, where each node's children fully cover the parent
     * segment's area, with root node covering the whole image and leaf nodes being the most granular segments.
     * @throws IOException If failed to process the screenshot of the given page.
     */
    public AreaTree run(Page page) throws IOException {
        return run(page.getPngImage(), page.getIri());
    }

    /**
     * @param imageData Screenshot of the page which will be segmented.
     * @param iri IRI which will be assigned to the resulting {@link AreaTree}.
     * @return X-Y tree of rectangular, non-overlapping segments, where each node's children fully cover the parent
     * segment's area, with root node covering the whole image and leaf nodes being the most granular segments.
     * @throws IOException If failed to process the provided image data.
     */
    public AreaTree run(byte[] imageData, IRI iri) throws IOException {

        ByteArrayInputStream imageStream = new ByteArrayInputStream(imageData);
        BufferedImage image = ImageIO.read(imageStream); // To get image dimensions.

        var edgeProbs = getEdgeDetector().getEdgeProbabilities(imageData);
        DefaultArea root = new DefaultArea(new Rectangular(0, 0, image.getWidth(), image.getHeight()));
        DefaultAreaTree atree = new DefaultAreaTree(iri);
        atree.setParentIri(iri);

        long startTime = System.currentTimeMillis();
        logger.debug("Creating segmentation tree...");
        atree.setRoot(segment(root, split(root, edgeProbs), edgeProbs));
        logger.debug("Segmentation tree created in {} seconds.",
            String.format("%.2f", (System.currentTimeMillis() - startTime) / 1e3));

        return atree;
    }

    private static final ForkJoinPool segmentPool = new ForkJoinPool();

    /**
     * @param rootSegment Segment which will be divided into more segments if possible.
     * @param edgeProbabilities Probabilities of locally significant edges in the whole image.
     * @param initialSplit Result of the {@link #split(Area, Pair)} called on the root segment (optimization).
     * @return X-Y tree of rectangular, non-overlapping segments, where each node's children fully cover the parent
     * segment's area, with root node being the given segment and leaf nodes being the most granular ones.
     */
    private Area segment(Area rootSegment, Pair<List<Area>, SegmDirection> initialSplit,
                         Pair<double[][], double[][]> edgeProbabilities) {
        return segmentPool.invoke(new SegmentTask(rootSegment, initialSplit, edgeProbabilities));
    }

    private class SegmentTask extends RecursiveTask<Area> {

        private final Area rootSegment;
        private final Pair<List<Area>, SegmDirection> initialSplit;
        private final Pair<double[][], double[][]> edgeProbabilities;

        public SegmentTask(Area rootSegment, Pair<List<Area>, SegmDirection> initialSplit,
                           Pair<double[][], double[][]> edgeProbabilities) {
            this.rootSegment = rootSegment;
            this.initialSplit = initialSplit;
            this.edgeProbabilities = edgeProbabilities;
        }

        @Override
        protected Area compute() {
            logger.debug("Segmenting node [{},{}];[{},{}] ({}x{} px) at depth level {}.",
                rootSegment.getX1(), rootSegment.getY1(), rootSegment.getX2(), rootSegment.getY2(),
                rootSegment.getX2() - rootSegment.getX1(), rootSegment.getY2() - rootSegment.getY1(),
                Utils.getDepth(rootSegment));

            // Initial split to determine the segmentation direction on this level.
            if (initialSplit == null) { // Unable to split this segment any further.
                logger.debug("Couldn't segment any further (leaf node).");
                return rootSegment;
            }
            List<Area> toSplit = initialSplit.getValue0();
            SegmDirection direction = initialSplit.getValue1();

            // Create as many splits in the same direction as possible.
            ListIterator<Area> it = toSplit.listIterator();
            List<SegmentTask> childrenSegm = new ArrayList<>();
            while (it.hasNext()) {
                Area curr = it.next();

                // More splitting.
                Pair<List<Area>, SegmDirection> split = split(curr, edgeProbabilities);

                if (split == null || split.getValue1() != direction) { // Unable to split in the same direction => child.
                    rootSegment.appendChild(curr);
                    childrenSegm.add(new SegmentTask(curr, split, edgeProbabilities));
                } else { // Same direction => queue for more splits.
                    split.getValue0().forEach(it::add); // Insert (2) segments to the queue.
                    for (Area ignored : split.getValue0()) {
                        it.previous(); // Move backwards to iterate over the (2) inserted segments.
                    }
                }
            }

            if (parallelEnabled) {
                invokeAll(childrenSegm);
            } else {
                for (SegmentTask task : childrenSegm) task.invoke();
            }

            return rootSegment;
        }
    }

    /**
     * @param segment Segment which will be divided into 2 segments if possible.
     * @param edgeProbabilities Probabilities of locally significant horizontal/vertical edges in the whole image.
     * @return <ol>
     *   <li>Resulting segments created by dividing the given segment.</li>
     *   <li>Direction of the division done by the method.</li>
     * </ol>
     * Or null, if the area couldn't be divided.
     */
    private Pair<List<Area>, SegmDirection> split(Area segment, Pair<double[][], double[][]> edgeProbabilities) {

        double maxRowProb = 0.0;
        double maxColProb = 0.0;
        int rowWithMaxProb = 0;
        int colWithMaxProb = 0;

        // Calculate probabilities for possible segmentation lines, store the highest ones.
        for (int row = segment.getY1() + getMinSegmentLength(); row < segment.getY2() - getMinSegmentLength(); row++) {
            double rowProb = getLineDetector().lineProbability(
                Arrays.copyOfRange(edgeProbabilities.getValue0()[row], segment.getX1(), segment.getX2())
            );
            if (rowProb > maxRowProb) {
                maxRowProb = rowProb;
                rowWithMaxProb = row;
                if (rowProb == 1) break; // Optimization.
            }
        }

        for (int col = segment.getX1() + getMinSegmentLength(); col < segment.getX2() - getMinSegmentLength(); col++) {
            final int colF = col;
            double colProb = getLineDetector().lineProbability(
                Arrays.stream(Arrays.copyOfRange(edgeProbabilities.getValue1(), segment.getY1(), segment.getY2()))
                    .mapToDouble(row -> row[colF]).toArray()
            );
            if (colProb > maxColProb) {
                maxColProb = colProb;
                colWithMaxProb = col;
                if (colProb == 1) break; // Optimization.
            }
        }

        // Pick a segmentation line if possible.
        if (maxRowProb > getSignLineProbThreshold() || maxColProb > getSignLineProbThreshold()) {

            Rectangular first = new Rectangular(segment.getBounds());
            Rectangular second = new Rectangular(segment.getBounds());
            SegmDirection direction;

            if (maxRowProb > maxColProb) {
                // Horizontal segmentation.
                direction = SegmDirection.HORIZONTAL;
                first.setY2(rowWithMaxProb);
                second.setY1(rowWithMaxProb + 1);
            } else {
                // Vertical segmentation.
                direction = SegmDirection.VERTICAL;
                first.setX2(colWithMaxProb);
                second.setX1(colWithMaxProb + 1);
            }

            return new Pair<>(
                new ArrayList<>() {{
                    add(new DefaultArea(first));
                    add(new DefaultArea(second));
                }},
                direction
            );

        } else {
            return null;
        }
    }
}
