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
            if (!isLinear(rule) || hasRepeatedVariable(rule))
                return false;
        }
        return true;
    }

    private static boolean hasRepeatedVariable(TGD tgd) {
        for (Variable variable : tgd.variables.values()) {
            if (variable.isBody() && isRepeated(variable, tgd))
                return true;
        }
        return false;
    }

    private static boolean isLinear(TGD tgd) {
        return tgd.body.getAtoms().size() == 1;
    }

    public static boolean isSticky(Set<TGD> tgds) {
        findMarkedVariables(tgds);
        for (TGD tgd : tgds) {
            for (Variable variable : tgd.variables.values()) {
                if (variable.isBody() && isRepeated(variable, tgd) && variable.isMarked())
                    return false;
            }
        }
        return true;
    }

    public static boolean isWeaklyAcyclic(Set<TGD> rules) {
        Map<Position, Node> dGraph = buildDependencyGraph(rules);
        return getInfiniteRankPositions(dGraph).isEmpty();
    }

    public static boolean isWeaklySticky(Set<TGD> tgds) {
        findMarkedVariables(tgds);
        Map<Position, Node> dGraph = buildDependencyGraph(tgds);
        Set<Position> infinitePositions = getInfiniteRankPositions(dGraph);
        for (TGD tgd : tgds) {
            for (Variable variable : tgd.variables.values()) {
                if (!variable.isBody()) continue;
                Set<Position> positions = getBodyPositions(variable, tgd);
                if (isRepeated(variable, tgd) && variable.isMarked() && containsAll(infinitePositions, positions))
                    return false;
            }
        }
        return true;
    }

    public static Set<Position> getInfiniteRankPositions(Map<Position, Node> dGraph) {
        HashSet<Position> positions = new HashSet<>();
        for (Node node : dGraph.values()) {
            if (isPathBetween(node, node) == 2) {
                if (!positions.contains(node.p)) System.out.println(node.p + " has infinite rank.");
                positions.add(node.p);
            }
        }
        return positions;
    }

    public static Map<Position, Node> buildDependencyGraph(Set<TGD> rules) {
        HashMap<Position, Node> graph = new HashMap<>();
        for (TGD rule : rules) {
            for (Variable variable : rule.variables.values()) {
                if (!variable.isBody()) continue;
                Set<Node> bodyNodes = fetchNode(graph, getBodyPositions(variable, rule));
                Set<Node> headNodes = fetchNode(graph, getHeadPositions(variable, rule));
                for (Node b : bodyNodes) {
                    b.nexts.addAll(headNodes);
                    for (Variable evar : rule.existentialVars) {
                        b.nextSpecials.addAll(fetchNode(graph, getHeadPositions(evar, rule)));
                    }
                }
            }
        }
        return graph;
    }

    private static int isPathBetween(Node n1, Node n2) {
        Set<Node> visited = new HashSet<>();
        return isPathBetween(n1, n2, visited);
    }

    private static int isPathBetween(Node n1, Node n2, Set<Node> visited) {
        Set<Node> nexts = new HashSet<>(n1.nexts);
        nexts.addAll(n1.nextSpecials);
        for (Node next : nexts) {
            boolean special = n1.nextSpecials.contains(next);
            if (next.equals(n2)) {
                visited.add(n2);
                return special? 2 : 1;
            }
            if (!visited.contains(next)){
                visited.add(next);
                int path = isPathBetween(next, n2, visited);
                if (path == 2)
                    return 2;
                else if (path == 1)
                    return special? 2 : 1;
            }
        }
        return 0;
    }

    public static Set<Position> findAncestors(Set<Node> graph, Position position) {
        HashSet<Position> positions = new HashSet<>();
        for (Node next : graph) {
            if (isPathBetween(next, new Node(position)) > 0) {
                positions.add(next.p);
            }
        }
        return positions;
    }

    public static Set<Position> findAncestors(Set<Node> graph, Set<Position> positions) {
        HashSet<Position> result = new HashSet<>();
        for (Position position : positions) {
            result.addAll(findAncestors(graph, position));
        }
        return result;
    }

    private static Set<Node> fetchNode(Map<Position, Node> graph, Set<Position> positions) {
        HashSet<Node> nodes = new HashSet<>();
        for (Position position : positions) {
            if (!graph.containsKey(position)) graph.put(position, new Node(position));
            nodes.add(graph.get(position));
        }
        return nodes;
    }

    private static Set<Position> getHeadPositions(Variable variable, TGD rule) {
        HashSet<Position> positions = new HashSet<>();
        int pos = 0;
        for (Term term : rule.head.terms) {
            if (term == variable) {
                Position position = new Position(pos, rule.head.predicate);
                positions.add(position);
            }
            pos++;
        }
        return positions;
    }

    private static Set<Position> getBodyPositions(Variable variable, TGD rule) {
        HashSet<Position> positions = new HashSet<>();
        for (Atom atom : rule.body.getAtoms()) {
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

    private static void findMarkedVariables(Set<TGD> rules) {
        Set<Position> markedPositions = new HashSet<Position>();
        for (TGD rule : rules) {
            int pos;
            for (Atom atom : rule.body.getAtoms()) {
                pos = 0;
                for (Term term : atom.terms) {
                    if (!(term instanceof Variable)) continue;
                    Variable variable = (Variable) term;
                    Position position = new Position(pos, atom.predicate);
                    if (!rule.headVariables.contains(variable)) {
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
                    if (containsAll(markedPositions, getHeadPositions(variable, rule)) && !variable.isMarked()) {
                        markedPositions.addAll(getBodyPositions(variable, rule));
                        variable.setMarked(true);
                        newMarked = true;
                    }
                }
            }
        }
    }

    private static boolean containsAll(Set<Position> ps1, Set<Position> ps2) {
        for (Position position : ps2) {
            if (!ps1.contains(position)) return false;
        }
        return true;
    }

    private static boolean isRepeated(Variable variable, TGD tgd) {
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

    public static void main1(String[] args) throws IOException {
        File in = new File("C:\\Users\\Mostafa\\Desktop\\omd_prj\\omd_rep\\omd\\rewrite.txt");
        Program program = Parser.parseProgram(in);
//        System.out.println("isLinear(program.rules) = " + SyntacticAnalyzer.isLinear(program.tgds));
//        System.out.println("isSticky(program.rules) = " + SyntacticAnalyzer.isSticky(program.tgds));
//        System.out.println("isWeaklyAcyclic(program.rules) = " + SyntacticAnalyzer.isWeaklyAcyclic(program.tgds));
//        System.out.println("isWeaklySticky(program.rules) = " + SyntacticAnalyzer.isWeaklySticky(program.tgds));
        List<CQ> cqs = Parser.parseQueries(in);
        CQ cq = cqs.get(0);
        MagicSetGadget.rewrite(program, cq);
        program.chase();
        Set<Assignment> evaluate = program.evaluate(cq);
        for (Assignment assignment : evaluate) {
            System.out.println("assignment = " + assignment);
        }
    }

    public static void main(String[] args) throws IOException {
        File in = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\linear.txt");
        Program program = Parser.parseProgram(in);
        System.out.println("isSimpleLinear(program.tgds) = " + isSimpleLinear(program.tgds));
        System.out.println("isLinear(program.tgds) = " + isLinear(program.tgds));
        System.out.println("terminates(program) = " + terminates(program));
        /*Map<Position, Node> graph = buildDependencyGraph(program.tgds);
        Node n1 = graph.get(new Position(1, Predicate.predicates.get("r")));
        Node n2 = graph.get(new Position(0, Predicate.predicates.get("r")));
        System.out.println("isPathBetween(n1,n2) = " + isPathBetween(n1, n2));*/
    }
}
