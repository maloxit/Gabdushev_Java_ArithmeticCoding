package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.RC;

import java.util.Arrays;

abstract class AdaptiveArithmeticCoder {

    public static final int alphabetLen = 256;
    protected static final double weightsMin = 1;
    protected final double[] weights;
    protected final double[] splitPoints;
    protected final double normalWeightsMax;
    protected final double ceilingWeightsMax;
    protected final AutoDataBuffer out;

    /**
     *Abstract Coder, that implements adaptive functionality with overflow prevention
     * @param normalWeightsMax Normal weight value, to which the highest value will be dropped after reaching ceilingWeightsMax
     * @param ceilingWeightsMax Ceiling weight value of individual character. When some weight reaches it, all weights
     *                          are dropped down to be not greater than normalWeightsMax
     * @param out Buffered output object
     */
    protected AdaptiveArithmeticCoder(double normalWeightsMax, double ceilingWeightsMax, AutoDataBuffer out) {
        this.normalWeightsMax = normalWeightsMax;
        this.ceilingWeightsMax = ceilingWeightsMax;
        this.weights = new double[alphabetLen + 1];
        Arrays.fill(this.weights, weightsMin);
        splitPoints = new double[alphabetLen + 2];
        RecalculateSplitPoints();
        this.out = out;
    }

    /**
     * Processes given byte of input data
     * @param val next input byte
     * @return Return Code object, which contains information about reason of the end of work
     */
    public abstract RC PutByte(byte val);

    /**
     * Finish coding work
     * @return Return Code object, which contains information about reason of the end of work
     */
    public abstract RC Finish();

    /**
     *Recalculates split points fusing current weights array
     */
    protected void RecalculateSplitPoints() {
        double fullSum = 0;
        for (double weight : weights) {
            fullSum += weight;
        }
        double sum = 0;
        splitPoints[0] = 0;
        for (int i = 0; i < alphabetLen + 1; i++) {
            sum += weights[i];
            splitPoints[i + 1] = sum / fullSum;
        }
    }

    /**
     *Fixes all weights to be not greater than normalWeightsMax to prevent overflow
     */
    protected void FixWeights() {
        double multiplier = normalWeightsMax / ceilingWeightsMax;
        for (int i = 0; i < alphabetLen + 1; i++) {
            weights[i] = Double.max(weights[i] * multiplier, weightsMin);
        }
    }

    /**
     * Safely updates weight
     * @param index index of weight to update
     */
    protected void UpdateWeight(int index) {
        weights[index] += 1;
        if (weights[index] >= ceilingWeightsMax) {
            FixWeights();
        }
        RecalculateSplitPoints();
    }
}
