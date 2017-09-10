package ca.mcmaster.mmilani.omd.datalog.primitives;

public class Variable extends Term {
    static int index = 0;

    public Rule rule;
    public boolean marked;

    Variable(String name, Rule rule) {
        this.label = name;
        this.rule = rule;
    }

    public boolean isBodyVariable() {
        return rule instanceof TGD && !rule.headVariables.contains(this);
    }

    public boolean isExistential() {
        return rule instanceof TGD && ((TGD)rule).existentialVars.contains(this);
    }

    public static Variable getNewQueryVariable() {
        index++;
        return new Variable("x_" + index, null);
    }
}
