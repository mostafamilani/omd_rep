package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MagicSetRewriter {
    public static Program rewrite(Program program, CQ cq) {
        Set<Predicate> newAdorned = adornedQueryPredicates(cq);
        Set<Predicate> adorned = new HashSet<>();
        while (!newAdorned.isEmpty()) {
            adorned.addAll(newAdorned);
            newAdorned.clear();
            for (Predicate predicate : adorned) {
                TGD adornedRules = getAdornedRules(predicate);
            }
        }
        return null;
    }

    private static TGD getAdornedRules(Predicate adornedPredicate) {

        return null;
    }

    public static Set<Predicate> adornedQueryPredicates(CQ cq) {
        HashSet<Predicate> predicates = new HashSet<>();
        for (PositiveAtom atom : cq.body.getAtoms()) {
            predicates.add(adornedPredicate(atom));
        }
        return predicates;
    }

    private static Predicate adornedPredicate(PositiveAtom atom) {
        StringBuilder adornment = new StringBuilder();
        for (Term term : atom.terms) {
            if (term instanceof Variable) {
                adornment.append("f");
            } else if (term instanceof Constant) {
                adornment.append("b");
            }
        }
        return Predicate.fetchAdornedPredicate(atom.predicate, adornment);
    }
}
