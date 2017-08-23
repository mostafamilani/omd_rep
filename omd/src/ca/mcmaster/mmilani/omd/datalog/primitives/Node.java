package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Node {
    public Position p;
    public Set<Node> nexts = new HashSet<Node>();
    public Set<Node> nextSpecials = new HashSet<Node>();
    public static Map<Position, Node> nodes = new HashMap<Position, Node>();

    private Node(Position position) {
        this.p = position;
    }

    public static Node fetchNode(Position position) {
        if (!nodes.containsKey(position)) nodes.put(position, new Node(position));
        return nodes.get(position);
    }
}
