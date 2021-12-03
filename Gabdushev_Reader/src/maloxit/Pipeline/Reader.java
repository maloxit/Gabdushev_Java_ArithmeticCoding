package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

class ReaderParams
{
    public final String BUFFER_SIZE_STR;

    ReaderParams(String buffer_size_str) {
        BUFFER_SIZE_STR = buffer_size_str;
    }
}

public class Reader implements IReader {


    private int BUFFER_SIZE;
    private InputStream input;
    private IConsumer consumer;
    private byte[] preparedData;
    private final TYPE[] outputTypes = {TYPE.BYTE_ARRAY, TYPE.CHAR_ARRAY, TYPE.INT_ARRAY};
    private TYPE fixedOutputType;


    /**
     * Uses a given config file to set parameters value
     * @param cfgFileName Reader config file path
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC setConfig(String cfgFileName) {
        RC rc;
        IUniversalConfigReader config = new UniversalConfigReader();
        ReaderGrammar grammar = new ReaderGrammar();
        try {
            rc = config.SetGrammar(grammar, RC.RCWho.READER);
            if (!rc.isSuccess())
                return rc;
            rc = config.ParseConfig(new FileReader(cfgFileName));
            if (!rc.isSuccess())
                return rc;
        } catch (FileNotFoundException ex) {
            return RC.RC_READER_CONFIG_FILE_ERROR;
        }
        HashMap<String, String> data = config.GetData();
        ReaderParams params = grammar.ReaderParamsFromData(data);
        if (params == null)
            return RC.RC_READER_CONFIG_GRAMMAR_ERROR;

        rc = SemanticAnalise(params);
        if (!rc.isSuccess())
            return rc;

        return RC.RC_SUCCESS;
    }

    /**
     * Checks if given parameters are semantically correct and initialises readers fields.
     * @param params Parameters for reader
     * @return Return Code object, which contains information about reason of the end of work
     */
    private RC SemanticAnalise(ReaderParams params) {
        try {
            String tmp = params.BUFFER_SIZE_STR;
            BUFFER_SIZE = Integer.parseInt(tmp);
            if (BUFFER_SIZE <= 0) {
                return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
            }
        } catch (NumberFormatException ex) {
            return RC.RC_READER_CONFIG_SEMANTIC_ERROR;
        }
        assert (BUFFER_SIZE % Character.BYTES == 0 && BUFFER_SIZE % Integer.BYTES == 0);
        return RC.RC_SUCCESS;
    }

    /**
     * Sets a consumer that will consume and process information provided by this object
     * @param consumer an IConsumer object
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC setConsumer(IConsumer consumer) {
        this.consumer = consumer;
        return consumer.setProvider(this);
    }

    /**
     * Sets an input stream to read from
     * @param input stream of input data to read
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC setInputStream(InputStream input) {
        this.input = input;
        return RC.RC_SUCCESS;
    }

    @Override
    public TYPE[] getOutputTypes()
    {
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

    class CharMediator implements IMediator
    {
        @Override
        public Object getData() {
            if (preparedData == null)
                return null;
            String text = new String(preparedData, StandardCharsets.UTF_8);
            char[] chars = text.toCharArray();
            return chars;
        }
    }

    class IntMediator implements IMediator
    {
        @Override
        public Object getData() {
            if (preparedData == null)
                return null;
            IntBuffer intBuff = ByteBuffer.wrap(preparedData).asIntBuffer();
            int[] ints = new int[intBuff.remaining()];
            intBuff.get(ints);
            return preparedData;
        }
    }

    @Override
    public IMediator getMediator(TYPE type) {
        switch (type)
        {
            case BYTE_ARRAY:
                fixedOutputType = type;
                return new ByteMediator();
            case CHAR_ARRAY:
                fixedOutputType = type;
                return new CharMediator();
            case INT_ARRAY:
                fixedOutputType = type;
                return new IntMediator();
        }
        return null;
    }

    /**
     * Runs pipeline by reading and sending packets od input data to next pipeline element to process until all is read or some not SUCCESS Return Code received
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        int readLen;
        while (true) {
            try {
                readLen = input.read(buffer, 0, BUFFER_SIZE);
            } catch (IOException e) {
                return RC.RC_READER_FAILED_TO_READ;
            }
            if (readLen <= 0) {
                break;
            }
            switch (fixedOutputType) {
                case BYTE_ARRAY:
                    break;
                case CHAR_ARRAY:
                    if (readLen % 2 != 0)
                        return new RC(RC.RCWho.READER, RC.RCType.CODE_CUSTOM_ERROR, "Can't read input file as char array");
                    break;
                case INT_ARRAY:
                    if (readLen % 4 != 0)
                        return new RC(RC.RCWho.READER, RC.RCType.CODE_CUSTOM_ERROR, "Can't read input file as int array");
                    break;
            }
            preparedData = Arrays.copyOf(buffer, readLen);
            RC rc = consumer.consume();
            if (!rc.isSuccess())
                return rc;
        }
        preparedData = null;
        RC rc = consumer.consume();
        if (!rc.isSuccess())
            return rc;
        try {
            input.close();
        } catch (IOException e) {
            return RC.RC_READER_FAILED_TO_READ;
        }
        return RC.RC_SUCCESS;
    }
}
