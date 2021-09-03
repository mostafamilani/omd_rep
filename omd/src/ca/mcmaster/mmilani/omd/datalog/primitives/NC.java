package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;

public class NC extends Rule<Conjunct, FalseAtom> {
    @Override
    public void addProgram(Program program) {
        program.ncs.add(this);
    }

    public Variable fetchVariable(String s, boolean body) {
        boolean existential = false;
        if (!variables.containsKey(s)) {
            variables.put(s, fetchNewVariable());
            if (!body)
                existential = true;
        }
        Variable variable = variables.get(s);
        if (body)
            variable.setBody();
        else
            variable.setHead();
        variable.setExistential(existential);
        return variable;
    }

    @Override
    public String toString() {
        return ":-" + body;
    }
}
