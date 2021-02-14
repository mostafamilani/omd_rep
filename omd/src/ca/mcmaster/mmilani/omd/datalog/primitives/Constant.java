package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashMap;
import java.util.Map;

public class Constant extends Term {
    private static Map<String, Constant> constants = new HashMap<>();

    private Constant(String label) {
        this.label = label;
    }

    public static Constant fetch(String label) {
        if (!constants.containsKey(label))
            constants.put(label, new Constant(label));
        return constants.get(label);
    }

    @Override
    public String toString() {
        return "'" + label + "'";
    }
}
