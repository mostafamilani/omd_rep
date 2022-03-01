package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.*;

public class Node {
    public Position p;
//    public Set<Node> nexts = new HashSet<Node>();
//    public Set<Node> nextSpecials = new HashSet<Node>();
    public Set<Edge> nexts = new HashSet<>();
//    public Set<Node> prNodes = new HashSet<>();
    public int index = -1;
    public boolean onStack;
    public int lowLink;

    public Node(Position position) {
        this.p = position;
    }

    public Edge nextEdge(Node node) {
        for (Edge edge : nexts) {
            if (edge.destination.equals(node))
                return edge;
        }
        return null;
    }

    public boolean isNext(Node node) {
        return nextEdge(node) != null;
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

    public boolean addNext(Node next, TGD rule, boolean special) {
        Edge edge = nextEdge(next);
        if (edge == null) {
            edge = new Edge();
//            edge.source = this;
            edge.destination = next;
        }
//        edge.tgds.add(rule);
        edge.special = edge.special || special;
        boolean newAdded = false;
        if (!nexts.contains(edge)) {
            nexts.add(edge);
//            next.prNodes.add(this);
            newAdded = true;
        }
//            System.out.println(edge + " added!");
//        } else {
//            System.out.println(edge + " is already added!");
//        }
        return newAdded;
    }

    public Set<Node> nextNodes(boolean reverse) {
//        if (reverse)
//            return prNodes;
        HashSet<Node> nodes = new HashSet<>();
        for (Edge next : nexts) {
            nodes.add(next.destination);
        }
        return nodes;
    }

    public Set<Node> nextSpecialNodes() {
        HashSet<Node> nodes = new HashSet<>();
        for (Edge next : nexts) {
            if (next.special)
                nodes.add(next.destination);
        }
        return nodes;
    }
}
