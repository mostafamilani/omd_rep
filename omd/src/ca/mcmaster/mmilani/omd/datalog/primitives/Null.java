package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Null extends Term {
    static int index = 0;
    String label;
    private static Map<String, Null> nulls = new HashMap<>();
    public Set<Atom> atoms = new HashSet<>();

    @Override
    public String toString() {
        return label;
    }

    private Null(String label) {
        this.label = label;
    }

    public static Null invent() {
        index++;
        String label = "z_" + index;
        Null n = new Null(label);
        nulls.put(label, n);
        return n;
    }

    public static Null fetch(String s) {
        if (!nulls.containsKey(s))
            throw new RuntimeException("Invalid null label (" + s + ")");
        return nulls.get(s);
    }

    @Override
    public boolean equals(Object o) {
        return toString().equals(o.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
