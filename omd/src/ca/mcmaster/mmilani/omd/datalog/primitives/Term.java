package ca.mcmaster.mmilani.omd.datalog.primitives;

public abstract class Term {

    public static Term parse(String s, boolean body, Rule... rule) {
        Term term = null;
        if (s.startsWith("z_")) {
            term = Null.fetch(s);
        } else if (s.contains("'")) {
            term = Constant.fetch(s.replaceAll("'", ""));
        } else {
            if (rule == null || rule.length == 0)
                throw new RuntimeException(s);
            term = rule[0].fetchVariable(s, body);
        }
        return term;
    }


}
