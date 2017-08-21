package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Rule {
    public List<Atom> body;
    public Atom head;
    public Map<String, Variable> variables = new HashMap<>();
    public Set<Variable> existentials = new HashSet<>();

    public Term fetchVariable(String name) {
        if (!variables.containsKey(name)) {
            variables.put(name, new Variable(name, this));
        }
            return variables.get(name);
    }

    @Override
    public String toString() {
        String s = "";
        for (Atom atom : body) {
            s += atom + ",";
        }
        s = s.substring(0, s.length()-1);
        if (head != null) {
            s = head + ":-" + s;
        } else {
            if (this instanceof Query)
                s += s + "?";
            else
                s = "!:-" + s;

        }
        return s;
    }

    public boolean isTGD() {
        return head != null && !(head instanceof EqulityAtom);
    }

    public boolean isEGD() {
        return head != null && head instanceof EqulityAtom;
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
