package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.RC;

import java.util.Arrays;

abstract class ArithmeticCodingProcessor {

    public static final int alphabetLen = 256;
    protected static final double weightsMin = 1;
    protected final double[] weights;
    protected final double[] splitPoints;
    protected final double normalWeightsMax;
    protected final double ceilingWeightsMax;
    protected final AutoDataBuffer out;

    protected ArithmeticCodingProcessor(double normalWeightsMax, double ceilingWeightsMax, AutoDataBuffer out) {
        this.normalWeightsMax = normalWeightsMax;
        this.ceilingWeightsMax = ceilingWeightsMax;
        this.weights = new double[alphabetLen + 1];
        Arrays.fill(this.weights, weightsMin);
        splitPoints = new double[alphabetLen + 2];
        RecalculateSplitPoints();
        this.out = out;
    }

    public abstract RC PutByte(byte val);

    public abstract RC Finish();

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

    protected void FixWeights() {
        double multiplier = normalWeightsMax / ceilingWeightsMax;
        for (int i = 0; i < alphabetLen + 1; i++) {
            weights[i] = Double.max(weights[i] * multiplier, weightsMin);
        }
    }

    protected void UpdateWeight(int index) {
        weights[index] += 1;
        if (weights[index] >= ceilingWeightsMax) {
            FixWeights();
        }
        RecalculateSplitPoints();
    }
}
