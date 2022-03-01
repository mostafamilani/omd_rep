package ca.mcmaster.mmilani.omd.datalog.engine;

import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.primitives.*;
import ca.mcmaster.mmilani.omd.datalog.tools.syntax.AssignmentGadget;
import ca.mcmaster.mmilani.omd.datalog.tools.syntax.AtomGadget;

import java.util.*;
import java.util.function.UnaryOperator;

public class InMemoryDatabase extends Database {
    public Map<String, Fact> facts = new HashMap<>();

    @Override
    public String toString() {
        if (facts.isEmpty()) return "";
        StringBuilder s = new StringBuilder();
        for (Fact fact : facts.values()) {
            s.append(fact).append(", ");
        }
        return s.substring(0, s.length()-2);
    }

    Set<Assignment> evaluate(Conjunct c) {
        Set<Assignment> answers = new HashSet<>();
        Assignment dummy = new Assignment();
        answers.add(dummy);
        for (Atom atom : c.getAtoms()) {
            Set<Assignment> partialAnswers = new HashSet<>();
            for (Fact fact : facts.values()) {
                Assignment partial = AtomGadget.mapTo(atom, fact);
                if (partial!=null) {
                    partialAnswers.add(partial);
                    partial.level = fact.level;
                }
            }
            answers = AssignmentGadget.merge(partialAnswers, answers);
        }
        return answers;
    }

    public Set<Assignment> evaluate(Query q) {
        Set<Conjunct> cs = new HashSet<>();
        if (q instanceof CQ) cs.add(((CQ)q).body);
        else if (q instanceof UCQ) cs.addAll(((UCQ)q).body);

        HashSet<Assignment> evaluations = new HashSet<>();
        for (Conjunct conjunct : cs) {
            Set<Assignment> evs = evaluate(conjunct);
            for (Assignment e : evs) {
                Assignment filter = filter(e, new HashSet(q.headVariables));
                if (filter != null) evaluations.add(filter);
            }
        }
        return evaluations;
    }

    private Assignment filter(Assignment e, Set variables) {
        Assignment evaluation = new Assignment();
        for (Term term : e.getMappings().keySet()) {
            if (variables.contains(term)) {
                Term mapTo = e.getMappings().get(term);
                if (!(mapTo instanceof Constant)) return null;
                evaluation.put(term, mapTo);
            }
        }
        return evaluation;
    }

    InMemoryDatabase copy() {
        InMemoryDatabase database = new InMemoryDatabase();
        database.facts = new HashMap<>();
        for (String key : facts.keySet()) {
            database.facts.put(key, facts.get(key));
        }
        database.program = program;
        return database;
    }

    @Override
    public Fact[] getFacts() {
        Object[] array = facts.values().toArray();
        return Arrays.copyOf(array, array.length, Fact[].class);
    }

    @Override
    public Set<Assignment> evaluate(UCQ ucq) {
        return null;
    }

    public boolean isEmpty() {
        return facts.isEmpty();
    }

    public Fact fetch(String s) {
        if (!facts.containsKey(s))
            facts.put(s, (Fact) Parser.parse(s, false, program));
        return facts.get(s);
    }

    public Fact addFact(Predicate predicate, List<Term> terms, int level) {
        Fact f = new Fact(predicate, terms);
        String s = f.toString();
        if (!facts.containsKey(s)) {
            facts.put(s, f);
        }
        Fact fact = facts.get(s);
        fact.level = level;
        return fact;
    }

    @Override
    public boolean isEmpty(Predicate predicate) {
        for (Fact fact : facts.values()) {
            if (fact.predicate == predicate)
                return false;
        }
        return !recordCount.containsKey(predicate.name) || recordCount.get(predicate.name) <= 0;
    }

    public void addFact(Fact fact) {
        if (!facts.containsKey(fact.toString())) {
            facts.put(fact.toString(), fact);
            if (!recordCount.containsKey(fact.predicate.name))
                recordCount.put(fact.predicate.name, 0);
            int count = recordCount.get(fact.predicate.name) + 1;
            recordCount.put(fact.predicate.name, count);
        }
    }

    public void checkNullChange(Null n, Term t) {
        Map<String, Fact> updatedFacts = new HashMap<String, Fact>();
        for (Fact fact : facts.values()) {
            final boolean[] changed = {false};
            String key = fact.toString();
            fact.terms.replaceAll(new UnaryOperator<Term>() {
                @Override
                public Term apply(Term term) {
                    if (term == n){
                        changed[0] = true;
                        return t;
                    } else
                        return term;
                }
            });
            if (changed[0]) {
                updatedFacts.put(key, fact);
            }
        }
        for (String key : updatedFacts.keySet()) {
            facts.remove(key);
            Fact newFact = updatedFacts.get(key);
            facts.put(newFact.toString(), newFact);
        }
    }
}
