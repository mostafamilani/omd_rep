package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.Constant;
import ca.mcmaster.mmilani.omd.datalog.primitives.Term;
import ca.mcmaster.mmilani.omd.datalog.primitives.Variable;

import java.util.*;

public class Answer {
    Map<Variable, Constant> mappings = new HashMap<>();

    public boolean map(Term tq, Term tf) {
        if (tq instanceof Constant) return tq == tf;
        if (mappings.containsKey(tq) && mappings.get(tq) != tf) {
            return false;
        } else {
            mappings.put((Variable) tq, (Constant) tf);
            return true;
        }
    }

    @Override
    public String toString() {
        String s = "";
        for (Variable variable : mappings.keySet()) {
            s += variable.name + " -> " + mappings.get(variable) + "\n";
        }
        return s + "\n";
    }

    public static boolean compatible(Answer a1, Answer a2) {
        for (Variable variable : a1.mappings.keySet()) {
            if (a2.mappings.containsKey(variable) && a2.mappings.get(variable) == a1.mappings.get(variable))
                return false;
        }
        return true;
    }

    public static Answer merge(Answer a1, Answer a2) {
        Answer a = new Answer();
        for (Variable v : a1.mappings.keySet()) {
            a.mappings.put(v, a1.mappings.get(v));
        }
        for (Variable v : a2.mappings.keySet()) {
            a.mappings.put(v, a2.mappings.get(v));
        }
        return a;
    }
}
