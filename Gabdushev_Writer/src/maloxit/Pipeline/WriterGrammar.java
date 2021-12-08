package maloxit.Pipeline;

import java.util.HashMap;

public class WriterGrammar extends Grammar{
    private static final String BUFFER_SIZE_CONFIG_NAME = "buffer_size";

    public WriterGrammar()
    {
        super(BUFFER_SIZE_CONFIG_NAME);
    }

    public WriterParams WriterParamsFromData(HashMap<String, String> data)
    {
        String BUFFER_SIZE_STR = data.get(BUFFER_SIZE_CONFIG_NAME);
        if (BUFFER_SIZE_STR == null)
            return null;
        return new WriterParams(BUFFER_SIZE_STR);
    }
}
