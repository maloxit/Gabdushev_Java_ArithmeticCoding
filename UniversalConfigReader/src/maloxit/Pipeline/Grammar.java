package maloxit.Pipeline;

import java.util.HashMap;

public class Grammar implements IGrammar{
    private final HashMap<String, Boolean> paramNames;
    private final static String NameAndValueSeparator = ":=";

    Grammar(String ... params)
    {
        paramNames = new HashMap<>(params.length);
        for (String param:
                params) {
            paramNames.put(param, false);
        }
    }


    @Override
    public String getNameAndValueSeparator() {
        return NameAndValueSeparator;
    }

    @Override
    public String getValueOpenBracket() {
        return "\"";
    }

    @Override
    public String getValueCloseBracket() {
        return "\"";
    }

    @Override
    public ParamState paramState(String paramName)
    {
        Boolean isSet = paramNames.get(paramName);
        if (isSet == null)
            return ParamState.UNKNOWN;
        if (isSet)
            return ParamState.SET;
        else
            return ParamState.UNSET;
    }

    @Override
    public void markParamSet(String paramName) {
        Boolean isSet = paramNames.get(paramName);
        if (isSet != null) {
            paramNames.replace(paramName, true);
        }
    }

    @Override
    public void reset() {
        paramNames.replaceAll((name, isSet) -> false);
    }

    @Override
    public boolean isAllSet() {
        return !paramNames.containsValue(false);
    }
}
