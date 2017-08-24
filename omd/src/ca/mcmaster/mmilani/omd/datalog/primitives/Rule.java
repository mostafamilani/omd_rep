package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Rule {
    public List<Atom> body;
    public Atom head;
    public Map<String, Variable> variables = new HashMap<>();
    public Set<Variable> existentials = new HashSet<>();
    public Set<Variable> headVariables = new HashSet<>();

    public Term fetchVariable(String name, boolean body) {
        boolean existential = false;
        if (!body && !variables.containsKey(name)) existential = true;
        if (!variables.containsKey(name)) {
            variables.put(name, new Variable(name, this));
        }
        if (!body) {
            headVariables.add(variables.get(name));
            if (existential) {
                existentials.add(variables.get(name));
            }
        }
        return variables.get(name);
    }

    @Override
    public String toString() {
        String s = "";
        for (Atom atom : body) {
            s += atom + ",";
        }
        s = s.substring(0, s.length() - 1);
        if (head != null) {
            s = head + ":-" + s;
        } else {
            if (this instanceof Query)
                s += s + "?";
            else
                s = ":-" + s;

        }
        return s;
    }

    public boolean isTGD() {
        return head != null && !(head instanceof EqualityAtom);
    }

    public boolean isEGD() {
        return head != null && head instanceof EqualityAtom;
    }

    public boolean isNC() {
        return head == null && !(this instanceof Query);
    }

    @Override
    public boolean equals(Object o) {
        return (this + "").equals(((Rule) o) + "");
    }

    @Override
    public int hashCode() {
        return (this + "").hashCode();
    }
}
