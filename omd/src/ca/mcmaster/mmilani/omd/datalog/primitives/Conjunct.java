package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Conjunct {
    private List<PositiveAtom> atoms = new ArrayList<>();

    public Conjunct(Conjunct conjunct, PositiveAtom toRemove) {
        atoms = new ArrayList<>(conjunct.getAtoms());
        atoms.remove(toRemove);
    }

    public Conjunct() {
    }

    @Override
    public String toString() {
        if (atoms.isEmpty()) return "";
        StringBuilder s = new StringBuilder();
        for (Atom atom : atoms) {
            s.append(atom).append(",");
        }
        return s.substring(0, s.length()-1);
    }

    public Map<Predicate, Set<PositiveAtom>> getPredicates() {
        HashMap<Predicate, Set<PositiveAtom>> atoms = new HashMap<>();
        for (PositiveAtom atom : this.atoms) {
            if (!atoms.containsKey(atom.predicate)) atoms.put(atom.predicate, new HashSet<>());
            atoms.get(atom.predicate).add(atom);
        }
        return atoms;
    }

    public List<PositiveAtom> getAtoms() {
        return Collections.unmodifiableList(atoms);
    }

    public void add(PositiveAtom atom) {
        for (PositiveAtom positiveAtom : atoms) {
            if (Atom.equalsMasked(atom, positiveAtom))
                return;
        }
        atoms.add(atom);
        sort();
    }

    public void addAll(List<PositiveAtom> atomsToAdd) {
        for (PositiveAtom atom : atomsToAdd) {
            add(atom);
        }
        sort();
    }

    void sort() {
        Collections.sort(atoms, new Comparator<PositiveAtom>() {
            @Override
            public int compare(PositiveAtom o1, PositiveAtom o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
    }

    public Set<Variable> getVariables() {
        HashSet<Variable> variables = new HashSet<>();
        for (PositiveAtom atom : atoms) {
            variables.addAll(atom.getVariables());
        }
        return variables;
    }

    public void addFirst(PositiveAtom atom) {
        atoms.add(0, atom);
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
