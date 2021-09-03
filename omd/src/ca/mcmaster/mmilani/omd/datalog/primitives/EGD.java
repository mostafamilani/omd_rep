package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;

import java.util.HashSet;
import java.util.Set;

public class EGD extends Rule<Conjunct, EqualityAtom> {
    public Set<Variable> headVariables = new HashSet<>();

    @Override
    public void addProgram(Program program) {
        program.egds.add(this);
    }

    @Override
    public Variable fetchVariable(String s, boolean body) {
        boolean existential = false;
        if (!variables.containsKey(s)) {
            variables.put(s, fetchNewVariable());
            if (!body)
                existential = true;
        }
        Variable variable = variables.get(s);
        if (!body)
            headVariables.add(variable);
        if (body)
            variable.setBody();
        else
            variable.setHead();
        variable.setExistential(existential);
        return variable;
    }

    @Override
    public String toString() {
        return head + ":-" + body;
    }
}
