package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.primitives.*;
import ca.mcmaster.mmilani.omd.datalog.synthesizer.ProgramGenerator;
import ca.mcmaster.mmilani.omd.datalog.tools.syntax.SearchSetting;

import java.io.*;
import java.util.*;
import java.util.function.UnaryOperator;

import static ca.mcmaster.mmilani.omd.datalog.executer.TerminationAnalyzer.terminates;

public class SyntacticAnalyzer {
    public static String[] annotations = generateAnnotations(10);

    public static int[] annotationCounts = {1,2,5,15,52,203,877,4140,21147,115975,678570,4213597,27644437};

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
//        System.out.println("Finding connected components! #nodes: " + dGraph.keySet().size());
        SearchSetting setting = new SearchSetting();
        for (Node node : dGraph.values()) {
            if (node.index == -1) {
                setting.components.addAll(strongConnectNonR(node));
//                strongConnect(node, setting);
            }
        }
        HashSet<Node> result = new HashSet<>();
//        checkComponents(setting.components);
        for (FCComponent component : setting.components) {
            if (component.special) {
                result.addAll(component.members);
                program.nSpecialComponents++;
            }
        }
        program.nComponents = setting.components.size();
//        System.out.println("#components: " + program.nComponents);
//        System.out.println("#special components: " + program.nSpecialComponents);
        return result;
    }

    private static void checkComponents(Set<FCComponent> components) {
        System.out.println("Checking components...");
        for (FCComponent component : components) {
            checkComponent(component.members);
            for (FCComponent c2 : components) {
                if (c2 != component) {
                    checkComponentPair(component,c2);
                }
            }
        }
    }

    private static void checkComponentPair(FCComponent c1, FCComponent c2) {
        if (isLinked(c1,c2) && isLinked(c2,c1))
            throw new RuntimeException("Between components error!");
    }

    private static boolean isLinked(FCComponent c1, FCComponent c2) {
        for (Node n1 : c1.members) {
            for (Node n2 : c2.members) {
                if (isPathBetween(n1,n2))
                    return true;
            }
        }
        return false;
    }

    private static void checkComponent(Set<Node> members) {
        if (members.size() == 1)
            return;
        Node n0 = null;
        for (Node n1 : members) {
            if (n0==null) n0 = n1;
            List<Node> path = getPathBetween(n1, n1);
            if (path == null)
                throw new RuntimeException("Within components error!");
            path = getPathBetween(n0, n1);
            if (path == null)
                throw new RuntimeException("Within components error!");
        }
    }

    public static Set<Position> getPositions(Set<Node> nodes) {
        HashSet<Position> positions = new HashSet<>();
        for (Node node : nodes) {
            positions.add(node.p);
        }
        return positions;
    }

    public static Set<FCComponent> strongConnectNonR(Node node) {
        Set<FCComponent> components = new HashSet<>();

        int preCount = 0;
        Stack stack = new Stack();
        Stack minStack = new Stack();
        Stack iterStack = new Stack();
        Set initi = new HashSet();
        Edge e = new Edge();
        initi.add(e);
        e.destination = node;
        Iterator<Edge> iterator = initi.iterator();
//        Set<Node> visited = new HashSet<>();
        while(true) {
            if (iterator.hasNext()) {
                Node v = iterator.next().destination;
//                if (!visited.contains(v)) {
                if (v.index == -1) {
                    v.lowLink = preCount;
                    preCount++;
//                    visited.add(v);
//                    System.out.println("visited.size() = " + visited.size());
//                    System.out.println("v.p = " + v.p);
                    v.index = 1;
                    stack.push(v);
                    minStack.push(v.lowLink);
                    iterStack.push(v);
                    iterStack.push(iterator);
                    iterator = v.nexts.iterator();
                } else if (minStack.size() > 0) {
                    int min = (int) minStack.pop();
                    if (v.lowLink < min) min = v.lowLink;
                    minStack.push(min);
                }
            } else {
                if (iterStack.size() == 0) break;

                iterator = (Iterator<Edge>) iterStack.pop();
                Node v = (Node) iterStack.pop();
                int min = (int) minStack.pop();

                if (min < v.lowLink) {
                    v.lowLink = min;
                } else {
                    FCComponent component = new FCComponent();
                    Node wNode;
                    do {
                        wNode = (Node) stack.pop();
                        component.members.add(wNode);
                        wNode.lowLink = Integer.MAX_VALUE;
                    } while (!wNode.equals(v));
                    components.add(component);
                }
            }
        }
        return components;
    }

    public static void strongConnect(Node node, SearchSetting setting) {
        node.index = setting.globalIndex;
        node.lowLink = setting.globalIndex;
        setting.globalIndex++;
        setting.stack.push(node);
        node.onStack = true;
        for (Edge next : node.nexts) {
            /*if (next.destination.index == -1 || next.destination.onStack)
                setting.stack.push(null);*/
            if (next.special)
                setting.stack.push(null);
            if (next.destination.index == -1) {
                strongConnect(next.destination, setting);
                node.lowLink = Math.min(node.lowLink, next.destination.lowLink);
            } else if (next.destination.onStack){
                node.lowLink = Math.min(node.lowLink, next.destination.index);
            }
        }
        if (node.lowLink == node.index) {
            FCComponent component = new FCComponent();
            Node next;
            do {
                next = setting.stack.pop();
                if (next == null) {
                    if (component.members.size() > 0) component.special = true;
                    continue;
                }
                next.onStack = false;
                component.members.add(next);
            } while (node != next);
            if (!setting.stack.isEmpty())
                while(setting.stack.peek() == null) setting.stack.pop();
            if (component.members.size() > 1) setting.components.add(component);
        }
    }

    /*public static Set<Set<Node>> Search(Set<Node> graph)
    {
        Set<Set<Node>> stronglyConnectedComponents = new HashSet<>();

        int preCount = 0;
        Stack stack = new Stack<Node>();

        Stack minStack = new Stack<Node>();
        var enumeratorStack = new Stack<IEnumerator<int>>();
        var enumerator = Enumerable.Range(0, graph.VertexCount).GetEnumerator();
        while (true)
        {
            if (enumerator.MoveNext())
            {
                int v = enumerator.Current;
                if (!visited[v])
                {
                    low[v] = preCount++;
                    visited[v] = true;
                    stack.Push(v);
                    int min = low[v];
                    // Level down
                    minStack.Push(min);
                    enumeratorStack.Push(enumerator);
                    enumerator = Enumerable.Range(0, graph.OutgoingEdgeCount(v))
                            .Select(i => graph.OutgoingEdge(v, i).Target)
                    .GetEnumerator();
                }
                else if (minStack.Count > 0)
                {
                    int min = minStack.Pop();
                    if (low[v] < min) min = low[v];
                    minStack.Push(min);
                }
            }
            else
            {
                // Level up
                if (enumeratorStack.Count == 0) break;

                enumerator = enumeratorStack.Pop();
                int v = enumerator.Current;
                int min = minStack.Pop();

                if (min < low[v])
                {
                    low[v] = min;
                }
                else
                {
                    List<int> component = new List<int>();

                    int w;
                    do
                    {
                        w = stack.Pop();
                        component.Add(w);
                        low[w] = graph.VertexCount;
                    } while (w != v);
                    stronglyConnectedComponents.Add(component);
                }

                if (minStack.Count > 0)
                {
                    min = minStack.Pop();
                    if (low[v] < min) min = low[v];
                    minStack.Push(min);
                }
            }
        }
        return stronglyConnectedComponents;
    }*/

    public static Set<Node> getInfiniteRankNodes(Map<Position, Node> dGraph, Set<Node> nodesInSpecialCycle) {
        return findDescendants(nodesInSpecialCycle,false);
    }

    public static Set<Position> getInfiniteRankPositions(Map<Position, Node> dGraph, Set<Node> nodesInSpecialCycle) {
        return getPositions(getInfiniteRankNodes(dGraph, nodesInSpecialCycle));
    }

    public static Map<Position, Node> buildDependencyGraph(Set<TGD> rules) {
        HashMap<Position, Node> graph = new HashMap<>();
        for (TGD rule : rules) {
            updateGraph(graph, rule);
        }
        return graph;
    }

    public static boolean updateGraph(Map<Position, Node> graph, TGD rule) {
        boolean updated = false;
        for (Variable variable : rule.variables.values()) {
            if (!variable.isBody() || !rule.isFrontier(variable)) continue;
            Set<Node> bodyNodes = fetchNode(graph, getPositionsInConjunct(variable, rule.body));
            Set<Node> headNodes = fetchNode(graph, getPositionsInConjunct(variable, rule.head));
            for (Node b : bodyNodes) {
                for (Variable evar : rule.existentialVars) {
                    Set<Node> nextSpecials = fetchNode(graph, getPositionsInConjunct(evar, rule.head));
                    for (Node next : nextSpecials) {
                        updated = b.addNext(next, rule, true) || updated;
                    }
                }
                for (Node next : headNodes) {
                    updated = b.addNext(next, rule, false) || updated;
                }
            }
        }
        return updated;
    }

    public static int isPathBetweenSimple(Node n1, Node n2) {
        Set<Node> visited = new HashSet<>();
        return isPathBetweenSimple(n1, n2, visited);
    }

    public static int isPathBetweenSimple(Node n1, Node n2, Set<Node> visited) {
        for (Edge next : n1.nexts) {
            if (next.destination.equals(n2)) {
                visited.add(n2);
                return next.special ? 2 : 1;
            }
            if (!visited.contains(next)) {
                visited.add(next.destination);
                int path = isPathBetweenSimple(next.destination, n2, visited);
                if (path == 2)
                    return 2;
                else if (path == 1)
                    return next.special ? 2 : 1;
            }
        }
        return 0;
    }

    public static boolean  isPathBetween(Node n1, Node n2) {
        List<Node> path = getPathBetween(n1, n2);
        return path != null;
    }

    public static List<Node> getPathBetween(Node n1, Node n2) {
        ArrayList<Node> path = new ArrayList<>();
        Stack<Node> stack = new Stack<>();
        Set<Node> visited = new HashSet<>();
        visited.add(n1);
        stack.push(n1);
        while(!stack.isEmpty()) {
            Node top = stack.pop();
            path.add(top);
            boolean backtrack = true;
            for (Node next : top.nextNodes(false)) {
                if (next.equals(n2)) {
                    path.add(next);
                    return path;
                }
                if (!visited.contains(next)) {
                    visited.add(next);
                    stack.push(next);
                    backtrack = false;
                }
            }
            if (backtrack)
                path.remove(path.size()-1);
        }
        return null;
    }



    public static Map<Node, Node> getReverseMap(Map<Node, Node> map) {
        Map<Node, Node> result = new HashMap<>();
        for (Node node : map.keySet()) {
            result.put(map.get(node), node);
        }
        return result;
    }

    public static Set<Node> findDescendants(Set<Node> nodes, boolean reverse) {
        Set<Node> ancestors = new HashSet<Node>();
        Set<Node> newAncestors = new HashSet<Node>();

        newAncestors.addAll(nodes);
        Set<Node> visited = new HashSet<>();
        while (!newAncestors.isEmpty()) {
            Set<Node> temp = new HashSet<Node>();
            for (Node node : newAncestors) {
                if (visited.contains(node)) continue;
                ancestors.add(node);
                temp.addAll(node.nextNodes(reverse));
                visited.add(node);
            }
            temp.removeAll(ancestors);
            newAncestors = temp;
        }
        return ancestors;
    }

    public static Set<Position> findAncestors(Map<Position, Node> graph, Set<Position> positions) {
        return getPositions(findDescendants(getNodes(graph, positions), true));
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
                if (term.label.equals(variable.label)) {
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
                System.out.println("The number of rules (tgds): " + program.tgds.size() + " and facts: " + program.edb.getFacts().length);

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

    public static void main4(String[] args) throws IOException {
        File in = new File(
                "/home/cqadev/Desktop/chase-termination/programs/synthetic-at-arity/program-569-r.txt");
//        File in = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl-dl\\00754.txt");
        long startTime = System.nanoTime();
        Program program = Parser.parseProgram(in);
        long endTime = System.nanoTime();
        System.out.println("time to parse program: " + ((endTime - startTime) / 1000000000F));
        System.out.println("The number of rules (tgds): " + program.tgds.size() + " and facts: " + program.edb.getFacts().length);

        startTime = System.nanoTime();
        Map<Position, Node> graph = buildDependencyGraph(program.tgds);
        endTime = System.nanoTime();
        System.out.println("Time to build dependency graph: " + ((endTime - startTime) / 1000000000F));

        startTime = System.nanoTime();
        Set<Node> nodesInSpecialCycle = getNodesInSpecialCycle(graph, program);
        Set<Position> positions = getPositions(nodesInSpecialCycle);
        endTime = System.nanoTime();
        System.out.println("Time to find infinite rank positions: " + ((endTime - startTime) / 1000000000F));

        startTime = System.nanoTime();
        Set<Position> infiniteRankPositions = getInfiniteRankPositions(graph, nodesInSpecialCycle);
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
            totalFanout += node.nextNodes(false).size();
        }
        return totalFanout / size;
    }

    public static Node getNode(Set<Node> nodes, String label) {
        for (Node next : nodes) {
            if (next.toString().equals(label)) return next;
        }
        return null;
    }

    public static Program simplify(Program program) {
        long startTime = System.nanoTime();
        Program result = new Program();
        int c= 0;
        for (TGD tgd : program.tgds) {
            c += simplify(tgd, program);
        }
        System.out.println("#Total number of rules in the initial program: " + program.tgds.size());
        System.out.println("#Total number of rules in the simplified program: " + c);
        long endTime = System.nanoTime();
        System.out.println("#time (sec) " + ((endTime - startTime) / 1000000000F));
        return result;
    }

    public static int simplify(TGD tgd, Program program) {
        int c = 0;
        System.out.println("#Simplifying rule: " + tgd);
        if (tgd.body.getAtoms().size() != 1) throw new RuntimeException("Simplifying a non linear program!");
        List<Variable> vs = tgd.body.getAtoms().get(0).getVariables();
        for (int i = 0; i < annotationCounts[vs.size()-1]; i++) {
            String ant = annotations[i].substring(0, vs.size());
            simplify(program, tgd, ant);
            c++;
        }
        return c;
    }

    public static TGD simplify(Program program, TGD tgd, String ant) {
        Atom b = tgd.body.getAtoms().get(0);
        Atom h = tgd.head.getAtoms().get(0);
        List<Variable> vs = b.getVariables();
        List<Term> hTerms = new ArrayList<>(h.terms);
        String hAnnot = "";
        String completeAn = "";
        List<Term> newTerms = new ArrayList<>();
        for (Term var : b.terms) {
            int index = b.terms.indexOf(var);
            String s = "" + ant.charAt(index);
            Variable nVar = vs.get(Integer.parseInt(s, 16)-1);
            newTerms.add(nVar);
            completeAn += s;

            if (nVar != var && hTerms.contains(var)) {
                hTerms.replaceAll(term -> {
                    if (term == var)
                        return nVar;
                    return term;
                });
            }
        }
        List<Variable> hvs = new ArrayList<>();
        for (Term term : hTerms) {
            if (!hvs.contains(term))
                hvs.add((Variable) term);
            hAnnot += "" + (hvs.indexOf(term)+1);
        }
        List<Term> temp = new ArrayList<>();
        for (Term term : hTerms) {
            if (!temp.contains(term)) temp.add(term);
        }
        hTerms = temp;
        temp = new ArrayList<>();
        for (Term term : newTerms) {
            if (!temp.contains(term)) temp.add(term);
        }
        newTerms = temp;
        PositiveAtom nB = new PositiveAtom(program.schema.fetchPredicate(b.predicate.name + "@" + completeAn, b.predicate.arity), newTerms);
        PositiveAtom nH = new PositiveAtom(program.schema.fetchPredicate(h.predicate.name + "@" + hAnnot, h.predicate.arity), hTerms);
//        System.out.println(nH + ":-" + nB);
        TGD result = new TGD();
        result.body = new Conjunct();
        result.body.add(nB);
        result.head = new Conjunct();
        result.head.add(nH);
        result.initVars();
        ProgramGenerator.setExistentialVariables(result);
        return result;
    }

    private static boolean match(String a, String b) {
        for (int i = 0; i < b.length(); i++) {
        }
        return false;
    }

    private static String getAnnotation(Atom a) {
        List<Term> terms = a.terms;
        String annotation = "1";
        for (Term t : terms) {
            annotation += terms.indexOf(t);
        }
        return annotation;
    }

    private static String[] generateAnnotations(int count) {
        Set<String> annotations = new HashSet<>();
        annotations.add("1");
        for (int i = 2; i <= count; i++) {
            Set<String> newAns = new HashSet<>();
            for (int j = i-1; j >= 1; j--) {
                for (String annotation : annotations) {
                    int max = 0;
                    for (int k = 0; k < annotation.length(); k++) {
                        int n = Integer.parseInt(annotation.charAt(k) + "");
                        if (n > max) max = n;
                    }
                    max++;
                    String newAn1 = annotation + Integer.toHexString(max);
                    newAns.add(newAn1);
                    String newAn2 = annotation + Integer.toHexString(getValueIndex(annotation, j));
                    newAns.add(newAn2);
                }
            }
//            System.out.println("newAns.size() = " + newAns.size());
            annotations = newAns;
        }
//        System.out.println("# New Annotations " + annotations.size());
        String res[] = annotations.toArray(new String[annotations.size()]);
        Arrays.sort(res, Comparator.comparing(o -> new StringBuilder(o).reverse().toString()));
        return res;
    }


    public static Set<String> generateCompatibleAnnotations(String shape) {
//        System.out.println(shape);
        Set<String> annotations = new HashSet<>();
        annotations.add("1");
        for (int i = 2; i <= shape.length(); i++) {
            Set<String> newAns = new HashSet<>();
            String sh = shape.substring(0, i);
            for (int j = i-1; j >= 1; j--) {
                for (String annotation : annotations) {
                    int max = 0;
                    for (int k = 0; k < annotation.length(); k++) {
                        int n = Integer.parseInt(annotation.charAt(k) + "");
                        if (n > max) max = n;
                    }
                    max++;
                    String newAn1 = annotation + Integer.toHexString(max);
                    if (isCompatible(newAn1, sh)) newAns.add(newAn1);
                    String newAn2 = annotation + Integer.toHexString(getValueIndex(annotation, j));
                    if (isCompatible(newAn2, sh)) newAns.add(newAn2);
                }
            }
            annotations = newAns;
        }
        return annotations;
    }

    public static boolean isCompatible(String rulShape, String currentShape) {
        if (rulShape.length() != currentShape.length()) return false;
        if (rulShape.equals(currentShape)) return true;
        for (int i = 0; i < rulShape.length(); i++) {
            if (rulShape.charAt(i) < currentShape.charAt(i)) return false;
        }
        return true;
    }

    private static int getValueIndex(String annotation, int j) {
        int c = Integer.parseInt(annotation.charAt(j-1)+"", 16);
        for (int i = 0; i < j-2; i++) {
            int ch = Integer.parseInt(annotation.charAt(i) + "");
            if (ch == c)
                return getValueIndex(annotation, c);
        }
        return c;
    }

    private static void copyMap(HashMap<Variable, Variable> dest, Map<Variable, Variable> src) {
        for (Variable key : src.keySet()) {
            dest.put(key, src.get(key));
        }
    }

    public static void main1(String[] args) throws FileNotFoundException {
        /*for (int i = 0; i < 10; i++) {
            String atomStr = "P(";
            for (int j = 0; j <= i; j++) {
                atomStr += "x" + j + ",";
            }
            atomStr = atomStr.substring(0, atomStr.length()-1) + ")";
            System.out.println("atom = " + atomStr);
            Atom atom = Parser.parse(atomStr, true, new Program(), new TGD());
            long startTime = System.nanoTime();
            generateUnifications(atom);
            long endTime = System.nanoTime();
            System.out.println("time (sec) " + ((endTime - startTime) / 1000000000F));
        }*/
        PrintStream out = new PrintStream(new FileOutputStream("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\output.txt"));
        System.setOut(out);

        String[] annotations = null;
        System.out.println("#################");
        for (int i = 1; i < 14; i++) {
            System.out.println("Arity: " + i);
            long startTime = System.nanoTime();
            annotations = generateAnnotations(i);
            long endTime = System.nanoTime();
            System.out.println("Time (sec) " + ((endTime - startTime) / 1000000000F) + "\n\t");
        }
        System.out.println("#################");
        for (String annt : annotations) {
            System.out.println(annt);
        }
        System.out.println("#################");
        out.flush();
    }

    public static void main(String[] args) throws IOException {
        File dir = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\linear");
        File[] files = dir.listFiles();
        for (File file : files) {
            Program program = Parser.parseProgram(file);
            String outName = file.getAbsolutePath().replace(".txt", "-sim.txt").replace("linear", "simplified");
            File out = new File(outName);
            out.createNewFile();
            System.setOut(new PrintStream(out));
            simplify(program);
        }
    }
}

