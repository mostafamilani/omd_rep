package ca.mcmaster.mmilani.omd.datalog.tools.syntax;

import ca.mcmaster.mmilani.omd.datalog.Assignment;
import ca.mcmaster.mmilani.omd.datalog.primitives.Term;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AssignmentGadget {
    private static boolean compatibleWith(Assignment a1, Assignment a2) {
        for (Term t : a1.getMappings().keySet()) {
            if (a2.getMappings().containsKey(t) && a1.getMappings().get(t) != a2.getMappings().get(t))
                return false;
        }
        return true;
    }

    private static boolean areCompatible(Assignment a1, Assignment a2) {
        return compatibleWith(a1, a2) && compatibleWith(a2, a1);
    }

    public static boolean areCompatible(Assignment a, Set<Assignment> assignments) {
        for (Assignment assignment : assignments) {
            if (areCompatible(assignment, a)) return false;
        }
        return true;
    }

    public static Assignment merge(Assignment a1, Assignment a2) {
        Assignment assignment = (Assignment) a1.clone();
        for (Term t : a2.getMappings().keySet()) {
            assignment.put(t, a2.getMappings().get(t));
        }
        assignment.setLevel(Math.max(a1.getLevel(), a2.getLevel())) ;
        return assignment;
    }

    public static Set<Assignment> merge(Set<Assignment> e1, Set<Assignment> e2) {
        Set<Assignment> e = new HashSet<>();
        for (Assignment a1 : e1) {
            for (Assignment a2 : e2) {
                if (areCompatible(a1, a2)) {
                    Assignment res = (Assignment) a1.clone();
                    e.add(merge(res, a2));
                }
            }
        }
        return e;
    }
}
