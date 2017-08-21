package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.util.*;
import java.util.function.UnaryOperator;

public class Program {
    Set<Rule> rules;
    public Database edb = new Database();
    Map<Rule,Set<Answer>> applieds = new HashMap<Rule, Set<Answer>>();

    Database idb;

    public Program() {
        rules = new HashSet<Rule>();
    }

    void init() {

    }

    public Set<Answer> evaluate(Query q) {
        return idb.evaluate(q);
    }

    public void chase() {
        idb = edb.copy();
        System.out.println("edb:");
        System.out.println(idb);
        for (int i = 0; i < Predicate.maxArity(); i++) {
            boolean newAtom = true;
            while (newAtom) {
                newAtom = false;
                for (Rule rule : rules) {
                    if (rule.isTGD() && fireRule(rule)) {
                        newAtom = true;
                    }
                    applyNCs();
                    applyEGDs();
                }
            }
            resume();
        }
        System.out.println("idb:");
        System.out.println(idb);
    }

    public void resume() {
        Null.freezeAll();
    }

    private void applyNCs() {
        for (Rule rule : rules) {
            if (rule.isNC()) fireRule(rule);
        }
    }

    private void applyEGDs() {
        for (Rule rule : rules) {
            if (rule.isEGD()) fireRule(rule);
        }
    }

    private boolean fireRule(Rule rule) {
        Query q = new Query();
        q.body = rule.body;
        Set<Answer> ans = idb.evaluate(q);
        boolean newAtom = false;
        for (Answer a : ans) {
            if (applied(rule, a)) continue;
            if (rule.isEGD()) {
                applyEGD(rule, a);
            }
            if (rule.isNC()) {
                applyNC(rule);
            }
            if (rule.isTGD()) {
                Fact at = generate(rule.head, a);
                addApplied(rule,a);
                if (checkAddition(at)) {
                    idb.facts.add(at);
                    System.out.println(at);
                    newAtom = true;
                }
            }
        }
        return newAtom;
    }

    private boolean checkAddition(Fact a) {
        for (Fact fact : idb.facts) {
            if (homomorphic(a, fact))
                return false;
        }
        return true;
    }

    private boolean homomorphic(Fact a1, Fact a2) {
        if (!a1.predicate.equals(a2.predicate))
            return false;
        Map<Term,Term> u = new HashMap<>();
        for (int i = 0; i < a1.terms.size(); i++) {
            Term t1 = a1.terms.get(i);
            Term t2 = a2.terms.get(i);
            if (t1.equals(t2))
                continue;
            if (t1 instanceof Constant)
                return false;
            if (t1 instanceof Null && ((Null)t1).frozen)
                return false;
            if (!u.containsKey(t1)) u.put(t1,t2);
            if (!u.get(t1).equals(t2))
                return false;
        }
        return true;
    }

    private void addApplied(Rule rule, Answer answer) {
        if (!applieds.containsKey(rule))
            applieds.put(rule, new HashSet<Answer>());
        applieds.get(rule).add(answer);

    }

    private boolean applied(Rule rule, Answer answer) {
        return applieds.containsKey(rule) && applieds.get(rule).contains(answer);
    }

    private void applyNC(Rule rule) {
        Query q = new Query();
        q.body = rule.body;
        if (!idb.evaluate(q).isEmpty())
            throw new RuntimeException("Chase failure! NC  (" + rule + ") does not hold!");
    }

    private void applyEGD(Rule rule, Answer a) {
        EqualityAtom eatom = (EqualityAtom) rule.head;
        if (!a.mappings.containsKey(eatom.t1) || !a.mappings.containsKey(eatom.t2))
            throw new RuntimeException("Syntax error! Invalid egds (" + rule + ")");
        Term c1 = a.mappings.get(eatom.t1);
        Term c2 = a.mappings.get(eatom.t2);
        if (c1 == c2)
            return;
        if (c1 instanceof Constant && c2 instanceof Constant)
            throw new RuntimeException("Chase failure! Egd  (" + rule + ") does not hold!");
        throw new RuntimeException("Equating nulls! Not separable!");
//        Null n;
//        Term t;
//        if (c1 instanceof Null) {
//            n = (Null) c1;
//            t = c2;
//        } else if (c1 instanceof Null) {
//            n = (Null) c2;
//            t = c1;
//        } else {
//            throw new RuntimeException("Equality values are invalid!");
//        }
//        replaceWith(n,t);
    }

    private void replaceWith(Null n, Term t) {
        for (Atom atom : n.atoms) {
            Fact.facts.keySet().remove(atom.toString());
            atom.terms.replaceAll(new UnaryOperator<Term>() {
                @Override
                public Term apply(Term term) {
                    if (term == n)
                        return t;
                    return term;
                }
            });
            Fact.facts.put(atom.toString(), (Fact) atom);
        }
        checkAnomalies();
    }

    private void checkAnomalies() {
        Set<Fact> facts = new HashSet<>();
        for (Fact fact : idb.facts) {
            if (!Fact.facts.containsKey(fact.toString()))
                throw new RuntimeException("Ivalid fact in IDB! " + fact);
            else
                facts.add(Fact.facts.get(fact.toString()));
        }
        idb.facts = facts;
    }


    private Fact generate(Atom atom, Answer answer) {
        ArrayList<Term> terms = new ArrayList<>();
        Set<Null> nulls = new HashSet<>();
        for (Term term : atom.terms) {
            if (term instanceof Constant)
                terms.add(term);
            else if (term instanceof Variable) {
                if (answer.mappings.containsKey(term))
                    terms.add(answer.mappings.get(term));
                else {
                    Null n = Null.invent();
                    terms.add(n);
                    nulls.add(n);
                }
            }
        }
        Fact fact = Fact.addFact(atom.predicate, terms);
        for (Null n : nulls) {
            n.atoms.add(fact);
        }
        return fact;
    }
}
