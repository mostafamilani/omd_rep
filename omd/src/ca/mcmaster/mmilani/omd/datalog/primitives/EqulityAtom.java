package ca.mcmaster.mmilani.omd.datalog.primitives;

public class EqulityAtom extends BuiltIn {
    public Term t1;
    public Term t2;

    public EqulityAtom(Term t1, Term t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public String toString() {
        return t1 + "=" + t2;
    }
}
