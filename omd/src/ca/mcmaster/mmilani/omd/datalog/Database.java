package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.util.*;

public class Database {
    @Override
    public String toString() {
        if (facts.isEmpty()) return "";
        String s = "";
        for (Fact fact : facts) {
            s += fact + ", ";
        }
        return s.substring(0, s.length()-2);
    }

    Set<Fact> facts = new HashSet<Fact>();

    public Set<Answer> evaluate(Query q) {
        Set<Answer> answers = new HashSet<>();
        Answer dummy = new Answer();
        answers.add(dummy);
        for (Atom qatom : q.body) {
            Set<Answer> partialAnswers = new HashSet<>();
            for (Fact fact : facts) {
                Answer partial = unify(qatom, fact);
                if (partial!=null) partialAnswers.add(partial);
            }
            answers = merge(partialAnswers, answers);
        }
        return answers;
    }

    private Set<Answer> merge(Set<Answer> as1, Set<Answer> as2) {
        HashSet<Answer> ans = new HashSet<>();
        for (Answer a1 : as1) {
            for (Answer a2 : as2) {
                Answer merge = Answer.merge(a1, a2);
                if (merge != null) {
                    ans.add(merge);
                }
            }
        }
        return ans;
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

    protected Database copy() {
        Database database = new Database();
        database.facts = new HashSet<Fact>();
        for (Fact fact : facts) {
            database.facts.add(fact);
        }
        return database;
    }
}
