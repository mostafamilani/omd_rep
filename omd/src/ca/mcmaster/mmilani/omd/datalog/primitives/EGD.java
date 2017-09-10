package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.Program;

public class EGD extends Rule<Conjunct, EqualityAtom> {
    @Override
    public void addProgram(Program program) {
        program.egds.add(this);
    }

    @Override
    public String toString() {
        return head + ":-" + body;
    }
}
