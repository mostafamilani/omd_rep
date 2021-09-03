package ca.mcmaster.mmilani.omd.datalog.tools.syntax;

import ca.mcmaster.mmilani.omd.datalog.primitives.Assignment;
import ca.mcmaster.mmilani.omd.datalog.primitives.Atom;
import ca.mcmaster.mmilani.omd.datalog.primitives.Term;

public class AtomGadget {
    public static Assignment mapTo(Atom a, Atom b) {
        if (b.predicate != a.predicate) return null;
        Assignment answer = new Assignment();
        for (int i = 0; i < b.terms.toArray().length; i++) {
            Term tf = (Term) b.terms.toArray()[i];
            Term tq = (Term) a.terms.toArray()[i];
            if (!answer.tryToMap(tq, tf))
                return null;
        }
        return answer;
    }
}
