package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Variable extends Term {
    static int index = 0;
    boolean body;
    boolean existential;
    boolean marked;

    final static String DONT_CARE = "*";

    static Map<String, Variable> variables = new HashMap<>();

    private Variable(String name) {
        this.label = name;
    }

    public static Variable fetchVariable(String name) {
        if (!variables.containsKey(name)) variables.put(name, new Variable(name));
        return variables.get(name);
    }

    public static Variable fetchNewVariable() {
        index++;
        return fetchVariable("x_" + index);
    }

    public boolean isBody() {
        return body;
    }

    public boolean isExistential() {
        return existential;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setBody(boolean body) {
        this.body = body;
    }

    public void setExistential(boolean existential) {
        this.existential = existential;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean dontCare() {
        return label.equals(DONT_CARE);
    }

    public static Variable getDontCare() {
        return fetchVariable(DONT_CARE);
    }

    @Override
    public boolean equals(Object o) {
        return Objects.equals(o.toString(), this.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return label;
    }
}
