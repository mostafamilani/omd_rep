package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.Program;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TGD extends Rule<Conjunct, Conjunct> {
    public Set<Variable> existentialVars = new HashSet<>();

    @Override
    public void addProgram(Program program) {
        program.tgds.add(this);
    }

    public Variable fetchVariable(String s, boolean body) {
        if (!variables.containsKey(s)) {
            variables.put(s, Variable.fetchNewVariable());
        }
        Variable variable = variables.get(s);
        if (body)
            variable.setBody();
        else
            variable.setHead();
        if (!variable.isBody() && variable.isHead())
            variable.setExistential(true);
        return variable;
    }

    @Override
    public String toString() {
        return head + ":-" + body;
    }

}
