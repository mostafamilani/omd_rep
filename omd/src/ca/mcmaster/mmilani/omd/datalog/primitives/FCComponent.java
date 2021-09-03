package ca.mcmaster.mmilani.omd.datalog.primitives;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class FCComponent {
    public boolean special = false;
    public Set<Node> members = new HashSet<>();
}
