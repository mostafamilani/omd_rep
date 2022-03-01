package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TGD extends Rule<Conjunct, Conjunct> {
    public Set<Variable> existentialVars = new HashSet<>();

    @Override
    public void addProgram(Program program) {
        program.nExistential += existentialVars.size();
        program.tgds.add(this);
    }

    public Variable fetchVariable(String s, boolean body) {
        if (!variables.containsKey(s)) {
            variables.put(s, this.fetchVariable(s));
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

    public void initVars() {
        for (PositiveAtom atom : body.getAtoms()) {
            for (Term term : atom.terms) {
                Variable v = fetchVariable(term.label);
                v.body = true;
                variables.put(term.label, v);
            }
        }
        for (PositiveAtom atom : head.getAtoms()) {
            for (Term term : atom.terms) {
                Variable v = fetchVariable(term.label);
                v.head = true;
                variables.put(term.label, v);
            }
        }
    }

    public boolean isFrontier(Variable variable) {
        return head.getVariables().contains(variable);
    }

    @Override
    public String toString() {
        return head + ":-" + body;
    }

    public String predicateString() {
        return body.getAtoms().get(0).predicate.toString() + head.getAtoms().get(0).predicate.toString();
    }
}
