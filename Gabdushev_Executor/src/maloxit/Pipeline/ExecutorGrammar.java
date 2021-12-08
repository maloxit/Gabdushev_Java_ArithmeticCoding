package maloxit.Pipeline;

import java.util.HashMap;

public class ExecutorGrammar extends Grammar{
    private static final String BUFFER_SIZE_CONFIG_NAME = "buffer_size";
    private static final String NORMAL_WEIGHTS_MAX_CONFIG_NAME = "normal_weights_max";
    private static final String CEILING_WEIGHTS_MAX_CONFIG_NAME = "ceiling_weights_max";
    private static final String MODE_CONFIG_NAME = "mode";

    public ExecutorGrammar()
    {
        super(BUFFER_SIZE_CONFIG_NAME,
                NORMAL_WEIGHTS_MAX_CONFIG_NAME,
                CEILING_WEIGHTS_MAX_CONFIG_NAME,
                MODE_CONFIG_NAME);
    }

    public ExecutorParams ExecutorParamsFromData(HashMap<String, String> data)
    {
        String BUFFER_SIZE_STR = data.get(BUFFER_SIZE_CONFIG_NAME);
        if (BUFFER_SIZE_STR == null)
            return null;
        String normalWeightsMaxStr = data.get(NORMAL_WEIGHTS_MAX_CONFIG_NAME);
        if (normalWeightsMaxStr == null)
            return null;
        String ceilingWeightsMaxStr = data.get(CEILING_WEIGHTS_MAX_CONFIG_NAME);
        if (ceilingWeightsMaxStr == null)
            return null;
        String modeStr = data.get(MODE_CONFIG_NAME);
        if (modeStr == null)
            return null;
        return new ExecutorParams(BUFFER_SIZE_STR, normalWeightsMaxStr, ceilingWeightsMaxStr, modeStr);
    }
}
