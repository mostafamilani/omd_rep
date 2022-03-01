package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public abstract class Atom {
    public Predicate predicate;
    public List<Term> terms;

    private List<Variable> variables;

    public Atom() {
    }

    public Atom(Predicate predicate, List<Term> terms) {
        this.predicate = predicate;
        this.terms = terms;
    }

    public static boolean isFact(List<Term> terms) {
        for (Term next : terms) {
            if (!(next instanceof Constant))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String name = predicate.name;
        if (!Character.isLetter(name.charAt(0)) || Character.isUpperCase(name.charAt(0)))
            name = "p" + name;
        StringBuilder s = new StringBuilder(name + "(");
        for (Term term : terms) {
            if (term instanceof Variable) {
                s.append(term.toString().toUpperCase()).append(",");
            } else
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

    public List<Variable> getVariables() {
        if (variables == null)
            setVariables();
        return variables;
    }

    public void setVariables() {
        variables = new ArrayList<>();
        for (Term term : terms) {
            if (term instanceof Variable && !variables.contains(term))
                variables.add((Variable) term);
        }
    }
}
