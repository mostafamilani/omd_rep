package ca.mcmaster.mmilani.omd.datalog.primitives;

public class Variable extends Term {
    public String name;
    public Rule rule;
    public boolean marked;

    public Variable(String name, Rule rule) {
        this.name = name;
        this.rule = rule;
    }

    @Override
    public String toString() {
        return "" + name;
    }

    public boolean isBodyVariable() {
        return !rule.existentials.contains(this);
    }

    public boolean isExistentialVariable() {
        return rule.existentials.contains(this);
    }
}
