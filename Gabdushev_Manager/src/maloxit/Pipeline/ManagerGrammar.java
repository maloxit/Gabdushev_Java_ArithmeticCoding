package maloxit.Pipeline;

import java.util.HashMap;


public class ManagerGrammar extends Grammar{
    private static final String INPUT_FILE_CONFIG_NAME = "input_file";
    private static final String OUTPUT_FILE_CONFIG_NAME = "output_file";
    private static final String READER_CONFIG_FILE_CONFIG_NAME = "reader_config_file";
    private static final String READER_CLASS_NAME_CONFIG_NAME = "reader_class";
    private static final String WRITER_CONFIG_FILE_CONFIG_NAME = "writer_config_file";
    private static final String WRITER_CLASS_NAME_CONFIG_NAME = "writer_class";
    private static final String EXECUTOR_CONFIG_FILE_CONFIG_NAME = "executor_config_file";
    private static final String EXECUTOR_CLASS_NAME_CONFIG_NAME = "executor_class";

    public ManagerGrammar()
    {
        super(INPUT_FILE_CONFIG_NAME,
                OUTPUT_FILE_CONFIG_NAME,
                READER_CONFIG_FILE_CONFIG_NAME,
                WRITER_CONFIG_FILE_CONFIG_NAME,
                EXECUTOR_CONFIG_FILE_CONFIG_NAME,
                READER_CLASS_NAME_CONFIG_NAME,
                WRITER_CLASS_NAME_CONFIG_NAME,
                EXECUTOR_CLASS_NAME_CONFIG_NAME);
    }
    public PipelineParams PipelineParamsFromData(HashMap<String, String> data)
    {
        String inputFileName = data.get(INPUT_FILE_CONFIG_NAME);
        if (inputFileName == null)
            return null;
        String outputFileName = data.get(OUTPUT_FILE_CONFIG_NAME);
        if (outputFileName == null)
            return null;
        String readerConfigFileName = data.get(READER_CONFIG_FILE_CONFIG_NAME);
        if (readerConfigFileName == null)
            return null;
        String readerClassName = data.get(READER_CLASS_NAME_CONFIG_NAME);
        if (readerClassName == null)
            return null;
        String writerConfigFileName = data.get(WRITER_CONFIG_FILE_CONFIG_NAME);
        if (writerConfigFileName == null)
            return null;
        String writerClassName = data.get(WRITER_CLASS_NAME_CONFIG_NAME);
        if (writerClassName == null)
            return null;
        String executorConfigFileName = data.get(EXECUTOR_CONFIG_FILE_CONFIG_NAME);
        if (executorConfigFileName == null)
            return null;
        String executorClassName = data.get(EXECUTOR_CLASS_NAME_CONFIG_NAME);
        if (executorClassName == null)
            return null;
        return new PipelineParams(inputFileName, outputFileName, readerConfigFileName, readerClassName, writerConfigFileName, writerClassName, executorConfigFileName, executorClassName);
    }

}
