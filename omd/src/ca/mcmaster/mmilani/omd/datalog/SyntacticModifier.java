package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SyntacticModifier {
    public static Program rewriteToSticky(Program vs) {
        return null;
    }

    public static UCQ rewrite(CQ q, Set<TGD> rules) {
        UCQ ucq = new UCQ();
        ucq.head = q.head;
        ucq.body.add(q.body);
        boolean newSubquery = true;
        while(newSubquery) {
            newSubquery = false;
            for (Conjunct conjunct : ucq.body) {
                for (TGD tgd : rules) {
                    Set<Conjunct> cs = match(tgd,conjunct);
                    for (Conjunct newConjunct : cs) {
                        if (!contain(ucq.body,newConjunct)) {
                            ucq.body.add(newConjunct);
                        }
                    }
                }
            }
        }
        return ucq;
    }

    private static boolean contain(Set<Conjunct> conjuncts, Conjunct conjunct) {
        for (Conjunct c : conjuncts) {
            if (!unify(c, conjunct).isEmpty())
                return true;
        }
        return false;
    }

    private static Set<Assignment> unify(Conjunct c1, Conjunct c2) {
        HashSet<Assignment> assignments = new HashSet<>();
        if (c1.atoms.size() != c2.atoms.size()) return assignments;
        Map<Predicate, Set<Atom>> atoms1 = c1.getPredicates();
        Map<Predicate, Set<Atom>> atoms2 = c2.getPredicates();
        if (!atoms1.keySet().containsAll(atoms2.keySet()) || !atoms2.keySet().containsAll(atoms1.keySet())) return assignments;
        for (Predicate predicate : atoms1.keySet()) {
            Assignment assignment = unify(atoms1.get(predicate), atoms2.get(predicate), new Assignment());
            if (assignment == null)
                return assignments;
            else
                assignments.add(assignment);
        }
        return assignments;
    }

    private static Assignment unify(Set<Atom> atoms1, Set<Atom> atoms2, Assignment assignment) {
        for (Atom a1 : atoms1) {
            for (Atom a2 : atoms2) {
                Assignment unify = unify(a1, a2);
                if (unify != null && assignment.compatible(unify)) {
                    assignment.merge(unify);
                    Set<Atom> sub1 = subset(atoms1, a1);
                    Set<Atom> sub2 = subset(atoms1, a2);
                    Assignment subunify = unify(sub1, sub2, assignment);
                    if (subunify != null) {
                        return subunify;
                    }
                }
            }
        }
        return null;
    }

    private static Set<Atom> subset(Set<Atom> atoms, Atom a) {
        HashSet<Atom> res = new HashSet<>();
        for (Atom atom : atoms) {
            if (atom != a)
                res.add(atom);
        }
        return res;
    }

    private static Set<Conjunct> match(TGD tgd, Conjunct conjunct) {
        HashSet<Conjunct> cs = new HashSet<>();
        for (Atom atom : conjunct.atoms) {
            Assignment assignment  = unify(atom, (Atom) tgd.head);
            if (assignment != null) {
                cs.add(generateConjunct(conjunct, tgd, assignment));
            }
        }
        return cs;
    }

    private static Conjunct generateConjunct(Conjunct conjunct, TGD tgd, Assignment assignment) {
        List<Atom> atoms = new ArrayList<>();
        Assignment a = (Assignment) assignment.clone();
        for (Atom atom : tgd.body.atoms) {
            ArrayList<Term> ts = new ArrayList<>();
            for (Term term : atom.terms) {
                if (a.mappings.containsKey(term)) {a.mappings.put(term, Variable.getNewQueryVariable());}
                ts.add(a.mappings.get(term));

            }
            atoms.add(new PositiveAtom(atom.predicate, ts));
        }
        Conjunct newConjunct = new Conjunct();
        for (Atom atom : conjunct.atoms) {
            if (!atom.equals(tgd.head)) {
                newConjunct.atoms.add(atom);
            }
            else {
                newConjunct.atoms.addAll(atoms);
            }
        }
        return null;
    }

    static Assignment unify(Atom a, Atom b) {
        if (b.predicate != a.predicate) return null;
        Assignment answer = new Assignment();
        for (int i = 0; i < b.terms.toArray().length; i++) {
            Term tf = (Term) b.terms.toArray()[i];
            Term tq = (Term) a.terms.toArray()[i];
            if (!answer.map(tq, tf))
                return null;
        }
        return answer;
    }

    static Set<Assignment> merge(Set<Assignment> e1, Set<Assignment> e2) {
        Set<Assignment> e = new HashSet<>();
        for (Assignment a1 : e1) {
            for (Assignment a2 : e2) {
                if (a1.compatible(a2)) {
                    Assignment res = (Assignment) a1.clone();
                    res.merge(a2);
                    e.add(res);
                }
            }
        }
        return e;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\Mostafa\\Desktop\\omd_prj\\omd_rep\\omd\\rewrite.txt");
        Program p = Parser.parseProgram(file);
        List<CQ> queries = Parser.parseQueries(file);

        for (Query query : queries) {
            System.out.println("query = " + query);
            UCQ ucq = rewrite((CQ) query, p.tgds);
            Set<Assignment> evaluate = p.edb.evaluate(ucq);
            for (Assignment assignment : evaluate) {
                System.out.println("assignment = " + assignment);
            }
        }
    }
}
