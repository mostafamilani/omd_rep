package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashSet;
import java.util.Set;

public class CQ extends Query<Conjunct> {
    public Set<Variable> headVariables = new HashSet<>();
    public Variable fetchVariable(String s, boolean body) {
        boolean existential = false;
        if (!variables.containsKey(s)) {
            variables.put(s, fetchNewVariable());
            if (!body)
                existential = true;
        }
        Variable variable = variables.get(s);
        if (!body)
            headVariables.add(variable);
        if (body)
            variable.setBody();
        else
            variable.setHead();
        variable.setExistential(existential);
        return variable;
    }
}
