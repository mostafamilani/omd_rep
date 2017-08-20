package ca.mcmaster.mmilani.omd.datalog.primitives;

public class Null extends Term {
    static int index = 0;
    @Override
    public String toString() {
        return "z_" + index;
    }

    private Null(String label) {
    }

    public static Null invent() {
        index++;
        return new Null("z_" + index);
    }
}
