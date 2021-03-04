package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;
import ca.mcmaster.mmilani.omd.datalog.tools.syntax.AtomGadget;
import ca.mcmaster.mmilani.omd.datalog.tools.syntax.ConjunctGadget;

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
        Set<Conjunct> newSubqueries = new HashSet<>();
        newSubqueries.add(q.body);
        while(!newSubqueries.isEmpty()) {
            System.out.println("New iteration");
            ucq.body.addAll(newSubqueries);
            newSubqueries.clear();
            for (Conjunct conjunct : ucq.body) {
                for (TGD tgd : rules) {
                    Set<Conjunct> cs = match(tgd,conjunct);
                    for (Conjunct newConjunct : cs) {
                        if (!contains(ucq, newConjunct) && newConjunct.getVariables().containsAll(ucq.head.getVariables())) {
                            newSubqueries.add(newConjunct);
                            System.out.println("newConjunct = " + newConjunct);
                        }
                    }
                }
            }
        }
        ucq.headVariables = ucq.head.getVariables();
        return ucq;
    }

    private static boolean contains(UCQ ucq, Conjunct conjunct) {
        for (Conjunct next : ucq.body) {
            if (ConjunctGadget.mapTo(next, conjunct))
                return true;
        }
        return false;
    }

    private static Set<Conjunct> match(TGD tgd, Conjunct conjunct) {
        HashSet<Conjunct> cs = new HashSet<>();
        for (Atom atom : conjunct.getAtoms()) {
            Assignment assignment  = AtomGadget.mapTo(tgd.head.getAtoms().get(0), atom);
            if (assignment != null) {
                for (Variable variable : tgd.variables.values()) {
                    if (!assignment.getMappings().containsKey(variable)) assignment.put(variable, Variable.getDontCare());
                }
                cs.add(generateConjunct(conjunct, tgd, assignment));
            }
        }
        return cs;
    }

    private static Conjunct generateConjunct(Conjunct conjunct, TGD tgd, Assignment assignment) {
        List<PositiveAtom> bodyAtoms = new ArrayList<>();
        for (PositiveAtom atom : tgd.body.getAtoms()) {
            bodyAtoms.add(atom.apply(assignment));
        }
        Conjunct newConjunct = new Conjunct();
        PositiveAtom head = tgd.head.getAtoms().get(0).apply(assignment);
        for (PositiveAtom atom : conjunct.getAtoms()) {
            PositiveAtom toAdd = atom.apply(assignment);
            if (head.equals(toAdd)) {
                newConjunct.addAll(bodyAtoms);
            } else {
                newConjunct.add(toAdd);
            }
        }
        return newConjunct;
    }

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\Mostafa\\Desktop\\omd_prj\\omd_rep\\omd\\rewrite.txt");
        Program p = Parser.parseProgram(file);
        List<CQ> queries = Parser.parseQueries(file);

        for (Query query : queries) {
            System.out.println("query = " + query);
            UCQ ucq = rewrite((CQ) query, p.tgds);
            System.out.println("ucq = " + ucq);
            Set<Assignment> evaluate = p.edb.evaluate(ucq);
            for (Assignment assignment : evaluate) {
                System.out.println("assignment = " + assignment);
            }
        }
    }
}
