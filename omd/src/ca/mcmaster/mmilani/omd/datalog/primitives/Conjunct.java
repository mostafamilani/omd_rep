package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Conjunct {
    public List<Atom> atoms = new ArrayList<>();

    @Override
    public String toString() {
        if (atoms.isEmpty()) return "";
        StringBuilder s = new StringBuilder();
        for (Atom atom : atoms) {
            s.append(atom).append(",");
        }
        return s.substring(0, s.length()-1);
    }

    public Map<Predicate, Set<Atom>> getPredicates() {
        HashMap<Predicate, Set<Atom>> atoms = new HashMap<>();
        for (Atom atom : this.atoms) {
            if (atoms.containsKey(atom.predicate)) atoms.put(atom.predicate, new HashSet<>());
            atoms.get(atom.predicate).add(atom);
        }
        return atoms;
    }

    /*@Override
    public Object clone() {
        Conjunct c = new Conjunct();
        for (Atom atom : atoms) {
            c.atoms.add((Atom) ((PositiveAtom)atom).clone());
        }
        return c;
    }*/
}
