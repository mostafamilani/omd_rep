package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Node {
    public Position p;
    public Set<Node> nexts = new HashSet<Node>();
    public Set<Node> nextSpecials = new HashSet<Node>();

    public Node(Position position) {
        this.p = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(p, node.p);
    }

    @Override
    public int hashCode() {
        return Objects.hash(p);
    }
}
