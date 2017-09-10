package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.Program;

public class NC extends Rule<Conjunct, FalseAtom> {
    @Override
    public void addProgram(Program program) {
        program.ncs.add(this);
    }

    @Override
    public String toString() {
        return ":-" + body;
    }
}
