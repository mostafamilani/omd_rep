package ca.mcmaster.mmilani.omd.datalog.primitives;

public abstract class Term {
    String label;
    static Term parse(String s, boolean body, Rule... owner) {
        Term term;
        if (s.startsWith("z_")) {
            term = Null.fetch(s);
        } else if (s.contains("'")) {
            term = Constant.fetch(s.replaceAll("'", ""));
        } else {
            if (owner == null || owner.length == 0)
                throw new RuntimeException(s);
            term = owner[0].fetchVariable(s, body);
        }
        return term;
    }

    public String toString() {
        return label;
    }
}
