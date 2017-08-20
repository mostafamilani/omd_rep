package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

public abstract class Atom {
    public Predicate predicate;
    public List<Term> terms;

    public Atom() {
    }

    public Atom(Predicate predicate, List<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    public static Atom parse(String s, Rule... rule) {
        StringTokenizer t = new StringTokenizer(s, "(,=).");
        Atom atom = null;
        if (s.contains("=")) {
            atom = new EqulityAtom(Term.parse(t.nextToken(), rule), Term.parse(t.nextToken(), rule));
        } else {
            String pname = t.nextToken();
            List<Term> ts = new ArrayList<>();
            boolean fact = true;
            while(t.hasMoreTokens()) {
                Term term = Term.parse(t.nextToken(), rule);
                ts.add(term);
                if (term instanceof Variable)
                    fact = false;
            }
            Predicate p = Predicate.fetch(pname, ts.size());
            if (fact)
                atom = new Fact(p, ts);
            else
                atom = new PositiveAtom(p, ts);
        }
        return atom;
    }

    @Override
    public String toString() {
        String s = predicate.name + "(";
        for (Term term : terms) {
            s += term + ",";
        }
        return s.substring(0, s.length()-1) + ")";
    }
}
