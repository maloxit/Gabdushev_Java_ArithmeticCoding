package maloxit.Pipeline;

import java.util.HashMap;

public class ManagerGrammar extends Grammar{
    private static final String INPUT_FILE_CONFIG_NAME = "input_file";
    private static final String OUTPUT_FILE_CONFIG_NAME = "output_file";
    private static final String READER_CONFIG_FILE_CONFIG_NAME = "reader_config_file";
    private static final String READER_CLASS_NAME_CONFIG_NAME = "reader_class";
    private static final String WRITER_CONFIG_FILE_CONFIG_NAME = "writer_config_file";
    private static final String WRITER_CLASS_NAME_CONFIG_NAME = "writer_class";
    private static final String EXECUTOR_CONFIG_FILE_CONFIG_NAME_LIST = "executor_config_file_list";
    private static final String EXECUTOR_CLASS_NAME_CONFIG_NAME_LIST = "executor_class_list";

    private static final String LIST_ITEM_SEPARATOR = "\\s*,\\s*";

    public ManagerGrammar()
    {
        super(INPUT_FILE_CONFIG_NAME,
                OUTPUT_FILE_CONFIG_NAME,
                READER_CONFIG_FILE_CONFIG_NAME,
                WRITER_CONFIG_FILE_CONFIG_NAME,
                EXECUTOR_CONFIG_FILE_CONFIG_NAME_LIST,
                READER_CLASS_NAME_CONFIG_NAME,
                WRITER_CLASS_NAME_CONFIG_NAME,
                EXECUTOR_CLASS_NAME_CONFIG_NAME_LIST);
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
        String executorConfigFileNameListStr = data.get(EXECUTOR_CONFIG_FILE_CONFIG_NAME_LIST);
        if (executorConfigFileNameListStr == null)
            return null;
        String[] executorConfigFileNameList = executorConfigFileNameListStr.trim().split(LIST_ITEM_SEPARATOR, -1);
        String executorClassNameListStr = data.get(EXECUTOR_CLASS_NAME_CONFIG_NAME_LIST);
        if (executorClassNameListStr == null)
            return null;
        String[] executorClassNameList = executorClassNameListStr.trim().split(LIST_ITEM_SEPARATOR, -1);
        return new PipelineParams(inputFileName, outputFileName, readerConfigFileName, readerClassName, writerConfigFileName, writerClassName, executorConfigFileNameList, executorClassNameList);
    }

}
