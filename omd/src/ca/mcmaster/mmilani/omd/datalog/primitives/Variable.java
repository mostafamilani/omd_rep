package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Variable extends Term {
    boolean body = false;
    boolean head = false;
    boolean existential;
    boolean marked;

    final static String DONT_CARE = "*";

    public Variable(String name) {
        this.label = name;
    }

    public boolean isBody() {
        return body;
    }

    public boolean isExistential() {
        return existential;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setBody() {
        this.body = true;
    }

    public void setExistential(boolean existential) {
        this.existential = existential;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public boolean dontCare() {
        return label.equals(DONT_CARE);
    }

    public boolean isHead() {
        return head;
    }

    public void setHead() {
        this.head = true;
    }

    @Override
    public boolean equals(Object o) {
        return Objects.equals(o.toString(), this.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return label;
    }
}
