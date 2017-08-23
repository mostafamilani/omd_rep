package ca.mcmaster.mmilani.omd.datalog.primitives;

public class Position {
    int pos;
    Predicate predicate;

    public Position(int pos, Predicate predicate) {
        this.pos = pos;
        this.predicate = predicate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (pos != position.pos) return false;
        return predicate != null ? predicate.equals(position.predicate) : position.predicate == null;
    }

    @Override
    public int hashCode() {
        return Predicate.predicates.values().size() * predicate.hashCode() + pos;
    }
}
