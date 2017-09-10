package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Null extends Term {
    public static int INDEX = 1;
    public boolean frozen = false;
    public boolean confirmed = false;
    public int index = 0;
    private static Map<String, Null> nulls = new HashMap<>();
    public Set<Atom> atoms = new HashSet<>();

    private Null(String label, int index) {
        this.label = label; this.index = index;
    }

    public static Null invent() {
        String label = "z_" + INDEX;
        Null n = new Null(label, INDEX);
        INDEX++;
        nulls.put(label, n);
        return n;
    }

    public void remove() {
        if (confirmed)
            return;
        INDEX = index;
        nulls.remove(label);
    }

    static Null fetch(String s) {
        if (!nulls.containsKey(s))
            throw new RuntimeException("Invalid null label (" + s + ")");
        return nulls.get(s);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Null && toString().equals(o.toString());
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
