package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public abstract class Atom {
    public Predicate predicate;
    public List<Term> terms;

    public Atom() {
    }

    public Atom(Predicate predicate, List<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    public static Atom parse(String s, boolean body, Rule... rule) {
        StringTokenizer t = new StringTokenizer(s, "(,=)");
        Atom atom = null;
        if (s.contains("=")) {
            atom = new EqualityAtom(Term.parse(t.nextToken(), body, rule), Term.parse(t.nextToken(), body, rule));
        } else {
            String pname = t.nextToken();
            List<Term> ts = new ArrayList<>();
            while (t.hasMoreTokens()) {
                Term term = Term.parse(t.nextToken(), body, rule);
                ts.add(term);
            }
            Predicate p = Predicate.fetch(pname, ts.size());
            if (isFact(ts))
                atom = Fact.addFact(p, ts, 0);
            else
                atom = new PositiveAtom(p, ts);
        }
        return atom;
    }

    private static boolean isFact(List<Term> terms) {
        for (Term next : terms) {
            if (!(next instanceof Constant))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(predicate.name + "(");
        for (Term term : terms) {
            s.append(term).append(",");
        }
        return s.substring(0, s.length() - 1) + ")";
    }

    @Override
    public boolean equals(Object obj) {
        return ("" + obj).equals(this + "");
    }

    @Override
    public int hashCode() {
        return (this + "").hashCode();
    }

    public static boolean equalsMasked(Atom a1, Atom a2) {
        if (a1.equals(a2)) return true;
        if (a1.predicate != a2.predicate || a1.terms.size() != a2.terms.size()) return false;
        for (int i = 0; i < a1.terms.size(); i++) {
            Term t1 = a1.terms.get(i);
            Term t2 = a2.terms.get(i);
            if (!t1.equals(t2)) {
                if ((t1 instanceof Variable && ((Variable) t1).dontCare()) ||
                        (t2 instanceof Variable && ((Variable) t2).dontCare()))
                    continue;
                return false;
            };
        }
        return true;
    }

    public Set<Variable> getVariables() {
        HashSet<Variable> variables = new HashSet<>();
        for (Term term : terms) {
            if (term instanceof Variable)
                variables.add((Variable) term);
        }
        return variables;
    }
}
