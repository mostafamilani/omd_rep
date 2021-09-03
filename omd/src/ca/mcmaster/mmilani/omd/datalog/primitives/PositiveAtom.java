package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.ArrayList;
import java.util.List;

public class PositiveAtom extends Atom {

    public PositiveAtom(Predicate p, List<Term> ts) {
        super(p, ts);
    }

    public PositiveAtom apply(Assignment assignment) {
        ArrayList<Term> ts = new ArrayList<>();
        for (Term term : terms) {
            if (term instanceof Constant)
                ts.add(term);
            else if (term instanceof Variable) {
                ts.add(assignment.getMappings().getOrDefault(term, term));
            }
        }
        return new PositiveAtom(predicate, ts);
    }
}
