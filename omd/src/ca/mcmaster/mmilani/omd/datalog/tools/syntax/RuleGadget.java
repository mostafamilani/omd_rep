package ca.mcmaster.mmilani.omd.datalog.tools.syntax;

import ca.mcmaster.mmilani.omd.datalog.primitives.Atom;
import ca.mcmaster.mmilani.omd.datalog.primitives.TGD;
import ca.mcmaster.mmilani.omd.datalog.primitives.Variable;

import java.util.*;

public class RuleGadget {
    public static boolean equalTo(TGD r1, TGD r2) {
        List<Atom> atoms1 = getAtoms(r1);
        List<Atom> atoms2 = getAtoms(r2);
        if (atoms1.size() != atoms2.size())
            return false;
        Map<Variable, Variable> vars = new HashMap<>();
        for (int i = 0; i < atoms1.size(); i++) {
            Atom a1 = atoms1.get(i);
            Atom a2 = atoms2.get(i);
            if (a1.predicate != a2.predicate)
                return false;
            for (int j = 0; j < a1.terms.size(); j++) {
                Variable v1 = (Variable) a1.terms.get(j);
                Variable v2 = (Variable) a2.terms.get(j);
                if (vars.containsKey(v1) && vars.get(v1) != v2)
                    return false;
                vars.put(v1, v2);
            }
        }
        return true;
    }

    public static List<Atom> getAtoms(TGD rule) {
        ArrayList<Atom> atoms = new ArrayList<>();
        atoms.addAll(rule.head.getAtoms());
        atoms.addAll(rule.body.getAtoms());
        return atoms;
    }

    public static boolean contains(Set<TGD> rules, TGD rule) {
        for (TGD tgd : rules) {
            if (equalTo(tgd, rule))
                return true;
        }
        return false;
    }
}
