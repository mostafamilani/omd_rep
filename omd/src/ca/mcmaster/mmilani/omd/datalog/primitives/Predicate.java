package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Predicate {
    public String name;
    public int arity;

    static Map<String, Predicate> predicates = new HashMap<>();

    public Predicate(String name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    public static Predicate fetch(String name, int arity) {
        if (!predicates.containsKey(name))
            predicates.put(name, new Predicate(name, arity));
        Predicate predicate = predicates.get(name);
        if (predicate.arity != arity)
            throw new RuntimeException("Invalid Arity!");
        return predicate;
    }

    public static int maxArity() {
        int max = Integer.MIN_VALUE;
        for (Predicate predicate : predicates.values()) {
            if (predicate.arity > max)
                max = predicate.arity;
        }
        return max;
    }

    @Override
    public String toString() {
        return name;
    }
}
