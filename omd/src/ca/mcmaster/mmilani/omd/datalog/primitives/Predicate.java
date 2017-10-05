package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Predicate {
    public String name;
    public int arity;

    static Map<String, Predicate> predicates = new HashMap<>();

    private Predicate(String name, int arity) {
        this.name = name;
        this.arity = arity;
    }

    static Predicate fetch(String name, int arity) {
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

    public Predicate fetchAdornedPredicate(String adornment) {
        if (isAdorned())
            return null;
        return fetch(name + "^" + adornment, arity);
    }

    private boolean isAdorned() {
        return name.contains("^");
    }

    public Predicate fetchSimplePredicate() {
        if (!isAdorned())
            return null;
        return fetch(name.substring(0, name.indexOf("^")), arity);
    }

    public Set<Predicate> allAdorned() {
        HashSet<Predicate> result = new HashSet<>();
        for (Predicate predicate : predicates.values()) {
            if (predicate.name.contains(name + "^")) {
                result.add(predicate);
            }
        }
        return result;
    }

    public String getAdornment() {
        if (!isAdorned())
            return null;
        return name.substring(name.indexOf("^") + 1, name.length());
    }
}
