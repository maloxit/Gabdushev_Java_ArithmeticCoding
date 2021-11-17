package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.RC;

class ArithmeticDecoder extends ArithmeticCodingProcessor {

    private boolean hasReachedEnd;
    private double workingLow;
    private double workingHigh;
    private double possibleLow;
    private double possibleHigh;
    int lowSplitPointIndex;
    int highSplitPointIndex;

    public ArithmeticDecoder(double normalWeightsMax, double ceilingWeightsMax, AutoDataBuffer out) {
        super(normalWeightsMax, ceilingWeightsMax, out);
        hasReachedEnd = false;
        workingLow = 0;
        workingHigh = 1;
        possibleLow = 0;
        possibleHigh = 1;
        lowSplitPointIndex = -1;
        highSplitPointIndex = splitPoints.length;
    }

    @Override
    public RC PutByte(byte val) {
        if (hasReachedEnd)
            return RC.RC_SUCCESS;
        RC rc = RC.RC_SUCCESS;
        for (int i = 7; i >= 0 && rc.isSuccess(); i--) {
            int bit = (val >> i) & 0x01;
            ProcessNextBit(bit);
            rc = TryOutputByte();
        }
        return rc;
    }

    @Override
    public RC Finish() {
        RC rc = RC.RC_SUCCESS;
        while (!hasReachedEnd && rc.isSuccess()) {
            ProcessNextBit(0);
            rc = TryOutputByte();
        }
        out.flush();
        return rc;
    }

    private void ProcessNextBit(int bit) {
        double shrinkPoint = ShrinkPoint();
        if (bit == 0) {
            possibleHigh = shrinkPoint;
        } else {
            possibleLow = shrinkPoint;
        }
    }

    private RC TryOutputByte() {
        RC rc = RC.RC_SUCCESS;
        double range = workingHigh - workingLow;
        for (int i = lowSplitPointIndex + 1; i < highSplitPointIndex; i++) {
            if ((workingLow + splitPoints[i] * range) <= possibleLow) {
                lowSplitPointIndex = i;
            } else
                break;
        }
        for (int i = highSplitPointIndex - 1; i > lowSplitPointIndex; i--) {
            if ((workingLow + splitPoints[i] * range) >= possibleHigh) {
                highSplitPointIndex = i;
            } else
                break;
        }
        if (highSplitPointIndex - lowSplitPointIndex <= 1) {
            int index = lowSplitPointIndex;
            lowSplitPointIndex = -1;
            highSplitPointIndex = splitPoints.length;
            if (index == alphabetLen) {
                hasReachedEnd = true;
                rc = out.flush();
            } else {
                UpdateWorkingRange(index);
                TryZoomIn();
                UpdateWeight(index);
                rc = out.write((byte) index);
            }
        }
        return rc;
    }

    private void UpdateWorkingRange(int index) {
        double range = workingHigh - workingLow;
        workingHigh = workingLow + splitPoints[index + 1] * range;
        workingLow = workingLow + splitPoints[index] * range;
    }

    private void TryZoomIn() {
        while (true) {
            if (workingHigh < 0.5) {
                ZoomIn(0);
            } else if (workingLow >= 0.5) {
                ZoomIn(1);
            } else if (workingLow >= 0.25 && workingHigh < 0.75) {
                ZoomIn(0.5);
            } else {
                break;
            }
        }

    }

    private void ZoomIn(double expandPoint) {
        workingLow = 2 * workingLow - expandPoint;
        workingHigh = 2 * workingHigh - expandPoint;
        possibleLow = 2 * possibleLow - expandPoint;
        possibleHigh = 2 * possibleHigh - expandPoint;
    }

    private double ShrinkPoint() {
        return possibleLow + (possibleHigh - possibleLow) / 2;
    }

}
