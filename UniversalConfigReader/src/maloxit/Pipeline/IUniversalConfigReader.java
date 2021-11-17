package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.RC;

import java.util.HashMap;

public interface IUniversalConfigReader {
    RC SetGrammar(IGrammar grammar, RC.RCWho who);

    RC ParseConfig(Readable in);

    HashMap<String, String> GetData();
}
