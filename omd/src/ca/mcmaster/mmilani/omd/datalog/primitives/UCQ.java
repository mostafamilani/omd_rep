package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class UCQ extends Query<Set<Conjunct>> {
    public UCQ() {
        body = new HashSet<>();
    }

    @Override
    public String toString() {
        StringBuilder bodystr = new StringBuilder();
        for (Conjunct conjunct : body) {
            bodystr.append("[").append(conjunct).append("]\n");
        }
        return head + "?-" + bodystr;
    }

    public Variable fetchVariable(String s, boolean body) {
        boolean existential = false;
        if (!variables.containsKey(s)) {
            variables.put(s, Variable.fetchNewVariable());
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
