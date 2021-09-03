package ca.mcmaster.mmilani.omd.datalog.tools.syntax;

import ca.mcmaster.mmilani.omd.datalog.primitives.FCComponent;
import ca.mcmaster.mmilani.omd.datalog.primitives.Node;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class SearchSetting {
    public Stack<Node> stack = new Stack<>();
    public int globalIndex = 0;
    public Set<FCComponent> components = new HashSet<>();
}
