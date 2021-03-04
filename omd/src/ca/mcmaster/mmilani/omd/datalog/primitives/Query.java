package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.Program;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public abstract class Query<B> extends Rule<B, Atom> {
    public Set<Variable> headVariables = new HashSet<>();
    @Override
    public String toString() {
        return head.toString() + "?-" + body.toString();
    }

    public void addProgram(Program program) {

    }
}
