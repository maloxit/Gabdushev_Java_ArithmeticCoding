package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.RC;

class ArithmeticEncoder extends AdaptiveArithmeticCoder {

    private int bufferByte;
    private int bufferByteFreeBits;
    private int outstandingBitsCounter;
    private double workingLow;
    private double workingHigh;

    public ArithmeticEncoder(double normalWeightsMax, double ceilingWeightsMax, AutoDataBuffer out) {
        super(normalWeightsMax, ceilingWeightsMax, out);
        bufferByte = 0;
        bufferByteFreeBits = 8;
        outstandingBitsCounter = 0;
        workingLow = 0;
        workingHigh = 1;
    }

    @Override
    public RC PutByte(byte val) {
        int index = val & 0xFF;
        UpdateWorkingRange(index);
        RC rc = TryPutBits();
        if (rc.isSuccess())
            UpdateWeight(index);
        return rc;
    }

    @Override
    public RC Finish() {
        UpdateWorkingRange(alphabetLen);
        RC rc = TryPutBits();
        if (!rc.isSuccess())
            return rc;
        rc = PrintFinalByte();
        if (!rc.isSuccess())
            return rc;
        return out.flush();
    }

    private void UpdateWorkingRange(int index) {
        double range = workingHigh - workingLow;
        workingHigh = workingLow + splitPoints[index + 1] * range;
        workingLow = workingLow + splitPoints[index] * range;
    }

    private RC TryPutBits() {
        RC rc = RC.RC_SUCCESS;
        while (true) {
            if (workingHigh < 0.5) {
                ZoomIn(0);
                rc = PushBitToBuff(0);
                if (!rc.isSuccess())
                    return rc;
            } else if (workingLow >= 0.5) {
                ZoomIn(1);
                rc = PushBitToBuff(1);
                if (!rc.isSuccess())
                    return rc;
            } else if (workingLow >= 0.25 && workingHigh < 0.75) {
                ZoomIn(0.5);
                outstandingBitsCounter++;
            } else {
                break;
            }
        }
        return rc;
    }

    private void ZoomIn(double expandPoint) {
        workingLow = 2 * workingLow - expandPoint;
        workingHigh = 2 * workingHigh - expandPoint;
    }

    private RC PushBitToBuff(int bit) {
        if (bufferByteFreeBits == 0) {
            RC rc = PrintAndClearBuffByte();
            if (!rc.isSuccess())
                return rc;
        }
        bufferByte = ((bufferByte << 1) | bit);
        bufferByteFreeBits--;
        while (outstandingBitsCounter > 0) {
            for (; bufferByteFreeBits > 0 && outstandingBitsCounter > 0; bufferByteFreeBits--, outstandingBitsCounter--) {
                bufferByte <<= 1;
                if (bit == 0)
                    bufferByte |= 1;
            }
            if (bufferByteFreeBits == 0) {
                RC rc = PrintAndClearBuffByte();
                if (!rc.isSuccess())
                    return rc;
            }
        }
        return RC.RC_SUCCESS;
    }

    private RC PrintFinalByte() {
        RC rc = RC.RC_SUCCESS;
        boolean bit = (workingLow < 0.25);
        rc = PushBitToBuff(bit ? 0 : 1);
        if (!rc.isSuccess())
            return rc;
        rc = PushBitToBuff(bit ? 1 : 0);
        if (!rc.isSuccess())
            return rc;
        if (bufferByteFreeBits == 8) {
            return RC.RC_SUCCESS;
        }
        bufferByte <<= bufferByteFreeBits;
        bufferByteFreeBits = 0;
        return PrintAndClearBuffByte();
    }

    private RC PrintAndClearBuffByte() {
        RC rc = out.write((byte) bufferByte);
        bufferByte = 0;
        bufferByteFreeBits = 8;
        return rc;
    }
}

