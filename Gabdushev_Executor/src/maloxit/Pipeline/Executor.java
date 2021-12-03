package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.IConsumer;
import com.java_polytech.pipeline_interfaces.IExecutor;
import com.java_polytech.pipeline_interfaces.RC;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

public class Executor implements IExecutor {

    private static final String BUFFER_SIZE_CONFIG_NAME = "buffer_size";
    private static final String NORMAL_WEIGHTS_MAX_CONFIG_NAME = "normal_weights_max";
    private static final String CEILING_WEIGHTS_MAX_CONFIG_NAME = "ceiling_weights_max";
    private static final String MODE_CONFIG_NAME = "mode";
    private static final String DECODING_MODE_STRING = "DECODING";
    private static final String ENCODING_MODE_STRING = "ENCODING";

    enum CodingMode {
        ENCODING,
        DECODING
    }

    private int BUFFER_SIZE;
    private double normalWeightsMax;
    private double ceilingWeightsMax;
    private CodingMode mode;
    private AdaptiveArithmeticCoder codingProcessor;
    private IConsumer consumer;

    /**
     * Uses a given config file to set parameters value
     * @param cfgFileName Executor config file path
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC setConfig(String cfgFileName) {
        IUniversalConfigReader config = new UniversalConfigReader();
        try {
            RC rc = config.SetGrammar(new Grammar(
                    BUFFER_SIZE_CONFIG_NAME,
                    NORMAL_WEIGHTS_MAX_CONFIG_NAME,
                    CEILING_WEIGHTS_MAX_CONFIG_NAME,
                    MODE_CONFIG_NAME
            ), RC.RCWho.EXECUTOR);
            if (!rc.isSuccess()) {
                return rc;
            }
            rc = config.ParseConfig(new FileReader(cfgFileName));
            if (!rc.isSuccess()) {
                return rc;
            }
        } catch (FileNotFoundException ex) {
            return RC.RC_EXECUTOR_CONFIG_FILE_ERROR;
        }
        HashMap<String, String> data = config.GetData();

        String bufferSizeValueString = data.get(BUFFER_SIZE_CONFIG_NAME);
        String normalWeightsMaxValueString = data.get(NORMAL_WEIGHTS_MAX_CONFIG_NAME);
        String ceilingWeightsMaxValueString = data.get(CEILING_WEIGHTS_MAX_CONFIG_NAME);
        String modeValueString = data.get(MODE_CONFIG_NAME);


        try {
            BUFFER_SIZE = Integer.parseInt(bufferSizeValueString);
        } catch (NumberFormatException ex) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        try {
            normalWeightsMax = Integer.parseInt(normalWeightsMaxValueString);
        } catch (NumberFormatException ex) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        try {
            ceilingWeightsMax = Integer.parseInt(ceilingWeightsMaxValueString);
        } catch (NumberFormatException ex) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }


        if (BUFFER_SIZE <= 0) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        if (normalWeightsMax <= 1) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        if (ceilingWeightsMax <= normalWeightsMax) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        if (modeValueString.equals(ENCODING_MODE_STRING)) {
            mode = CodingMode.ENCODING;
        } else if (modeValueString.equals(DECODING_MODE_STRING)) {
            mode = CodingMode.DECODING;
        } else {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }

        switch (mode) {
            case ENCODING:
                codingProcessor = new ArithmeticEncoder(normalWeightsMax, ceilingWeightsMax, new AutoDataBuffer(consumer::consume, BUFFER_SIZE));
                break;
            case DECODING:
                codingProcessor = new ArithmeticDecoder(normalWeightsMax, ceilingWeightsMax, new AutoDataBuffer(consumer::consume, BUFFER_SIZE));
                break;
        }

        return RC.RC_SUCCESS;
    }

    /**
     * Processes given data package
     * @param buff input data package or null if input has reached end
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC consume(byte[] buff) {
        RC rc = RC.RC_SUCCESS;
        if (buff == null) {
            rc = codingProcessor.Finish();
            if (!rc.isSuccess())
                return rc;
            return consumer.consume(null);
        }
        for (int i = 0; i < buff.length && rc.isSuccess(); i++) {
            rc = codingProcessor.PutByte(buff[i]);
        }
        return rc;
    }

    /**
     * Sets a consumer that will consume and process information provided by this object
     * @param consumer consumer an IConsumer object
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC setConsumer(IConsumer consumer) {
        this.consumer = consumer;
        return RC.RC_SUCCESS;
    }
}
