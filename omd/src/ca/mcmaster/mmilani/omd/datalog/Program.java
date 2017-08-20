package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Program {
    Set<Rule> rules;
    public Database edb = new Database();

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
            if (rule.isEGD()) {
                applyEGD(rule, a);
            }
            if (rule.isNC()) {
                applyNC(rule);
            }
            if (rule.isTGD()) {
                Fact at = generate(rule.head, a);
                if (!idb.facts.contains(at)) {
                    idb.facts.add(at);
                    newAtom = true;
                }
            }
        }
        return newAtom;
    }

    private void applyNC(Rule rule) {
        Query q = new Query();
        q.body = rule.body;
        if (!idb.evaluate(q).isEmpty())
            throw new RuntimeException("Chase failure! NC  (" + rule + ") does not hold!");
    }

    private void applyEGD(Rule rule, Answer a) {
        EqulityAtom eatom = (EqulityAtom) rule.head;
        if (!a.mappings.containsKey(eatom.t1) || !a.mappings.containsKey(eatom.t2))
            throw new RuntimeException("Syntax error! Invalid egds (" + rule + ")");
        Constant c1 = a.mappings.get(eatom.t1);
        Constant c2 = a.mappings.get(eatom.t2);
        if (c1 != c2)
            throw new RuntimeException("Chase failure! Egd  (" + rule + ") does not hold!");
    }

    private Fact generate(Atom atom, Answer answer) {
        ArrayList<Term> terms = new ArrayList<>();
        for (Term term : atom.terms) {
            if (term instanceof Constant)
                terms.add(term);
            else if (term instanceof Variable) {
                if (answer.mappings.containsKey(term))
                    terms.add(answer.mappings.get(term));
                else
                    terms.add(Null.invent());
            }
        }
        return Fact.fetch(atom.predicate, terms);
    }
}
