package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.UnaryOperator;

public class Program {
    public Set<TGD> tgds = new HashSet<>();
    public Set<EGD> egds = new HashSet<>();
    public Set<NC> ncs = new HashSet<>();
    Database edb = new Database();
    private Set<ApplicablePair> applicables = new HashSet<>();
    private Set<ApplicablePair> applieds = new HashSet<>();
    private Set<ApplicablePair> blockeds = new HashSet<>();

    private Database idb;

    public Set<Assignment> evaluate(Query q) {
        return idb.evaluate(q);
    }

    public void chase() {
        idb = edb.copy();
        System.out.println("edb:");
        System.out.println(idb);
        for (int i = 0; i <= Predicate.maxArity(); i++) {
            boolean newAtom = true;
            while (newAtom) {
                newAtom = applyNextPair();
                applyNCs();
                applyEGDs();
            }
            resume();
        }
        System.out.println("idb:");
        System.out.println(idb);
    }

    private boolean applyNextPair() {
        findApplicablePairs();
        if (applicables.isEmpty()) {
            return false;
        }
        boolean newAtom = false;
        List<ApplicablePair> list = new ArrayList<>();
        list.addAll(applicables);
        list.sort((o1, o2) -> o1.assignment.level - o2.assignment.level);
        ApplicablePair pair = list.get(0);

        Fact at = generate((Atom) pair.rule.head, pair.assignment);
        applicables.remove(pair);
        if (checkAddition(at)) {
            idb.facts.add(at);
            applieds.add(pair);
            confirmNullInvention(at);
            System.out.println(at);
            newAtom = true;
        } else {
            blockeds.add(pair);
            rollbackNullInvention(at);
        }
        return newAtom;
    }

    private void findApplicablePairs() {
        if (!applicables.isEmpty())
            return;
        for (Rule rule : tgds) {
            if (rule instanceof TGD) {
                Set<Assignment> evaluates = idb.evaluate(((TGD) rule).body);
                for (Assignment assignment : evaluates) {
                    ApplicablePair pair = new ApplicablePair(rule, assignment);
                    if (!applieds.contains(pair) && !blockeds.contains(pair))
                        applicables.add(pair);
                }
            }
        }
    }

    private void resume() {
        blockeds.clear();
        Null.freezeAll();
    }

    private void applyNCs() {
        for (Rule rule : ncs) {
            if (rule instanceof NC) applyNC(rule);
        }
    }

    private void applyEGDs() {
        for (EGD rule : egds) {
            if (rule instanceof EGD) applyEGD(rule);
        }
    }

    private void confirmNullInvention(Fact fact) {
        for (Term term : fact.terms) {
            if (term instanceof Null) {
                Null n = (Null) term;
                n.confirmed = true;
            }
        }
    }

    private void rollbackNullInvention(Fact fact) {
        for (Term term : fact.terms) {
            if (term instanceof Null) {
                Null n = (Null) term;
                if (!n.confirmed) {
                    n.remove();
                }
            }
        }
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
        Map<Term, Term> u = new HashMap<>();
        for (int i = 0; i < a1.terms.size(); i++) {
            Term t1 = a1.terms.get(i);
            Term t2 = a2.terms.get(i);
            if (t1.equals(t2))
                continue;
            if (t1 instanceof Constant)
                return false;
            if (t1 instanceof Null && ((Null) t1).frozen)
                return false;
            if (!u.containsKey(t1)) u.put(t1, t2);
            if (!u.get(t1).equals(t2))
                return false;
        }
        return true;
    }

    private void applyNC(Rule rule) {
        CQ q = new CQ();
        q.body = (Conjunct) rule.body;
        if (!idb.evaluate(q).isEmpty())
            throw new RuntimeException("Chase failure! NC  (" + rule + ") does not hold!");
    }

    private void applyEGD(Rule rule) {
        CQ q = new CQ();
        q.body = (Conjunct) rule.body;
        EqualityAtom eatom = (EqualityAtom) rule.head;
        Set<Assignment> evaluates = idb.evaluate(q);
        for (Assignment a : evaluates) {
            if (!a.mappings.containsKey(eatom.t1) || !a.mappings.containsKey(eatom.t2))
                throw new RuntimeException("Syntax error! Invalid egds (" + rule + ")");
            Term c1 = a.mappings.get(eatom.t1);
            Term c2 = a.mappings.get(eatom.t2);
            if (c1 == c2)
                return;
            if (c1 instanceof Constant && c2 instanceof Constant)
                throw new RuntimeException("Chase failure! Egd  (" + rule + ") does not hold!");
            Null n;
            Term t;
            if (c1 instanceof Null) {
                n = (Null) c1;
                t = c2;
            } else if (c2 instanceof Null) {
                n = (Null) c2;
                t = c1;
            } else {
                throw new RuntimeException("Equality values are invalid!");
            }
            replaceWith(n,t);
        }
    }

    private void replaceWith(Null n, Term t) {
        for (Atom atom : n.atoms) {
            atom.terms.replaceAll(new UnaryOperator<Term>() {
                @Override
                public Term apply(Term term) {
                    if (term == n)
                        return t;
                    return term;
                }
            });
        }
        Fact.checkNullChange(n, t);
    }

    private Fact generate(Atom atom, Assignment answer) {
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
        Fact fact = Fact.addFact(atom.predicate, terms, answer.level + 1);
        for (Null n : nulls) {
            n.atoms.add(fact);
        }
        return fact;
    }
}
