package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashMap;
import java.util.Map;

public class Constant extends Term {
    String label;
    static Map<String, Constant> constants = new HashMap<>();

    public Constant(String label) {
        this.label = label;
    }

    public static Constant fetch(String label) {
        if (constants.containsKey(label))
            return constants.get(label);
        else
            return new Constant(label);
    }

    @Override
    public String toString() {
        return "" + label;
    }
}
