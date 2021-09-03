package ca.mcmaster.mmilani.omd.datalog.primitives;

//import ca.mcmaster.mmilani.omd.datalog.engine.Program;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;

import java.util.*;

public abstract class Rule<B,H> {
    public B body;
    public H head;
    public Program program;

    public Map<String, Variable> variables = new HashMap<>();
    int index = 0;

    public Variable fetchVariable(String name) {
        if (!variables.containsKey(name)) variables.put(name, new Variable(name));
        return variables.get(name);
    }

    public Variable fetchNewVariable() {
        index++;
        return fetchVariable("x" + index/*Integer.toHexString(index)*/);
    }

    public abstract void addProgram(Program program);

    public abstract Variable fetchVariable(String s, boolean body);

    public Variable getDontCare() {
        return fetchVariable(Variable.DONT_CARE);
    }
}
