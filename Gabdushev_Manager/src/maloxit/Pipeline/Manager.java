package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.*;

import java.io.*;
import java.util.HashMap;

class Manager {
    private static final String INPUT_FILE_CONFIG_NAME = "input_file";
    private static final String OUTPUT_FILE_CONFIG_NAME = "output_file";
    private static final String READER_CONFIG_FILE_CONFIG_NAME = "reader_config_file";
    private static final String WRITER_CONFIG_FILE_CONFIG_NAME = "writer_config_file";
    private static final String EXECUTOR_CONFIG_FILE_CONFIG_NAME = "executor_config_file";
    private static final String READER_CLASS_NAME_CONFIG_NAME = "reader_class";
    private static final String WRITER_CLASS_NAME_CONFIG_NAME = "writer_class";
    private static final String EXECUTOR_CLASS_NAME_CONFIG_NAME = "executor_class";


    class PipelineParams {
        public final String inputFileName;
        public final String outputFileName;
        public final String readerConfigFileName;
        public final String writerConfigFileName;
        public final String executorConfigFileName;
        public final String readerClassName;
        public final String writerClassName;
        public final String executorClassName;


        public PipelineParams(String inputFileName, String outputFileName, String readerConfigFileName, String writerConfigFileName, String executorConfigFileName, String readerClassName, String writerClassName, String executorClassName) {
            this.inputFileName = inputFileName;
            this.outputFileName = outputFileName;
            this.readerConfigFileName = readerConfigFileName;
            this.writerConfigFileName = writerConfigFileName;
            this.executorConfigFileName = executorConfigFileName;
            this.readerClassName = readerClassName;
            this.writerClassName = writerClassName;
            this.executorClassName = executorClassName;
        }
    }

    /**Uses parameters from given config file to build a pipeline
     * @param managerConfigFileName Manager config file path
     * @return Return Code object, which contains information about reason of the end of work
     */
    public RC runFromConfig(String managerConfigFileName) {
        IUniversalConfigReader config = new UniversalConfigReader();
        try {
            RC rc = config.SetGrammar(new Grammar(
                    INPUT_FILE_CONFIG_NAME,
                    OUTPUT_FILE_CONFIG_NAME,
                    READER_CONFIG_FILE_CONFIG_NAME,
                    WRITER_CONFIG_FILE_CONFIG_NAME,
                    EXECUTOR_CONFIG_FILE_CONFIG_NAME,
                    READER_CLASS_NAME_CONFIG_NAME,
                    WRITER_CLASS_NAME_CONFIG_NAME,
                    EXECUTOR_CLASS_NAME_CONFIG_NAME
            ), RC.RCWho.MANAGER);
            if (!rc.isSuccess())
                return rc;
            rc = config.ParseConfig(new FileReader(managerConfigFileName));
            if (!rc.isSuccess())
                return rc;
        } catch (FileNotFoundException ex) {
            return RC.RC_MANAGER_CONFIG_FILE_ERROR;
        }
        HashMap<String, String> data = config.GetData();

        String inputFileName = data.get(INPUT_FILE_CONFIG_NAME);

        String outputFileName = data.get(OUTPUT_FILE_CONFIG_NAME);

        String readerConfigFileName = data.get(READER_CONFIG_FILE_CONFIG_NAME);

        String writerConfigFileName = data.get(WRITER_CONFIG_FILE_CONFIG_NAME);

        String executorConfigFileName = data.get(EXECUTOR_CONFIG_FILE_CONFIG_NAME);

        String readerClassName = data.get(READER_CLASS_NAME_CONFIG_NAME);

        String writerClassName = data.get(WRITER_CLASS_NAME_CONFIG_NAME);

        String executorClassName = data.get(EXECUTOR_CLASS_NAME_CONFIG_NAME);

        return buildPipeline(new PipelineParams(inputFileName, outputFileName, readerConfigFileName, writerConfigFileName, executorConfigFileName, readerClassName, writerClassName, executorClassName));
    }

    /**Builds and runs a pipeline with given parameters.
     * @param pipelineParams Parameters for pipeline
     * @return Return Code object, which contains information about reason of the end of work
     */
    private RC buildPipeline(PipelineParams pipelineParams) {
        FileInputStream input;
        FileOutputStream output;
        IReader reader;
        IWriter writer;
        IExecutor executor;

        try {
            input = new FileInputStream(pipelineParams.inputFileName);
        } catch (FileNotFoundException ex) {
            return RC.RC_MANAGER_INVALID_INPUT_FILE;
        }
        try {
            output = new FileOutputStream(pipelineParams.outputFileName);
        } catch (FileNotFoundException ex) {
            return RC.RC_MANAGER_INVALID_OUTPUT_FILE;
        }

        try {
            Class<?> tmp = Class.forName(pipelineParams.readerClassName);
            if (IReader.class.isAssignableFrom(tmp))
                reader = (IReader) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_READER_CLASS;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_READER_CLASS;
        }
        try {
            Class<?> tmp = Class.forName(pipelineParams.writerClassName);
            if (IWriter.class.isAssignableFrom(tmp))
                writer = (IWriter) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        }
        try {
            Class<?> tmp = Class.forName(pipelineParams.executorClassName);
            if (IExecutor.class.isAssignableFrom(tmp))
                executor = (IExecutor) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
        }


        RC rc = reader.setInputStream(input);
        if (!rc.isSuccess())
            return rc;
        rc = writer.setOutputStream(output);
        if (!rc.isSuccess())
            return rc;

        rc = reader.setConsumer(executor);
        if (!rc.isSuccess())
            return rc;
        rc = executor.setConsumer(writer);
        if (!rc.isSuccess())
            return rc;

        rc = reader.setConfig(pipelineParams.readerConfigFileName);
        if (!rc.isSuccess())
            return rc;
        rc = executor.setConfig(pipelineParams.executorConfigFileName);
        if (!rc.isSuccess())
            return rc;
        rc = writer.setConfig(pipelineParams.writerConfigFileName);
        if (!rc.isSuccess())
            return rc;

        rc = reader.run();
        if (!rc.isSuccess())
            return rc;

        try {
            input.close();
            output.close();
        } catch (IOException e) {
            return RC.RC_MANAGER_INVALID_OUTPUT_FILE;
        }
        return RC.RC_SUCCESS;
    }
}
