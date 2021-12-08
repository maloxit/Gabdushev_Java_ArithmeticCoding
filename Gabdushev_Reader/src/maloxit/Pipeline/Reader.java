package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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

    private static final String BUFFER_SIZE_CONFIG_NAME = "buffer_size";

    private int BUFFER_SIZE;
    private InputStream input;
    private IConsumer consumer;


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
        return RC.RC_SUCCESS;
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
            byte[] data = Arrays.copyOf(buffer, readLen);
            RC rc = consumer.consume(data);
            if (!rc.isSuccess())
                return rc;
        }
        RC rc = consumer.consume(null);
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
