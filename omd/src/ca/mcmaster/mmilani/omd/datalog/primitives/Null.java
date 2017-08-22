package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Null extends Term {
    public static int INDEX = 0;
    public boolean frozen = false;
    public boolean confirmed = false;
    public int index = 0;
    String label;
    public static Map<String, Null> nulls = new HashMap<>();
    public Set<Atom> atoms = new HashSet<>();

    @Override
    public String toString() {
        return label;
    }

    private Null(String label) {
        this.label = label;
    }

    public static Null invent() {
        INDEX++;
        String label = "z_" + INDEX;
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

    public static void freezeAll() {
        for (Null next : nulls.values()) {
            next.frozen = true;
        }
    }
}
