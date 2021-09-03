package ca.mcmaster.mmilani.omd.datalog.primitives;

public class ApplicablePair {
    public Assignment assignment;
    public Rule rule;

    public ApplicablePair(Rule rule, Assignment assignment) {
        this.rule = rule;
        this.assignment = assignment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicablePair that = (ApplicablePair) o;

        return (assignment != null ? assignment.equals(that.assignment) : that.assignment == null) && (rule != null ? rule.equals(that.rule) : that.rule == null);
    }

    @Override
    public int hashCode() {
        int result = assignment != null ? assignment.hashCode() : 0;
        result = 31 * result + (rule != null ? rule.hashCode() : 0);
        return result;
    }
}
