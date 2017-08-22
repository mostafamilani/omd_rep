package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Fact extends PositiveAtom {
    static public Map<String, Fact> facts = new HashMap<>();

    private Fact(Predicate p, List<Term> ts) {
        super(p, ts);
    }

    static Fact fetch(String s) {
        if (!facts.containsKey(s))
            facts.put(s, (Fact) Atom.parse(s, false));
        return facts.get(s);
    }

    public static Fact addFact(Predicate predicate, List<Term> terms) {
        Fact f = new Fact(predicate, terms);
        String s = f.toString();
        if (!facts.containsKey(s)) {
            facts.put(s, f);
        }
        return facts.get(s);
    }
}
