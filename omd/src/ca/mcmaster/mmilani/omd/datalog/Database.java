package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.util.*;

public class Database {
    @Override
    public String toString() {
        if (facts.isEmpty()) return "";
        StringBuilder s = new StringBuilder();
        for (Fact fact : facts) {
            s.append(fact).append(", ");
        }
        return s.substring(0, s.length()-2);
    }

    Set<Fact> facts = new HashSet<>();

    Set<Assignment> evaluate(Conjunct c) {
        Set<Assignment> answers = new HashSet<>();
        Assignment dummy = new Assignment();
        answers.add(dummy);
        for (Atom atom : c.atoms) {
            Set<Assignment> partialAnswers = new HashSet<>();
            for (Fact fact : facts) {
                Assignment partial = SyntacticModifier.unify(atom, fact);
                if (partial!=null) {
                    partialAnswers.add(partial);
                    partial.level = fact.level;
                }
            }
            answers = SyntacticModifier.merge(partialAnswers, answers);
        }
        return answers;
    }

    Set<Assignment> evaluate(Query q) {
        Set<Conjunct> cs = new HashSet<>();
        if (q instanceof CQ) cs.add(((CQ)q).body);
        else if (q instanceof UCQ) cs.addAll(((UCQ)q).body);

        HashSet<Assignment> evaluations = new HashSet<>();
        for (Conjunct conjunct : cs) {
            Set<Assignment> evs = evaluate(conjunct);
            for (Assignment e : evs) {
                evaluations.add(filter(e, q.headVariables));
            }
        }
        return evaluations;
    }

    private Assignment filter(Assignment e, Set variables) {
        Assignment evaluation = new Assignment();
        for (Term term : e.mappings.keySet()) {
            if (variables.contains(term))
                evaluation.mappings.put(term, e.mappings.get(term));
        }
        return evaluation;
    }

    Database copy() {
        Database database = new Database();
        database.facts = new HashSet<>();
        database.facts.addAll(facts);
        return database;
    }
}
