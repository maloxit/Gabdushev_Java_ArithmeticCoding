package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.IWriter;
import com.java_polytech.pipeline_interfaces.RC;

import java.io.*;
import java.util.HashMap;

public class Writer implements IWriter {


    private static final String BUFFER_SIZE_CONFIG_NAME = "buffer_size";

    private int BUFFER_SIZE = 0;
    private OutputStream output;
    private BufferedOutputStream bufferedOutput;

    @Override
    public RC setConfig(String cfgFileName) {
        IUniversalConfigReader config = new UniversalConfigReader();
        try {
            RC rc = config.SetGrammar(new Grammar(
                    BUFFER_SIZE_CONFIG_NAME
            ), RC.RCWho.WRITER);
            if (!rc.isSuccess()) {
                return rc;
            }
            rc = config.ParseConfig(new FileReader(cfgFileName));
            if (!rc.isSuccess()) {
                return rc;
            }
        } catch (FileNotFoundException ex) {
            return RC.RC_WRITER_CONFIG_FILE_ERROR;
        }
        HashMap<String, String> data = config.GetData();
        try {
            String tmp = data.get(BUFFER_SIZE_CONFIG_NAME);
            BUFFER_SIZE = Integer.parseInt(tmp);
            if (BUFFER_SIZE <= 0) {
                return RC.RC_WRITER_CONFIG_SEMANTIC_ERROR;
            }
        } catch (NumberFormatException ex) {
            return RC.RC_WRITER_CONFIG_SEMANTIC_ERROR;
        }
        if (output != null && bufferedOutput == null) {
            bufferedOutput = new BufferedOutputStream(output, BUFFER_SIZE);
        }
        return RC.RC_SUCCESS;
    }

    @Override
    public RC consume(byte[] buff) {
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

    @Override
    public RC setOutputStream(OutputStream output) {
        this.output = output;
        if (BUFFER_SIZE != 0) {
            bufferedOutput = new BufferedOutputStream(this.output, BUFFER_SIZE);
        }
        return RC.RC_SUCCESS;
    }
}
