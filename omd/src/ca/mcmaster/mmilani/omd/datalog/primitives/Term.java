package ca.mcmaster.mmilani.omd.datalog.primitives;

public abstract class Term {

    public static Term parse(String s, Rule... rule) {
        Term term = null;
        if (s.contains("'")) {
            term = Constant.fetch(s.replaceAll("'", ""));
        } else {
            term = rule[0].fetchVariable(s);
        }
        return term;
    }


}
