package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PositiveAtom extends Atom {

    public PositiveAtom(Predicate p, List<Term> ts) {
        super(p, ts);
    }

    @Override
    protected Object clone() {
        ArrayList<Term> ts = new ArrayList<>();
        for (Term term : terms) {
            if (term instanceof Constant)
                ts.add(term);
            else if (term instanceof Variable) {
                ts.add(new Variable(term.label, null));
            }
        }
        PositiveAtom atom = new PositiveAtom(predicate, ts);
        return atom;
    }
}
