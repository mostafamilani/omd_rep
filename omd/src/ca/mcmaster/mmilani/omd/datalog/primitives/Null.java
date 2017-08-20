package ca.mcmaster.mmilani.omd.datalog.primitives;

public class Null extends Term {
    int index;
    @Override
    public String toString() {
        return "z_" + index;
    }
}
