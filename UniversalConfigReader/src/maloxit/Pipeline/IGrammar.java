package maloxit.Pipeline;

public interface IGrammar {
    enum ParamState {
        UNSET,
        SET,
        UNKNOWN
    }

    String getNameAndValueSeparator();

    String getValueOpenBracket();

    String getValueCloseBracket();

    ParamState paramState(String paramName);

    void markParamSet(String paramName);

    void reset();
    boolean isAllSet();
}
