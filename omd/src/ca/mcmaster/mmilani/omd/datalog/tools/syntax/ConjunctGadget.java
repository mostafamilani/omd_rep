package ca.mcmaster.mmilani.omd.datalog.tools.syntax;

import ca.mcmaster.mmilani.omd.datalog.Assignment;
import ca.mcmaster.mmilani.omd.datalog.Parser;
import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.util.*;

public class ConjunctGadget {
    public static boolean equal(Conjunct c1, Conjunct c2) {
//        System.out.println(c1 + "=" + c2 + "?");
        if (c1.getAtoms().size() != c2.getAtoms().size()) {
            return false;
        }
        if (!haveSamePredicates(c1, c2)) {
            return false;
        }
        if (c1 == c2 || c1.toString().equals(c2.toString())) {
            return true;
        }
        return mapTo(c1, c2) && mapTo(c2, c1);
    }

    public static boolean mapTo(Conjunct c1, Conjunct c2) {
        Set<Assignment> assignments = generateAllAssignments(c1.getVariables(), c2.getVariables());
        for (Assignment assignment : assignments) {
            if (equates(assignment, c1,c2)) return true;
        }
        return false;
    }

    private static boolean equates(Assignment assignment, Conjunct c1, Conjunct c2) {
        for (PositiveAtom atom : c1.getAtoms()) {
            PositiveAtom apply = atom.apply(assignment);
            boolean covered = false;
            for (PositiveAtom next : c2.getAtoms()) {
                if (Atom.equalsMasked(apply, next)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) return false;
        }
//        String s = c2.toString();
//        for (PositiveAtom atom : c1.getAtoms()) {
//            if (!s.contains(atom.apply(assignment).toString())) return false;
//        }
        return true;
    }

    private static Set<Assignment> generateAllAssignments(Set<Variable> vs1, Set<Variable> vs2) {
        HashSet<Assignment> assignments = new HashSet<>();
        if (vs1.size() == 1 || vs2.size() == 1) {
            for (Variable v1 : vs1) {
                for (Variable v2 : vs2) {
                    Assignment e = new Assignment();
                    e.put(v1, v2);
                    assignments.add(e);
                }
            }
        } else {
            for (Variable v1 : vs1) {
                for (Variable v2 : vs2) {
                    HashSet<Variable> nvs1 = new HashSet<>(vs1);
                    nvs1.remove(v1);
                    HashSet<Variable> nvs2 = new HashSet<>(vs2);
                    nvs2.remove(v2);
                    Set<Assignment> aset = generateAllAssignments(nvs1, nvs2);
                    for (Assignment assignment : aset) {
                        assignment.put(v1,v2);
                    }
                    assignments.addAll(aset);
                }
            }
        }
        return assignments;
    }

    private static boolean haveSamePredicates(Conjunct c1, Conjunct c2) {
        Map<Predicate, Set<PositiveAtom>> cp1s = c1.getPredicates();
        Map<Predicate, Set<PositiveAtom>> cp2s = c2.getPredicates();
        Set<Predicate> ps1 = cp1s.keySet();
        Set<Predicate> ps2 = cp2s.keySet();
        if (!ps1.containsAll(ps2) || !ps2.containsAll(ps1)) return false;
        for (Predicate p1 : cp1s.keySet()) {
            if (!cp2s.containsKey(p1)) return false;
            if (cp2s.get(p1).size() != cp1s.get(p1).size()) return false;
        }
        return true;
    }

    public static void main(String[] args) {
//        Scanner reader = new Scanner(System.in);
//        boolean done = false;
//        while(!done) {
//            System.out.println("c1 : ");
//            String s1 = reader.next();
            String s1 = "p(x,y)";
//            System.out.println("c2 : ");
            String s2 = "p(*,b),p(a,*)";
//            String s2 = reader.next();
            CQ q1 = new CQ();
            CQ q2 = new CQ();
            Conjunct c1 = Parser.parseConjunct(s1, true, q1);
            Conjunct c2 = Parser.parseConjunct(s2, true, q2);
            boolean b1 = mapTo(c1, c2);
            boolean b2 = mapTo(c2, c1);
            System.out.println(c1 + " -> " + c2 + " : " + b1);
            System.out.println(c2 + " -> " + c1 + " : " + b2);
//            done = reader.next().equals("q");
//        }

    }
}
