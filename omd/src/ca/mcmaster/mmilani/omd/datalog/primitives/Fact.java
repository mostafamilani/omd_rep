package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Fact extends PositiveAtom {
    static Map<String, Fact> facts = new HashMap<>();

    public Fact(Predicate p, List<Term> ts) {
        super(p, ts);
    }

    static Fact fetch(String s) {
        s = s.replaceAll(" ", "");
        if (!facts.containsKey(s))
            facts.put(s, (Fact) Atom.parse(s));
        return facts.get(s);
    }

    public static Fact fetch(Predicate predicate, ArrayList<Term> terms) {
        String s = "";
        s += predicate.name + "(";
        for (Term next : terms) {
            s += next + ",";
        }
        return fetch(s.substring(0, s.length()-1) + ")");
    }
}
