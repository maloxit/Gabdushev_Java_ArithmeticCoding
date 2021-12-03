package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Writer implements IWriter {


    private static final String BUFFER_SIZE_CONFIG_NAME = "buffer_size";
    private int BUFFER_SIZE = 0;
    private OutputStream output;
    private BufferedOutputStream bufferedOutput;
    private final TYPE[] inputTypes = {TYPE.BYTE_ARRAY, TYPE.CHAR_ARRAY, TYPE.INT_ARRAY};
    private TYPE fixedInputType;
    private IMediator mediator;

    /**
     * Uses a given config file to set parameters value
     * @param cfgFileName Writer config file path
     * @return Return Code object, which contains information about reason of the end of work
     */
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
        assert (BUFFER_SIZE % Character.BYTES == 0 && BUFFER_SIZE % Integer.BYTES == 0);
        if (output != null && bufferedOutput == null) {
            bufferedOutput = new BufferedOutputStream(output, BUFFER_SIZE);
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
        if (BUFFER_SIZE != 0) {
            bufferedOutput = new BufferedOutputStream(this.output, BUFFER_SIZE);
        }
        return RC.RC_SUCCESS;
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
            return RC.RC_WRITER_TYPES_INTERSECTION_EMPTY_ERROR;
        mediator = iProvider.getMediator(fixedInputType);
        return RC.RC_SUCCESS;
    }

    /**
     * Processes given data package
     * @param buff input data package or null if input has reached end
     * @return Return Code object, which contains information about reason of the end of work
     */
    @Override
    public RC consume() {
        Object data = mediator.getData();
        if (data == null) {
            try {
                bufferedOutput.flush();
            } catch (IOException e) {
                return RC.RC_WRITER_FAILED_TO_WRITE;
            }
            return RC.RC_SUCCESS;
        }
        byte[] preparedData = null;
        switch (fixedInputType) {
            case BYTE_ARRAY:
                preparedData = (byte[])data;
                break;
            case CHAR_ARRAY:
                preparedData = new String((char[])data).getBytes(StandardCharsets.UTF_8);
                break;
            case INT_ARRAY:
                ByteBuffer byteBuff = ByteBuffer.allocate(((int[])data).length * Integer.BYTES);
                IntBuffer intBuff = byteBuff.asIntBuffer();
                intBuff.put((int[])data);
                preparedData = byteBuff.array();
                break;
        }
        try {
            bufferedOutput.write(preparedData);
        } catch (IOException e) {
            return RC.RC_WRITER_FAILED_TO_WRITE;
        }
        return RC.RC_SUCCESS;
    }
}
