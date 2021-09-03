package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.Arrays;

public class Range {
    int[] values;
    boolean[] open;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Range range = (Range) o;
        return Arrays.equals(values, range.values) && Arrays.equals(open, range.open);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(values);
        result = 31 * result + Arrays.hashCode(open);
        return result;
    }
}
