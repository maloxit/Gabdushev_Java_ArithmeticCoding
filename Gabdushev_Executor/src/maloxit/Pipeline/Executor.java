package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;

class ExecutorParams
{
    public final String BUFFER_SIZE_STR;
    public final String normalWeightsMaxStr;
    public final String ceilingWeightsMaxStr;
    public final String modeStr;

    public ExecutorParams(String BUFFER_SIZE_STR, String normalWeightsMaxStr, String ceilingWeightsMaxStr, String modeStr) {
        this.BUFFER_SIZE_STR = BUFFER_SIZE_STR;
        this.normalWeightsMaxStr = normalWeightsMaxStr;
        this.ceilingWeightsMaxStr = ceilingWeightsMaxStr;
        this.modeStr = modeStr;
    }
}

public class Executor implements IExecutor {

    enum CodingMode {
        ENCODING("ENCODING"),
        DECODING("DECODING");
        public final String string;

        CodingMode(String string) {
            this.string = string;
        }
    }

    private int BUFFER_SIZE;
    private double normalWeightsMax;
    private double ceilingWeightsMax;
    private CodingMode mode;
    private AdaptiveArithmeticCoder codingProcessor;
    private IConsumer consumer;
    private final TYPE[] outputTypes = {TYPE.BYTE_ARRAY};
    private TYPE fixedOutputType;

    private final TYPE[] inputTypes = {TYPE.BYTE_ARRAY};
    private TYPE fixedInputType;
    private IMediator mediator;

    private byte[] buffer;
    private byte[] preparedData;
    private int filled;

    /**
     * Uses a given config file to set parameters value
     * @param cfgFileName Executor config file path
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC setConfig(String cfgFileName) {
        RC rc;
        IUniversalConfigReader config = new UniversalConfigReader();
        ExecutorGrammar grammar = new ExecutorGrammar();
        try {
            rc = config.SetGrammar(grammar, RC.RCWho.EXECUTOR);
            if (!rc.isSuccess())
                return rc;
            rc = config.ParseConfig(new FileReader(cfgFileName));
            if (!rc.isSuccess())
                return rc;
        } catch (FileNotFoundException ex) {
            return RC.RC_EXECUTOR_CONFIG_FILE_ERROR;
        }
        HashMap<String, String> data = config.GetData();
        ExecutorParams params = grammar.ExecutorParamsFromData(data);
        if (params == null)
            return RC.RC_EXECUTOR_CONFIG_GRAMMAR_ERROR;
        rc = SemanticAnalise(params);
        if (!rc.isSuccess())
            return rc;

        buffer = new byte[BUFFER_SIZE];
        filled = 0;
        switch (mode) {
            case ENCODING:
                codingProcessor = new ArithmeticEncoder(normalWeightsMax, ceilingWeightsMax, this::callbackWrite);
                break;
            case DECODING:
                codingProcessor = new ArithmeticDecoder(normalWeightsMax, ceilingWeightsMax, this::callbackWrite);
                break;
        }
        return RC.RC_SUCCESS;
    }

    /**
     * Checks if given parameters are semantically correct and initialises executors fields.
     * @param params Parameters for executor
     * @return Return Code object, which contains information about reason of the end of work
     */
    private RC SemanticAnalise(ExecutorParams params)
    {
        try {
            BUFFER_SIZE = Integer.parseInt(params.BUFFER_SIZE_STR);
        } catch (NumberFormatException ex) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        try {
            normalWeightsMax = Integer.parseInt(params.normalWeightsMaxStr);
        } catch (NumberFormatException ex) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        try {
            ceilingWeightsMax = Integer.parseInt(params.ceilingWeightsMaxStr);
        } catch (NumberFormatException ex) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        if (BUFFER_SIZE <= 0) {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        assert (BUFFER_SIZE % Character.BYTES == 0 && BUFFER_SIZE % Integer.BYTES == 0);
        if (normalWeightsMax <= 1 || ceilingWeightsMax <= normalWeightsMax)
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        if (params.modeStr.equals(CodingMode.ENCODING.string)) {
            mode = CodingMode.ENCODING;
        } else if (params.modeStr.equals(CodingMode.DECODING.string)) {
            mode = CodingMode.DECODING;
        } else {
            return RC.RC_EXECUTOR_CONFIG_SEMANTIC_ERROR;
        }
        return RC.RC_SUCCESS;
    }


    private RC callbackWrite(byte val) {
        buffer[filled] = val;
        if (++filled == buffer.length) {
            return flush();
        }
        return RC.RC_SUCCESS;
    }
    private RC flush() {
        preparedData = Arrays.copyOf(buffer, filled);
        filled = 0;
        return consumer.consume();
    }

    /**
     * Processes next data package
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC consume() {
        byte[] data = (byte[])mediator.getData();
        RC rc = RC.RC_SUCCESS;
        if (data == null) {
            rc = codingProcessor.Finish();
            if (!rc.isSuccess())
                return rc;
            rc = flush();
            if (!rc.isSuccess())
                return rc;
            preparedData = null;
            return consumer.consume();
        }
        for (int i = 0; i < data.length && rc.isSuccess(); i++) {
            rc = codingProcessor.PutByte(data[i]);
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
        return this.consumer.setProvider(this);
    }

    @Override
    public RC setProvider(IProvider iProvider) {
        TYPE[] providerTypes = iProvider.getOutputTypes();
        fixedInputType = null;
        for (int i = 0; i < inputTypes.length && fixedInputType == null; i++) {
            for (int j = 0; j < providerTypes.length && fixedInputType == null; i++) {
                if (inputTypes[i].equals(providerTypes[j]))
                    fixedInputType = inputTypes[i];
            }
        }
        if (fixedInputType == null)
            return RC.RC_EXECUTOR_TYPES_INTERSECTION_EMPTY_ERROR;
        mediator = iProvider.getMediator(fixedInputType);
        return RC.RC_SUCCESS;
    }

    @Override
    public TYPE[] getOutputTypes() {
        return outputTypes;
    }

    class ByteMediator implements IMediator
    {
        @Override
        public Object getData() {
            if (preparedData == null)
                return null;
            return preparedData;
        }
    }

    @Override
    public IMediator getMediator(TYPE type) {
        if (type == TYPE.BYTE_ARRAY) {
            fixedOutputType = type;
            return new ByteMediator();
        }
        else
            return null;
    }
}
