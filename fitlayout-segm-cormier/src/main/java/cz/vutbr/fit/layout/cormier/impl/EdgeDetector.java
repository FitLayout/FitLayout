package cz.vutbr.fit.layout.cormier.impl;

import cz.vutbr.fit.layout.model.Rectangular;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.javatuples.Pair;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Detector for locally significant edges for the {@link CormierSegmentation}.
 * @see <a href="https://uwspace.uwaterloo.ca/handle/10012/13523">Michael Cormier (2018). Computer Vision on Web Pages:
 * A Study of Man-Made Images. UWSpace.</a>
 * @author <a href="mailto:xmaste02@stud.fit.vutbr.cz">František Maštera</a>
 */
public class EdgeDetector {

    static {
        // Initialize the OpenCV library.
        nu.pattern.OpenCV.loadLocally();
    }

    private static final Logger logger = LoggerFactory.getLogger(EdgeDetector.class);

    private int halfWindowWidth;
    private float standardDeviation;
    private float priorProbability;
    private int pyramidLevels;

    private boolean ndPregenEnabled = true;
    private boolean parallelEnabled = true;

    /**
     * @param halfWindowWidth Range of the neighborhood (around the pixel) taken into account when applying kernel
     *                        density estimation using a Gaussian kernel.
     * @param standardDeviation Standard deviation used when applying kernel density estimation using a Gaussian kernel.
     * @param priorProbability Prior probability that a given pixel is an edge.
     * @param pyramidLevels Number of levels for the Gaussian pyramid used for the multiscale edge detection. Set to 1 to
     *                      effectively turn off the multiscale edge detection
     */
    public EdgeDetector(int halfWindowWidth, float standardDeviation, float priorProbability, int pyramidLevels) {
        this.halfWindowWidth = halfWindowWidth;
        this.standardDeviation = standardDeviation;
        this.priorProbability = priorProbability;
        this.pyramidLevels = pyramidLevels;
    }

    /**
     * @param ndPregenEnabled If the {@link #edgeProbabilities(Mat, Mat)} optimization by pre-generating and storing
     *                        the {@link NormalDistribution} instances will be enabled. Disabling this will reduce the
     *                        memory requirements significantly, but also slows down the process.
     * @param parallelEnabled If the {@link #edgeProbabilities(Mat, Mat)} parallelization will be enabled. Disabling
     *                        this will cause significant slow down on multithreaded systems.
     * @see #EdgeDetector(int, float, float, int)
     */
    public EdgeDetector(int halfWindowWidth, float standardDeviation, float priorProbability, int pyramidLevels,
                        boolean ndPregenEnabled, boolean parallelEnabled) {
        this.halfWindowWidth = halfWindowWidth;
        this.standardDeviation = standardDeviation;
        this.priorProbability = priorProbability;
        this.pyramidLevels = pyramidLevels;
        this.ndPregenEnabled = ndPregenEnabled;
        this.parallelEnabled = parallelEnabled;
    }

    /**
     * @return Range of the neighborhood (around the pixel) taken into account when applying kernel density estimation
     * using a Gaussian kernel.
     */
    public int getHalfWindowWidth() {
        return halfWindowWidth;
    }

    /**
     * @param halfWindowWidth Range of the neighborhood (around the pixel) taken into account when applying kernel
     *                        density estimation using a Gaussian kernel.
     */
    public void setHalfWindowWidth(int halfWindowWidth) {
        this.halfWindowWidth = halfWindowWidth;
    }

    /**
     * @return Standard deviation used when applying kernel density estimation using a Gaussian kernel.
     */
    public float getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * @param standardDeviation Standard deviation used when applying kernerl density estimation using a Gaussian kernel.
     */
    public void setStandardDeviation(float standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    /**
     * @return Prior probability that a given pixel is an edge.
     */
    public float getPriorProbability() {
        return priorProbability;
    }

    /**
     * @param priorProbability Prior probability that a given pixel is an edge.
     */
    public void setPriorProbability(float priorProbability) {
        this.priorProbability = priorProbability;
    }

    /**
     * @return Number of levels for the Gaussian pyramid used for the multiscale edge detection. Set to 1 to effectively
     * turn off the multiscale edge detection
     */
    public int getPyramidLevels() {
        return pyramidLevels;
    }

    /**
     * @param pyramidLevels Number of levels for the Gaussian pyramid used for the multiscale edge detection. Set to 1 to
     *                      effectively turn off the multiscale edge detection
     */
    public void setPyramidLevels(int pyramidLevels) {
        this.pyramidLevels = pyramidLevels;
    }

    /**
     * @return Probabilities of a horizontal/vertical edge on each pixel in the given image.
     */
    public Pair<double[][], double[][]> getEdgeProbabilities(byte[] imageData) {
        return getEdgeProbabilities(Imgcodecs.imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR));
    }

    /**
     * @return Probabilities of a horizontal/vertical edge on each pixel in the given image.
     */
    public Pair<double[][], double[][]> getEdgeProbabilities(Mat image) {
        Mat greyscaleImage = toGreyscale(image);
        Pair<Mat, Mat> sobel = multiscaleSobel(greyscaleImage);

        return edgeProbabilities(sobel.getValue0(), sobel.getValue1());
    }

    /**
     * @return The given image turned to greyscale.
     */
    private static Mat toGreyscale(Mat image) {
        Mat greyscaleImage = new Mat();
        Imgproc.cvtColor(image, greyscaleImage, Imgproc.COLOR_RGB2GRAY);
        return greyscaleImage;
    }

    /**
     * @return Horzontal and vertical edges for the given image, calculated using
     * {@link Imgproc#Sobel(Mat, Mat, int, int, int)}. The dimensions of both horizontal and vertical edges are the same
     * as the original image.
     */
    private static Pair<Mat, Mat> sobel(Mat greyscaleImage) {

        Mat hEdges = new Mat();
        Mat vEdges = new Mat();

        Imgproc.Sobel(greyscaleImage, hEdges, CvType.CV_16S, 0, 1, 3);
        Imgproc.Sobel(greyscaleImage, vEdges, CvType.CV_16S, 1, 0, 3);

        return new Pair<>(hEdges, vEdges);
    }

    /**
     * @return Horzontal and vertical edges for the given image, calculated using multiscale
     * {@link Imgproc#Sobel(Mat, Mat, int, int, int)} edge detection with a Gaussian pyramid with
     * {@link #getPyramidLevels()} levels. Calling {@link #sobel(Mat)} returns the same result as calling this method when
     * {@link #getPyramidLevels()} is equal to 1. The dimensions of both horizontal and vertical edges are the same as the
     * original image.
     */
    public Pair<Mat, Mat> multiscaleSobel(Mat greyscaleImage) {

        if (getPyramidLevels() == 1) return sobel(greyscaleImage); // Optimization.

        Mat[] pyramid = new Mat[getPyramidLevels()];

        Mat hEdges = Mat.zeros(greyscaleImage.size(), CvType.CV_32F);
        Mat vEdges = Mat.zeros(greyscaleImage.size(), CvType.CV_32F);

        // Perform Sobel on each level of Gaussian pyramid and add the results in quadrature.
        for (int i = 0; i < getPyramidLevels(); i++) {

            // Gaussian pyramid construction.
            if (i > 0) {
                Mat currentLevel = new Mat();
                Imgproc.pyrDown(pyramid[i - 1], currentLevel);
                pyramid[i] = currentLevel;
            } else {
                pyramid[i] = greyscaleImage.clone();
            }

            // Sobel edge detection on this level.
            Pair<Mat, Mat> edges = sobel(pyramid[i]);
            edges.getValue0().convertTo(edges.getValue0(), CvType.CV_32F);
            edges.getValue1().convertTo(edges.getValue1(), CvType.CV_32F);
            Core.multiply(edges.getValue0(), edges.getValue0(), edges.getValue0());
            Core.multiply(edges.getValue1(), edges.getValue1(), edges.getValue1());

            if (i > 0) { // Upsale back, so it can be added to the total sum.
                Imgproc.resize(edges.getValue0(), edges.getValue0(), greyscaleImage.size());
                Imgproc.resize(edges.getValue1(), edges.getValue1(), greyscaleImage.size());
            }

            Core.add(hEdges, edges.getValue0(), hEdges);
            Core.add(vEdges, edges.getValue1(), vEdges);
        }

        Core.sqrt(hEdges, hEdges);
        Core.sqrt(vEdges, vEdges);

        return new Pair<>(hEdges, vEdges);
    }

    /**
     * @return Probabilities of a horizontal/vertical edge on each pixel considering the given edge horizontal/vertical
     * edge detection results.
     */
    private Pair<double[][], double[][]> edgeProbabilities(Mat hEdges, Mat vEdges) {

        long totalStartTime = System.currentTimeMillis();
        int height = hEdges.rows();
        int width = hEdges.cols();
        double[][] hEdgeProbability = new double[height][width];
        double[][] vEdgeProbability = new double[height][width];

        logger.debug("Culculating edge probabilities in each pixel in an image made of {} pixels ({}x{}),",
            width * height, width, height);

        Core.convertScaleAbs(hEdges, hEdges);
        Core.convertScaleAbs(vEdges, vEdges);

        NormalDistribution[][] hNormalDistributions = new NormalDistribution[height][width];
        NormalDistribution[][] vNormalDistributions = new NormalDistribution[height][width];
        if (ndPregenEnabled) {
            logger.debug("Pre-generating normal distributions...");
            for (int row = 0; row < height; row++) { // y of the whole image
                for (int col = 0; col < width; col++) { // x
                    hNormalDistributions[row][col] = new NormalDistribution(null, hEdges.get(row, col)[0], getStandardDeviation());
                    vNormalDistributions[row][col] = new NormalDistribution(null, vEdges.get(row, col)[0], getStandardDeviation());
                }
            }
        }

        logger.debug("Calculating probabilities...");
        long startTime = System.currentTimeMillis();
        AtomicInteger rowsDoneA = new AtomicInteger(0);

        IntStream rowRange = IntStream.range(0, height);
        if (parallelEnabled) rowRange = rowRange.parallel();
        rowRange.forEach(row -> { // y of the whole image

            for (int col = 0; col < width; col++) { // x

                // Separate neighborhoods for each side, excluding line with the current pixel.
                Rectangular topNeighborhood = new Rectangular(
                    Math.max(col - getHalfWindowWidth(), 0),         // X1 (left)
                    Math.max(row - getHalfWindowWidth(), 0),         // Y1 (top)
                    Math.min(col + getHalfWindowWidth() + 1, width), // X2 (right)
                    row                                              // Y2 (bottom)
                );
                Rectangular bottomNeighborhood = new Rectangular(
                    Math.max(col - getHalfWindowWidth(), 0),
                    row + 1,
                    Math.min(col + getHalfWindowWidth() + 1, width),
                    Math.min(row + getHalfWindowWidth() + 1, height)
                );
                Rectangular leftNeighborhood = new Rectangular(
                    Math.max(col - getHalfWindowWidth(), 0),
                    Math.max(row - getHalfWindowWidth(), 0),
                    col,
                    Math.min(row + getHalfWindowWidth() + 1, height)
                );
                Rectangular rightNeighborhood = new Rectangular(
                    col + 1,
                    Math.max(row - getHalfWindowWidth(), 0),
                    Math.min(col + getHalfWindowWidth() + 1, width),
                    Math.min(row + getHalfWindowWidth() + 1, height)
                );

                // Calculate the edge probabilities with given edge strength for each neighborhood.
                if (ndPregenEnabled) {
                    hEdgeProbability[row][col] = edgeProbability(
                        relativeEdgeStrength(hEdges.get(row, col)[0], topNeighborhood, hNormalDistributions),
                        relativeEdgeStrength(hEdges.get(row, col)[0], bottomNeighborhood, hNormalDistributions)
                    );
                    vEdgeProbability[row][col] = edgeProbability(
                        relativeEdgeStrength(vEdges.get(row, col)[0], leftNeighborhood, vNormalDistributions),
                        relativeEdgeStrength(vEdges.get(row, col)[0], rightNeighborhood, vNormalDistributions)
                    );
                } else {
                    hEdgeProbability[row][col] = edgeProbability(
                        relativeEdgeStrength(hEdges.get(row, col)[0], topNeighborhood, hEdges),
                        relativeEdgeStrength(hEdges.get(row, col)[0], bottomNeighborhood, hEdges)
                    );
                    vEdgeProbability[row][col] = edgeProbability(
                        relativeEdgeStrength(vEdges.get(row, col)[0], leftNeighborhood, vEdges),
                        relativeEdgeStrength(vEdges.get(row, col)[0], rightNeighborhood, vEdges)
                    );
                }
            }

            // Performance logging.
            int rowsDone = rowsDoneA.incrementAndGet();
            if (rowsDone % (height / 100) == 0) {
                long totalTime = System.currentTimeMillis() - startTime;
                double avgTimePerRow = (double) totalTime / rowsDone;
                double timeRemaining = (height - rowsDone) * avgTimePerRow;
                logger.debug("{}% of the image processed ({}/{} rows), total time: {} s, estimated time remaining: {} s",
                    String.format("%.0f", Math.ceil(((double) rowsDone / height) * 100)),
                    rowsDone, height,
                    String.format("%.2f", totalTime / 1e3),
                    String.format("%.2f", timeRemaining / 1e3)
                );
            }
        });

        logger.debug("Edge probablities calculated in {} seconds.",
            String.format("%.2f", (System.currentTimeMillis() - totalStartTime) / 1e3));
        return new Pair<>(hEdgeProbability, vEdgeProbability);
    }

    /**
     * @return Edge strength at the target pixel relative to its neighborhood (i.e. how the edge stands out from the
     * neighborhood), determined using the kernel density estimation (using the given normal distributions, which are
     * indexed using the given neighborhood).
     */
    private double relativeEdgeStrength(double targetPixelValue, Rectangular neighborhood, Mat edges) {

        double sum = 0.5; // Include the target pixel which has always cumulative probability 0.5.

        for (int row = neighborhood.getY1(); row < neighborhood.getY2(); row++) { // y
            for (int col = neighborhood.getX1(); col < neighborhood.getX2(); col++) { // x
                sum += new NormalDistribution(null,
                    edges.get(row, col)[0], getStandardDeviation()).cumulativeProbability(targetPixelValue); // At the target pixel.
            }
        }

        // +1 for the target pixel outside the neighborhood.
        return 1 - sum / ((neighborhood.getX2() - neighborhood.getX1())
            * (neighborhood.getY2() - neighborhood.getY1()) + 1);
    }

    /**
     * @return Edge strength at the target pixel relative to its neighborhood (i.e. how the edge stands out from the
     * neighborhood), determined using the kernel density estimation (using the given normal distributions, which are
     * indexed using the given neighborhood).
     */
    private double relativeEdgeStrength(double targetPixelValue, Rectangular neighborhood,
                                        NormalDistribution[][] normalDistributions) {

        double sum = 0.5; // Include the target pixel which has always cumulative probability 0.5.

        for (int row = neighborhood.getY1(); row < neighborhood.getY2(); row++) { // y
            for (int col = neighborhood.getX1(); col < neighborhood.getX2(); col++) { // x
                sum += normalDistributions[row][col].cumulativeProbability(targetPixelValue); // At the target pixel.
            }
        }

        // +1 for the target pixel outside the neighborhood.
        return 1 - sum / ((neighborhood.getX2() - neighborhood.getX1())
            * (neighborhood.getY2() - neighborhood.getY1()) + 1);
    }

    /**
     * @param edgeStrength Edge strength relative to the first side of the pixel's neighborhood.
     * @param edgeStrength2 Edge strength relative to the other side of the pixel's neighborhood.
     * @return Probability of an edge on the pixel with given edge strength.
     */
    private double edgeProbability(double edgeStrength, double edgeStrength2) {

        double edgeProbability =
            (getPriorProbability() * (1 + edgeStrength - getPriorProbability() * edgeStrength)
                / (getPriorProbability() + edgeStrength - getPriorProbability() * edgeStrength));
        double edgeProbability2 =
            (getPriorProbability() * (1 + edgeStrength2 - getPriorProbability() * edgeStrength2)
                / (getPriorProbability() + edgeStrength2 - getPriorProbability() * edgeStrength2));

        // Probability of an edge on either of the sides of the neighborhood.
        return edgeProbability + edgeProbability2 - edgeProbability * edgeProbability2;
    }
}
