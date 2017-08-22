package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.Atom;
import ca.mcmaster.mmilani.omd.datalog.primitives.Rule;
import ca.mcmaster.mmilani.omd.datalog.primitives.Term;
import ca.mcmaster.mmilani.omd.datalog.primitives.Variable;

import java.util.Iterator;
import java.util.Set;

public class SyntacticAnalyzer {
    public static boolean isLinear(Set<Rule> rules) {
        for (Rule rule : rules) {
            if (!isLinear(rule))
                return false;
        }
        return true;
    }

    private static boolean isLinear(Rule rule) {
        return rule.body.size() == 1;
    }

    public static boolean isSticky(Set<Rule> rules) {
        findMarkedVariables(rules);
        for (Rule rule : rules) {
            for (Variable variable : rule.variables.values()) {
                if (isRepeated(variable) && variable.marked)
                    return false;
            }
        }
        return true;
    }

    private static void findMarkedVariables(Set<Rule> rules) {
        boolean newMarked = true;
        while (newMarked) {
            newMarked = false;
            for (Rule rule : rules) {
                for (Variable variable : rule.variables.values()) {
                    if (!rule.headVariables.contains(variable)) {
                        variable.marked = true;
                        newMarked = true;
                    }
                }
            }

        }
    }

    private static boolean isRepeated(Variable variable) {
        boolean appeared = false;
        for (Atom atom : variable.rule.body) {
            for (Term term : atom.terms) {
                if (variable.equals(term)) {
                    if (appeared)
                        return true;
                    else
                        appeared = true;
                }
            }
        }
        return false;
    }

}
