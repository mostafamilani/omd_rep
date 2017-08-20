package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.Iterator;

public class Query extends Rule {

    @Override
    public String toString() {
        String s = "";
        for (Atom atom : body) {
            s += atom + ",";
        }
        return s.substring(0, s.length()-1);
    }
}
