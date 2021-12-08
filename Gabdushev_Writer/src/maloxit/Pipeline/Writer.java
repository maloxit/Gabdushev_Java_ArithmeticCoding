package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.IWriter;
import com.java_polytech.pipeline_interfaces.RC;

import java.io.*;
import java.util.HashMap;

class WriterParams {

    public final String BUFFER_SIZE_STR;

    WriterParams(String buffer_size_str) {
        BUFFER_SIZE_STR = buffer_size_str;
    }
}

public class Writer implements IWriter {

    private int BUFFER_SIZE = 0;
    private OutputStream output;
    private BufferedOutputStream bufferedOutput;

    /**
     * Uses a given config file to set parameters value
     * @param cfgFileName Writer config file path
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC setConfig(String cfgFileName) {
        RC rc;
        IUniversalConfigReader config = new UniversalConfigReader();
        WriterGrammar grammar = new WriterGrammar();
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
        WriterParams params = grammar.WriterParamsFromData(data);
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
    private RC SemanticAnalise(WriterParams params) {
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
     * Processes given data package
     * @param buff input data package or null if input has reached end
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC consume(byte[] buff) {
        if (bufferedOutput == null)
            bufferedOutput = new BufferedOutputStream(output, BUFFER_SIZE);
        if (buff == null) {
            try {
                bufferedOutput.flush();
            } catch (IOException e) {
                return RC.RC_WRITER_FAILED_TO_WRITE;
            }
            return RC.RC_SUCCESS;
        }
        try {
            bufferedOutput.write(buff);
        } catch (IOException e) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }
        return RC.RC_SUCCESS;
    }

    /**
     * Sets an output stream to write data to
     * @param output output stream to write data to
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC setOutputStream(OutputStream output) {
        this.output = output;
        return RC.RC_SUCCESS;
    }
}
