package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.List;

public class PositiveAtom extends Atom {

    public PositiveAtom(Predicate p, List<Term> ts) {
        super(p, ts);
    }
}
