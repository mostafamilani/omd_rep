package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.Program;

import java.util.HashSet;
import java.util.Set;

public class TGD extends Rule<Conjunct, Atom> {
    public Set<Variable> existentialVars = new HashSet<>();

    @Override
    public void addProgram(Program program) {
        program.tgds.add(this);
    }

    public Variable fetchVariable(String s, boolean body) {
        boolean existential = false;
        if (!variables.containsKey(s)) {
            variables.put(s, Variable.fetchNewVariable());
            if (!body)
                existential = true;
        }
        Variable variable = variables.get(s);
        if (!body)
            headVariables.add(variable);
        variable.setBody(body);
        variable.setExistential(existential);
        return variable;
    }

    @Override
    public String toString() {
        return head + ":-" + body;
    }
}
