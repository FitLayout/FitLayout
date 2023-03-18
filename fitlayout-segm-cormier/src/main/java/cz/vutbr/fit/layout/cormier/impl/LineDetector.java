package cz.vutbr.fit.layout.cormier.impl;

import org.opencv.core.Mat;

import java.util.Arrays;
import java.util.Random;

/**
 * Detector for semantically significant lines for the {@link CormierSegmentation}.
 * @see <a href="https://uwspace.uwaterloo.ca/handle/10012/13523">Michael Cormier (2018). Computer Vision on Web Pages:
 * A Study of Man-Made Images. UWSpace.</a>
 */
public class LineDetector {

    private int maxLineLength;
    private float edgeProbabilityThreshold;
    private int monteCarloTrials;

    /**
     * @param maxLineLength Maximum length of a segmentation line (in pixels) before the algorithm splits it in half
     *                      and processes each half separately.
     * @param edgeProbabilityThreshold Minimum probability of an edge in each pixel (obtained from
     * {@link EdgeDetector#getEdgeProbabilities(Mat)}) required in order to be considered significant.
     * @param monteCarloTrials Number of Monte Carlo trials for determining the probability that line is semantically
     *                         significant.
     */
    public LineDetector(int maxLineLength, float edgeProbabilityThreshold, int monteCarloTrials) {
        this.maxLineLength = maxLineLength;
        this.edgeProbabilityThreshold = edgeProbabilityThreshold;
        this.monteCarloTrials = monteCarloTrials;
    }

    /**
     * @return Maximum length of a segmentation line (in pixels) before the algorithm splits it in half and processes
     * each half separately.
     */
    public int getMaxLineLength() {
        return maxLineLength;
    }

    /**
     * @param maxLineLength Maximum length of a segmentation line (in pixels) before the algorithm splits it in half
     *                         and processes each half separately.
     */
    public void setMaxLineLength(int maxLineLength) {
        this.maxLineLength = maxLineLength;
    }

    /**
     * @return Minimum probability of an edge in each pixel (obtained from {@link EdgeDetector#getEdgeProbabilities(Mat)})
     * required in order to be considered significant.
     */
    public float getEdgeProbabilityThreshold() {
        return edgeProbabilityThreshold;
    }

    /**
     * @param edgeProbabilityThreshold Minimum probability of an edge in each pixel (obtained from
     * {@link EdgeDetector#getEdgeProbabilities(Mat)}) required in order to be considered significant.
     */
    public void setEdgeProbabilityThreshold(float edgeProbabilityThreshold) {
        this.edgeProbabilityThreshold = edgeProbabilityThreshold;
    }

    /**
     * @return Number of Monte Carlo trials for determining the probability that line is semantically significant.
     */
    public int getMonteCarloTrials() {
        return monteCarloTrials;
    }

    /**
     * @param monteCarloTrials Number of Monte Carlo trials for determining the probability that line is semantically
     *                         significant.
     */
    public void setMonteCarloTrials(int monteCarloTrials) {
        this.monteCarloTrials = monteCarloTrials;
    }

    /**
     * @param edgeProbabilities Edge probabilities obtained from {@link EdgeDetector#getEdgeProbabilities(Mat)} for each
     *                          pixel in the line to be checked in original order (i.e. not sorted).
     * @return Probability that the line made of pixels with given edge probabilities is semantically significant for
     *         segmentation.
     */
    public float lineProbability(double[] edgeProbabilities) {

        int length = edgeProbabilities.length;

        if (length > getMaxLineLength()) {
            // If too long, the result probability is probability that both halves are semantically significant.
            return lineProbability(Arrays.copyOfRange(edgeProbabilities, 0, length/2))
                * lineProbability(Arrays.copyOfRange(edgeProbabilities, length/2 + 1, length - 1));

        } else { // Otherwise, calculate the probability using a Monte Carlo simulation.

            Random rng = new Random();
            int successCount = 0; // Number of MC trials that succeeded.
            for (int i = 0; i < getMonteCarloTrials(); i++) {

                // The higher probability of locally significant edge at the pixel, the higher the chance of considering it significant.
                long significantPixels = Arrays.stream(edgeProbabilities)
                    .filter(prob -> prob > rng.nextFloat())
                    .count();

                // MC trial is successful if enough significant pixels were rolled within the line.
                if ((float) significantPixels / length >= getEdgeProbabilityThreshold()) {
                    successCount++;
                }
            }

            return (float) successCount / getMonteCarloTrials(); // Probability ~ rate of the successful MC trials.
        }
    }
}
