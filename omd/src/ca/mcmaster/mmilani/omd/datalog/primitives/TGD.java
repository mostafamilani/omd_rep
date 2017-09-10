package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.Program;

import java.util.HashSet;
import java.util.Set;

public class TGD extends Rule<Conjunct, Atom> {
    public Set<Variable> existentialVars = new HashSet<>();

    @Override
    Variable fetchVariable(String name, boolean body) {
        boolean existential = false;
        if (!body && !variables.containsKey(name)) existential = true;
        Variable v = super.fetchVariable(name, body);
        if (existential) existentialVars.add(v);
        return v;
    }

    @Override
    public void addProgram(Program program) {
        program.tgds.add(this);
    }

    @Override
    public String toString() {
        return head + ":-" + body;
    }
}
