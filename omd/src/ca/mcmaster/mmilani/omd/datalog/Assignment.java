package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.Constant;
import ca.mcmaster.mmilani.omd.datalog.primitives.Term;
import ca.mcmaster.mmilani.omd.datalog.primitives.Variable;

import java.util.*;

public class Assignment {
    Map<Term, Term> mappings = new HashMap<>();
    int level = 0;

    boolean map(Term tq, Term tf) {
        if (tq instanceof Constant) return tq == tf;
        if (mappings.containsKey(tq) && mappings.get(tq) != tf) {
            return false;
        } else {
            mappings.put((Variable) tq, tf);
            return true;
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Term term : mappings.keySet()) {
            s.append(term).append(" -> ").append(mappings.get(term)).append(", ");
        }
        return s + "\n";
    }

    public boolean compatible(Assignment a) {
        for (Term t : a.mappings.keySet()) {
            if (this.mappings.containsKey(t) && this.mappings.get(t) != a.mappings.get(t)) return false;
        }
        return true;
    }

    public void merge(Assignment a) {
//        if (!compatible(a)) return;
        for (Term t : a.mappings.keySet()) {
            this.mappings.put(t, a.mappings.get(t));
        }
        this.level = Math.max(this.level, a.level);
    }

    @Override
    protected Object clone() {
        Assignment assignment = new Assignment();
        assignment.level = this.level;
        assignment.mappings = new HashMap<>();
        for (Term key : this.mappings.keySet()) {
            assignment.mappings.put(key, this.mappings.get(key));
        }
        return assignment;
    }

    @Override
    public boolean equals(Object o) {
        Assignment v = (Assignment) o;
        if (v == null || !v.mappings.keySet().equals(this.mappings.keySet()))
            return false;
        for (Term key : v.mappings.keySet()) {
            if (!v.mappings.get(key).equals(mappings.get(key)))
                return false;
        }
        return true;

    }

    @Override
    public int hashCode() {
        return (this + "").hashCode();
    }
}
