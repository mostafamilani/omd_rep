package ca.mcmaster.mmilani.omd.datalog.primitives;

import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.engine.Program;

public abstract class Term {
    public String label;
    public static Term parse(String s, boolean body, Program program, Rule... owner) {
        Term term;
        s = Parser.sanitizePredicateName(s);
        if (s.startsWith("z_")) {
            term = Null.fetch(s);
        } else if (s.contains("'") || owner == null || owner.length == 0) {
            term = program.schema.fetchConstant(s.replaceAll("'", "\""));
        } else if (s.equals(Variable.DONT_CARE)) {
            term = owner[0].getDontCare();
        } else {
            term = owner[0].fetchVariable(s, body);
        }
        return term;
    }

    public String toString() {
        return label;
    }
}
