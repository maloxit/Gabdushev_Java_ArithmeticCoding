package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.*;

import java.io.*;
import java.util.HashMap;

class PipelineParams {
    public final String inputFileName;
    public final String outputFileName;
    public final String readerConfigFileName;
    public final String readerClassName;
    public final String writerConfigFileName;
    public final String writerClassName;
    public final String[] executorConfigFileNameList;
    public final String[] executorClassNameList;

    public PipelineParams(String inputFileName, String outputFileName, String readerConfigFileName, String readerClassName, String writerConfigFileName, String writerClassName, String[] executorConfigFileNameList, String[] executorClassNameList) {
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
        this.readerConfigFileName = readerConfigFileName;
        this.readerClassName = readerClassName;
        this.writerConfigFileName = writerConfigFileName;
        this.writerClassName = writerClassName;
        this.executorConfigFileNameList = executorConfigFileNameList;
        this.executorClassNameList = executorClassNameList;
    }
}

class Manager {
    private FileInputStream input;
    private FileOutputStream output;
    private String readerConfigFileName;
    private IReader reader;
    private String writerConfigFileName;
    private IWriter writer;
    private IExecutor[] executorList;
    private String[] executorConfigFileNameList;

    /**
     * Uses parameters from given config file to build a pipeline
     * @param managerConfigFileName Manager config file path
     * @return Return Code object, which contains information about reason of the end of work
     */
    public RC runFromConfig(String managerConfigFileName) {
        RC rc;
        IUniversalConfigReader config = new UniversalConfigReader();
        ManagerGrammar grammar = new ManagerGrammar();
        try {
            rc = config.SetGrammar(grammar, RC.RCWho.MANAGER);
            if (!rc.isSuccess())
                return rc;
            rc = config.ParseConfig(new FileReader(managerConfigFileName));
            if (!rc.isSuccess())
                return rc;
        } catch (FileNotFoundException ex) {
            return RC.RC_MANAGER_CONFIG_FILE_ERROR;
        }
        HashMap<String, String> data = config.GetData();
        PipelineParams params = grammar.PipelineParamsFromData(data);
        if (params == null)
            return RC.RC_MANAGER_CONFIG_GRAMMAR_ERROR;
        SemanticAnalise(params);
        return buildPipeline();
    }

    /**
     * Checks if given parameters are semantically correct and initialises manager fields.
     * @param params Parameters for pipeline
     * @return Return Code object, which contains information about reason of the end of work
     */
    private RC SemanticAnalise(PipelineParams params) {

        readerConfigFileName = params.readerConfigFileName;
        writerConfigFileName = params.writerConfigFileName;
        executorConfigFileNameList = params.executorConfigFileNameList;
        try {
            input = new FileInputStream(params.inputFileName);
        } catch (FileNotFoundException ex) {
            return RC.RC_MANAGER_INVALID_INPUT_FILE;
        }
        try {
            output = new FileOutputStream(params.outputFileName);
        } catch (FileNotFoundException ex) {
            return RC.RC_MANAGER_INVALID_OUTPUT_FILE;
        }

        try {
            Class<?> tmp = Class.forName(params.readerClassName);
            if (IReader.class.isAssignableFrom(tmp))
                reader = (IReader) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_READER_CLASS;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_READER_CLASS;
        }
        try {
            Class<?> tmp = Class.forName(params.writerClassName);
            if (IWriter.class.isAssignableFrom(tmp))
                writer = (IWriter) tmp.getDeclaredConstructor().newInstance();
            else
                return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_WRITER_CLASS;
        }

        if (params.executorConfigFileNameList.length != params.executorClassNameList.length)
            return new RC(RC.RCWho.MANAGER, RC.RCType.CODE_CONFIG_SEMANTIC_ERROR, "Executors config files count must match the count of executor classes.");
        executorList = new IExecutor[params.executorClassNameList.length];
        try {

            for (int i = 0; i < executorList.length; i++) {
                Class<?> tmp = Class.forName(params.executorClassNameList[i]);
                if (IExecutor.class.isAssignableFrom(tmp))
                    executorList[i] = (IExecutor) tmp.getDeclaredConstructor().newInstance();
                else
                    return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
            }
        } catch (Exception e) {
            return RC.RC_MANAGER_INVALID_EXECUTOR_CLASS;
        }
        return RC.RC_SUCCESS;
    }

    /**
     * Builds and runs a pipeline.
     * @return Return Code object, which contains information about reason of the end of work
     */
    private RC buildPipeline() {
        RC rc = reader.setInputStream(input);
        if (!rc.isSuccess())
            return rc;
        rc = writer.setOutputStream(output);
        if (!rc.isSuccess())
            return rc;

        IProvider provider = reader;
        for (IExecutor iExecutor : executorList) {
            rc = provider.setConsumer(iExecutor);
            if (!rc.isSuccess())
                return rc;
            provider = iExecutor;
        }
        rc = provider.setConsumer(writer);
        if (!rc.isSuccess())
            return rc;

        rc = reader.setConfig(readerConfigFileName);
        if (!rc.isSuccess())
            return rc;
        for (int i = 0; i < executorList.length; i++) {
            rc = executorList[i].setConfig(executorConfigFileNameList[i]);
            if (!rc.isSuccess())
                return rc;
        }
        rc = writer.setConfig(writerConfigFileName);
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
