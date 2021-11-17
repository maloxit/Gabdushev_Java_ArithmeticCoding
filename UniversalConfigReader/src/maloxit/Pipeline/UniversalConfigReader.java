package maloxit.Pipeline;

import com.java_polytech.pipeline_interfaces.RC;

import java.util.HashMap;
import java.util.Scanner;

class UniversalConfigReader implements IUniversalConfigReader{
    private IGrammar grammar;
    private final static char[] AdditionalAllowedNameChars = {'-', '_'};
    private RC.RCWho who;
    private HashMap<String, String> data;

    @Override
    public RC SetGrammar(IGrammar grammar, RC.RCWho who) {
        if (grammar == null || who == null || grammar.getNameAndValueSeparator() == null || grammar.getValueOpenBracket() == null || grammar.getValueCloseBracket() == null) {
            return new RC(RC.RCWho.UNKNOWN, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Incorrect grammar for UniversalConfigReader.");
        }
        this.grammar = grammar;
        this.who = who;
        return RC.RC_SUCCESS;
    }

    @Override
    public RC ParseConfig(Readable in) {
        Scanner scanner = new Scanner(in);
        data = new HashMap<>();
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] lst = line.trim().split(grammar.getNameAndValueSeparator(), -1);
            if (lst.length == 2) {
                String name = lst[0].trim();
                String valueString = lst[1].trim();
                if (!IsCorrectName(name)) {
                    return RC.RC_MANAGER_CONFIG_GRAMMAR_ERROR;
                }
                if (!IsCorrectValueString(valueString)) {
                    return RC.RC_MANAGER_CONFIG_GRAMMAR_ERROR;
                }
                IGrammar.ParamState state = grammar.paramState(name);
                if (state == IGrammar.ParamState.SET) {
                    return new RC(who, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Multiple definitions of one parameter.");
                }
                if (state == IGrammar.ParamState.UNKNOWN) {
                    return new RC(who, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Unknown parameter " + name + ".");
                }
                data.put(name, valueString.substring(grammar.getValueOpenBracket().length(), valueString.length() - grammar.getValueCloseBracket().length()));
                grammar.markParamSet(name);
            }
        }
        if (!grammar.isAllSet()) {
            return new RC(who, RC.RCType.CODE_CONFIG_GRAMMAR_ERROR, "Missing some parameters.");
        }
        return RC.RC_SUCCESS;
    }

    @Override
    public HashMap<String, String> GetData() {
        return data;
    }

    private boolean IsCorrectValueString(String value) {
        return !value.isEmpty() &&
                value.length() >= (grammar.getValueOpenBracket().length() + grammar.getValueCloseBracket().length()) &&
                value.startsWith(grammar.getValueOpenBracket()) &&
                value.endsWith(grammar.getValueCloseBracket());
    }

    private boolean IsCorrectName(String name) {
        if (name.isEmpty() || !Character.isLetter(name.charAt(0))) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (!(Character.isLetter(ch) || Character.isDigit(ch))) {
                boolean isAllowed = false;
                for (char allowedChar :
                        AdditionalAllowedNameChars) {
                    if (ch == allowedChar) {
                        isAllowed = true;
                        break;
                    }
                }
                if (!isAllowed)
                    return false;
            }
        }
        return true;
    }

}
