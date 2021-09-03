package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Fact extends PositiveAtom {
    public int level = Integer.MAX_VALUE;

    public Fact(Predicate p, List<Term> ts) {
        super(p, ts);
    }



    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }
}
