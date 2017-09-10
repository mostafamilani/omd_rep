package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;
import java.util.function.UnaryOperator;

public class Fact extends PositiveAtom {
    static public Map<String, Fact> facts = new HashMap<>();
    public int level = Integer.MAX_VALUE;

    private Fact(Predicate p, List<Term> ts) {
        super(p, ts);
    }

    static Fact fetch(String s) {
        if (!facts.containsKey(s))
            facts.put(s, (Fact) Atom.parse(s, false));
        return facts.get(s);
    }

    public static Fact addFact(Predicate predicate, List<Term> terms, int level) {
        Fact f = new Fact(predicate, terms);
        String s = f.toString();
        if (!facts.containsKey(s)) {
            facts.put(s, f);
        }
        Fact fact = facts.get(s);
        fact.level = level;
        return fact;
    }

    public static void checkNullChange(Null n, Term t) {
        Map<String, Fact> updatedFacts = new HashMap<String, Fact>();
        for (Fact fact : facts.values()) {
            final boolean[] changed = {false};
            String key = fact.toString();
            fact.terms.replaceAll(new UnaryOperator<Term>() {
                @Override
                public Term apply(Term term) {
                    if (term == n){
                        changed[0] = true;
                        return t;
                    } else
                        return term;
                }
            });
            if (changed[0]) {
                updatedFacts.put(key, fact);
            }
        }
        for (String key : updatedFacts.keySet()) {
            facts.remove(key);
            Fact newFact = updatedFacts.get(key);
            facts.put(newFact.toString(), newFact);
        }
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
