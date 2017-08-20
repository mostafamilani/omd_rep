package ca.mcmaster.mmilani.omd.datalog.primitives;

public class EqulityAtom extends BuiltIn {
    Term t1,t2;

    public EqulityAtom(Term t1, Term t2) {
        this.t1 = t1;
        this.t2 = t2;
    }
}
