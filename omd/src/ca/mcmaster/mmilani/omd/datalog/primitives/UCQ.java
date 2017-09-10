package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashSet;
import java.util.Set;

public class UCQ extends Query<Set<Conjunct>> {
    public UCQ() {
        body = new HashSet<>();
    }
}
