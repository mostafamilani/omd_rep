package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.util.*;

public class Database {
    Set<Fact> facts = new HashSet<Fact>();

    public Set<Answer> evaluate(Query q) {
        Set<Answer> answers = new HashSet<>();
        for (Atom qatom : q.body) {
            Set<Answer> partialAnswers = new HashSet<>();
            for (Fact fact : facts) {
                Answer partial = unify(q.head, fact);
                if (partial != null) {
                    partialAnswers.addAll(merge(answers, partial));
                }
            }
        }
        return answers;
    }

    private Set<Answer> merge(Set<Answer> answers, Answer answer) {
        HashSet<Answer> ranswers = new HashSet<>();
        for (Answer a : answers) {
            if (Answer.compatible(a, answer)) {
                ranswers.add(Answer.merge(a, answer));
            }
        }
        return ranswers;
    }

    private Answer unify(Atom q, Fact f) {
        if (f.predicate != q.predicate) return null;
        Answer answer = new Answer();
        for (int i = 0; i < f.terms.toArray().length; i++) {
            Term tf = (Term) f.terms.toArray()[i];
            Term tq = (Term) q.terms.toArray()[i];
            if (!answer.map(tq, tf))
                return null;
        }
        return answer;
    }
}
