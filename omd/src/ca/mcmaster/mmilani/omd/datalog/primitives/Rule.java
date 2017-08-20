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
}
