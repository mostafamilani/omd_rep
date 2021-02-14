package ca.mcmaster.mmilani.omd.datalog.primitives;

public class Position {
    private static final int MAX_PREDICATE_COUNT = 1000;
    int pos;
    public Predicate predicate;

    public Position(int pos, Predicate predicate) {
        this.pos = pos;
        this.predicate = predicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;
        return this.predicate == position.predicate && this.pos == position.pos;
    }

    @Override
    public int hashCode() {
        return MAX_PREDICATE_COUNT * predicate.hashCode() + pos;
    }

    @Override
    public String toString() {
        return predicate + "[" + pos + "]";
    }
}
