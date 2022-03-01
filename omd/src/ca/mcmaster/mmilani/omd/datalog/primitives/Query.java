package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Query<B> extends Rule<B, Atom> {
    public List<Variable> headVariables = new ArrayList<>();
    @Override
    public String toString() {
        return head.toString() + "?-" + body.toString();
    }

    public void addProgram(Program program) {

    }
}
