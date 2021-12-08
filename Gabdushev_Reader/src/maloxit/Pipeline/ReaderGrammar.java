package maloxit.Pipeline;

import java.util.HashMap;

public class ReaderGrammar extends Grammar{
    private static final String BUFFER_SIZE_CONFIG_NAME = "buffer_size";

    public ReaderGrammar()
    {
        super(BUFFER_SIZE_CONFIG_NAME);
    }

    public ReaderParams ReaderParamsFromData(HashMap<String, String> data)
    {
        String BUFFER_SIZE_STR = data.get(BUFFER_SIZE_CONFIG_NAME);
        if (BUFFER_SIZE_STR == null)
            return null;
        return new ReaderParams(BUFFER_SIZE_STR);
    }
}
