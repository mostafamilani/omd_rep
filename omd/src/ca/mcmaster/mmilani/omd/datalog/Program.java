package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.Atom;
import ca.mcmaster.mmilani.omd.datalog.primitives.Query;
import ca.mcmaster.mmilani.omd.datalog.primitives.Rule;

import java.io.File;
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
        return edb.evaluate(q);
    }

    public void chase() {
        boolean newAtom = true;
        while (newAtom) {
            newAtom = false;
            for (Rule rule : rules) {
                if (fireRule(rule))
                    newAtom = true;
            }
        }
    }

    private boolean fireRule(Rule rule) {
        for (Atom atom : rule.body) {
            Query q = new Query();
        }
        return false;
    }
}
