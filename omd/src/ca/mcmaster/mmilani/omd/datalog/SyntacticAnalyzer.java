package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static ca.mcmaster.mmilani.omd.datalog.TerminationAnalyzer.terminates;

public class SyntacticAnalyzer {
    public static boolean isLinear(Set<TGD> rules) {
        for (TGD rule : rules) {
            if (!isLinear(rule))
                return false;
        }
        return true;
    }

    public static boolean isSimpleLinear(Set<TGD> rules) {
        for (TGD rule : rules) {
            if (!isLinear(rule)) {
//                System.out.println("Nonlinear rule: " + rule);
                return false;
            }
            if (hasRepeatedVariable(rule)) {
//                System.out.println("Rule with self join: " + rule);
                return false;
            }
        }
        return true;
    }

    public static boolean hasRepeatedVariable(TGD tgd) {
        for (Variable variable : tgd.variables.values()) {
            if (variable.isBody() && isRepeated(variable, tgd))
                return true;
        }
        return false;
    }

    public static boolean isLinear(TGD tgd) {
        return tgd.body.getAtoms().size() == 1;
    }

    public static boolean isSticky(Set<TGD> tgds) {
        for (TGD tgd : tgds) {
            for (Variable variable : tgd.variables.values()) {
                if (variable.isBody() && isRepeated(variable, tgd) && variable.isMarked()) {
//                    System.out.println("Non-sticky rule: " + tgd + " with a repeated marked variable: " + variable);
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isWeaklyAcyclic(Set<TGD> rules, Set<Position> infiniteRankPositions) {
        return infiniteRankPositions.isEmpty();
    }

    public static boolean isWeaklySticky(Set<TGD> tgds, Set<Position> infinitePositions) {
        for (TGD tgd : tgds) {
            for (Variable variable : tgd.variables.values()) {
                if (!variable.isBody()) continue;
                Set<Position> positions = getPositionsInConjunct(variable, tgd.body);
                if (isRepeated(variable, tgd) && variable.isMarked() && containsAll(infinitePositions, positions))
                    return false;
            }
        }
        return true;
    }

    public static boolean isGuarded(Set<TGD> tgds) {
        for (TGD tgd : tgds) {
            if (!hasGuard(tgd)) {
//                System.out.println("Rule without a guard atom: " + tgd);
                return false;
            }
        }
        return true;
    }

    public static boolean hasGuard(TGD tgd) {
        for (PositiveAtom atom : tgd.body.getAtoms()) {
            if (isGuard(atom, tgd.body)) return true;
        }
        return false;
    }

    public static boolean isGuard(PositiveAtom atom, Conjunct body) {
        Set<Variable> variables = new HashSet<>(body.getVariables());
        variables.removeAll(getVariables(atom));
        return variables.isEmpty();
    }

    public static Set<Variable> getVariables(PositiveAtom atom) {
        HashSet<Variable> variables = new HashSet<>();
        for (Term term : atom.terms) {
            if (term instanceof Variable)
                variables.add((Variable) term);
        }
        return variables;
    }

    public static Set<Node> getNodesInSpecialCycle(Map<Position, Node> dGraph, Program program) {
        FCComponent.globalIndex = 0;
        FCComponent.stack = new Stack<Node>();
        Set<FCComponent> components = new HashSet<>();
        for (Node node : dGraph.values()) {
            if (node.index == -1) strongConnect(node, components);
        }
        HashSet<Node> result = new HashSet<>();
        for (FCComponent component : components) {
            if (component.special) {
                result.addAll(component.members);
                program.nSpecialComponents++;
            }
        }
        program.nComponents = components.size();
        return result;
    }

    public static Set<Position> getPositions(Set<Node> nodes) {
        HashSet<Position> positions = new HashSet<>();
        for (Node node : nodes) {
            positions.add(node.p);
        }
        return positions;
    }

    public static void strongConnect(Node node, Set<FCComponent> components) {
        node.index = FCComponent.globalIndex;
        node.lowLink = FCComponent.globalIndex;
        FCComponent.globalIndex++;
        FCComponent.stack.push(node);
        node.onStack = true;

        Set<Node> nexts = new HashSet<>(node.nexts);
        nexts.addAll(node.nextSpecials);

        for (Node next : nexts) {
            boolean special = false;
            if (node.nextSpecials.contains(next))
                special = true;

            if (next.index == -1) {
                strongConnect(next, components);
                node.lowLink = Math.min(node.lowLink, next.lowLink);
            } else if (next.onStack){
                node.lowLink = Math.min(node.lowLink, next.index);
            }
            if (special) FCComponent.stack.push(null);
        }

        if (node.lowLink == node.index && (FCComponent.stack.peek() == null || (
                FCComponent.stack.peek() != node &&
                FCComponent.stack.peek().lowLink == node.lowLink))) {
            FCComponent component = new FCComponent();
            Node next;
            do {
                next = FCComponent.stack.pop();
                if (next == null) {
                    component.special = true;
                    continue;
                }
                next.onStack = false;
                if (node.lowLink == next.lowLink) {
                    component.members.add(next);
                }
            } while (node != next);
            components.add(component);
        }
    }

    public static Set<Node> getInfiniteRankNodes(Map<Position, Node> dGraph, Set<Node> nodesInSpecialCycle) {
        return findDescendants(nodesInSpecialCycle);
    }

    public static Set<Position> getInfiniteRankPositions(Map<Position, Node> dGraph, Set<Node> nodesInSpecialCycle) {
        return getPositions(getInfiniteRankNodes(dGraph, nodesInSpecialCycle));
    }

    public static Map<Position, Node> buildDependencyGraph(Set<TGD> rules) {
        HashMap<Position, Node> graph = new HashMap<>();
        for (TGD rule : rules) {
            for (Variable variable : rule.variables.values()) {
                if (!variable.isBody()) continue;
                Set<Node> bodyNodes = fetchNode(graph, getPositionsInConjunct(variable, rule.body));
                Set<Node> headNodes = fetchNode(graph, getPositionsInConjunct(variable, rule.head));
                for (Node b : bodyNodes) {
                    b.nexts.addAll(headNodes);
                    for (Variable evar : rule.existentialVars) {
                        b.nextSpecials.addAll(fetchNode(graph, getPositionsInConjunct(evar, rule.head)));
                    }
                }
            }
        }
        return graph;
    }

    public static int isPathBetween(Node n1, Node n2) {
        Set<Node> visited = new HashSet<>();
        return isPathBetween(n1, n2, visited);
    }

    public static int isPathBetween(Node n1, Node n2, Set<Node> visited) {
        Set<Node> nexts = new HashSet<>(n1.nexts);
        nexts.addAll(n1.nextSpecials);
        for (Node next : nexts) {
            boolean special = n1.nextSpecials.contains(next);
            if (next.equals(n2)) {
                visited.add(n2);
                return special ? 2 : 1;
            }
            if (!visited.contains(next)) {
                visited.add(next);
                int path = isPathBetween(next, n2, visited);
                if (path == 2)
                    return 2;
                else if (path == 1)
                    return special ? 2 : 1;
            }
        }
        return 0;
    }

    public static Set<Node> findAncestors(Set<Node> graph, Set<Node> nodes) {
        Map<Node, Node> invertedGraph = generateInvertedGraph(graph);
        Set<Node> invertNodes = new HashSet<>();
        for (Node node : nodes) {
            invertNodes.add(invertedGraph.get(node));
        }
        Set<Node> invertDescendants = findDescendants(invertNodes);
        Map<Node, Node> temp = getReverseMap(invertedGraph);
        HashSet<Node> result = new HashSet<>();
        for (Node node : invertDescendants) {
            result.add(temp.get(node));
        }
        return result;
    }

    public static Map<Node, Node> getReverseMap(Map<Node, Node> map) {
        Map<Node, Node> result = new HashMap<>();
        for (Node node : map.keySet()) {
            result.put(map.get(node), node);
        }
        return result;
    }

    public static Set<Node> findDescendants(Set<Node> nodes) {
        Set<Node> ancestors = new HashSet<Node>();
        Set<Node> newAncestors = new HashSet<Node>();
        for (Node node : nodes) {
            newAncestors.add(node);
        }
        Set<Node> visited = new HashSet<>();
        while (!newAncestors.isEmpty()) {
            Set<Node> temp = new HashSet<Node>();
            for (Node node : newAncestors) {
                if (visited.contains(node)) continue;
                temp.addAll(node.nexts);
                temp.addAll(node.nextSpecials);
                visited.add(node);
            }
            temp.removeAll(ancestors);
            ancestors.addAll(temp);
            newAncestors = temp;
        }
        return ancestors;
    }

    public static Map<Node, Node> generateInvertedGraph(Set<Node> graph) {
        Map<Node, Node> result = new HashMap<>();
        for (Node node : graph) {
            if (!result.containsKey(node)) result.put(node, new Node(node.p));
            Node nodeInver = result.get(node);
            Set<Node> nexts = new HashSet<>(node.nexts);
            nexts.addAll(node.nextSpecials);
            for (Node next : nexts) {
                if (!result.containsKey(next)) result.put(next, new Node(next.p));
                Node nextInvert = result.get(next);
                nextInvert.nexts.add(nodeInver);
            }
        }
        return result;
    }

    public static Set<Position> findAncestors(Map<Position, Node> graph, Set<Position> positions) {
        return getPositions(findAncestors(new HashSet<>(graph.values()), getNodes(graph, positions)));
    }

    public static Set<Node> getNodes(Map<Position, Node> graph, Set<Position> positions) {
        HashSet<Node> nodes = new HashSet<>();
        for (Position position : positions) {
            nodes.add(graph.get(position));
        }
        return nodes;
    }

    public static Set<Node> fetchNode(Map<Position, Node> graph, Set<Position> positions) {
        HashSet<Node> nodes = new HashSet<>();
        for (Position position : positions) {
            if (!graph.containsKey(position)) graph.put(position, new Node(position));
            nodes.add(graph.get(position));
        }
        return nodes;
    }

    public static Set<Position> getPositionsInConjunct(Variable variable, Conjunct conjunct) {
        HashSet<Position> positions = new HashSet<>();
        for (Atom atom : conjunct.getAtoms()) {
            int pos = 0;
            for (Term term : atom.terms) {
                if (term == variable) {
                    Position position = new Position(pos, atom.predicate);
                    positions.add(position);
                }
                pos++;
            }
        }
        return positions;
    }

    public static void findMarkedVariables(Set<TGD> rules) {
        Set<Position> markedPositions = new HashSet<Position>();
        for (TGD rule : rules) {
            int pos;
            for (Atom atom : rule.body.getAtoms()) {
                pos = 0;
                for (Term term : atom.terms) {
                    if (!(term instanceof Variable)) continue;
                    Variable variable = (Variable) term;
                    Position position = new Position(pos, atom.predicate);
                    if (!rule.head.getVariables().contains(variable)) {
                        variable.setMarked(true);
                        markedPositions.add(position);
                    }
                    pos++;
                }
            }
        }
        boolean newMarked = true;
        while (newMarked && !markedPositions.isEmpty()) {
            newMarked = false;
            for (TGD rule : rules) {
                for (Variable variable : rule.variables.values()) {
                    if (!variable.isBody()) continue;
                    if (containsAll(markedPositions, getPositionsInConjunct(variable, rule.head)) && !variable.isMarked()) {
                        markedPositions.addAll(getPositionsInConjunct(variable, rule.body));
                        variable.setMarked(true);
                        newMarked = true;
                    }
                }
            }
        }
    }

    public static boolean containsAll(Set<Position> ps1, Set<Position> ps2) {
        for (Position position : ps2) {
            if (!ps1.contains(position)) return false;
        }
        return true;
    }

    public static boolean isRepeated(Variable variable, TGD tgd) {
        boolean appeared = false;
        for (Atom atom : tgd.body.getAtoms()) {
            for (Term term : atom.terms) {
                if (variable.equals(term)) {
                    if (appeared)
                        return true;
                    else
                        appeared = true;
                }
            }
        }
        return false;
    }

    public static void main2(String[] args) throws IOException {
//        File in = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\test_data\\slinear-2.txt");
        File in = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\paper_dataset\\00726.txt");
        Program program = Parser.parseProgram(in);
        System.out.println("The number of rules (tgds): " + program.tgds.size());
        Map<Position, Node> graph = buildDependencyGraph(program.tgds);
        Set<Node> nodesInSpecialCycle = getNodesInSpecialCycle(graph, program);
        Set<Position> infiniteRankPositions = getInfiniteRankPositions(graph, nodesInSpecialCycle);
        findMarkedVariables(program.tgds);
        System.out.println("Simple linear? " + (isSimpleLinear(program.tgds) ? "Yes" : "No"));
        System.out.println("Linear? " + (isLinear(program.tgds) ? "Yes" : "No"));
        System.out.println("WeaklyAcyclic? " + (isWeaklyAcyclic(program.tgds, infiniteRankPositions) ? "Yes" : "No"));
        System.out.println("Guarded? " + (isGuarded(program.tgds) ? "Yes" : "No"));
        System.out.println("Sticky? " + (isSticky(program.tgds) ? "Yes" : "No"));
        System.out.println("WeaklySticky? " + (isWeaklySticky(program.tgds, infiniteRankPositions) ? "Yes" : "No"));
        System.out.println("Terminates? " + (terminates(program, graph, infiniteRankPositions) ? "Yes" : "No"));
        /*Map<Position, Node> graph = buildDependencyGraph(program.tgds);
        Node n1 = graph.get(new Position(1, Predicate.predicates.get("r")));
        Node n2 = graph.get(new Position(0, Predicate.predicates.get("r")));
        System.out.println("isPathBetween(n1,n2) = " + isPathBetween(n1, n2));*/
    }

    public static void main3(String[] args) throws IOException {
        String pathname = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\processed";
        File dir = new File(pathname);
        String[] list = dir.list();
        for (String filename : list) {
            try {
                long endTime, startTime;
                File in = new File(pathname + "\\" + filename);
                System.out.println("#####################################################");
                System.out.println("file: " + filename);
                startTime = System.nanoTime();
                Program program = Parser.parseProgram(in);
                endTime = System.nanoTime();
                System.out.println("time to parse program: " + ((endTime - startTime) / 1000000000F));
                System.out.println("The number of rules (tgds): " + program.tgds.size() + " and facts: " + program.edb.facts.size());

                startTime = System.nanoTime();
                Map<Position, Node> graph = buildDependencyGraph(program.tgds);
                endTime = System.nanoTime();
                System.out.println("Time to build dependency graph: " + ((endTime - startTime) / 1000000000F));

                startTime = System.nanoTime();
                Set<Position> loopPositions = getPositions(getNodesInSpecialCycle(graph, program));
                endTime = System.nanoTime();
                System.out.println("Time to find infinite rank positions: " + ((endTime - startTime) / 1000000000F));

//                startTime = System.nanoTime();
//                findMarkedVariables(program.tgds);
//                endTime = System.nanoTime();
//                System.out.println("Time to find marked variables positions: " + ((endTime - startTime)/1000000000F));

                System.out.println("Simple linear? " + (isSimpleLinear(program.tgds) ? "Yes" : "No") + ", linear? " + (isLinear(program.tgds) ? "Yes" : "No"));
//                System.out.println("WeaklyAcyclic? " + (isWeaklyAcyclic(program.tgds, infiniteRankPositions) ? "Yes" : "No") + ", guarded? " + (isGuarded(program.tgds) ? "Yes" : "No"));
//                System.out.println("Sticky? " + (isSticky(program.tgds) ? "Yes" : "No") + ", weaklySticky? " + (isWeaklySticky(program.tgds, infiniteRankPositions) ? "Yes" : "No"));
                startTime = System.nanoTime();
                System.out.println("Terminates? " + (terminates(program, graph, loopPositions) ? "Yes" : "No"));
                endTime = System.nanoTime();
                System.out.println("Time to decide termination: " + ((endTime - startTime) / 1000000000F));
            } catch (Exception e) {
                System.out.println("Can not process " + pathname + "\\" + filename);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File in = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\test_data\\new-test-2.txt");
//        File in = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl-dl\\00162.txt");
        long startTime = System.nanoTime();
        Program program = Parser.parseProgram(in);
        long endTime = System.nanoTime();
        System.out.println("time to parse program: " + ((endTime - startTime) / 1000000000F));
        System.out.println("The number of rules (tgds): " + program.tgds.size() + " and facts: " + program.edb.facts.size());

        startTime = System.nanoTime();
        Map<Position, Node> graph = buildDependencyGraph(program.tgds);
        endTime = System.nanoTime();
        System.out.println("Time to build dependency graph: " + ((endTime - startTime) / 1000000000F));

        startTime = System.nanoTime();
        Set<Position> positions = getPositions(getNodesInSpecialCycle(graph, program));
        endTime = System.nanoTime();
        System.out.println("Time to find infinite rank positions: " + ((endTime - startTime) / 1000000000F));
    }

    public static int getNumberMarkedVariables(Program program) {
        int i = 0;
        for (TGD tgd : program.tgds) {
            for (Variable variable : tgd.variables.values()) {
                if (variable.isMarked()) i++;
            }
        }
        return i;
    }

    public static double averageFanout(Map<Position, Node> graph) {
        int size = graph.keySet().size();
        if (size == 0) return 0;
        int totalFanout = 0;
        for (Node node : graph.values()) {
            totalFanout += node.nexts.size() + node.nextSpecials.size();
        }
        return totalFanout / size;
    }
}
