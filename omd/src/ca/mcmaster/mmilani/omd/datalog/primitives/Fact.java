package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.List;

public class Fact extends PositiveAtom {
    public Fact(Predicate p, List<Term> ts) {
        super(p, ts);
    }
}
