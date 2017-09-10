package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.Program;

import java.util.*;

public abstract class Rule<B,H> {
    public B body;
    public H head;
    public Map<String, Variable> variables = new HashMap<>();
    public Set<Variable> headVariables = new HashSet<>();

    Variable fetchVariable(String name, boolean body) {
        if (!variables.containsKey(name)) {
            variables.put(name, new Variable(name, this));
        }
        if (!body) {
            headVariables.add(variables.get(name));
        }
        return variables.get(name);
    }

    public abstract void addProgram(Program program);
}
