package ca.mcmaster.mmilani.omd.datalog.primitives;

public class CQ extends Query<Conjunct> {
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
        variable.setBody(body);
        variable.setExistential(existential);
        return variable;
    }
}
