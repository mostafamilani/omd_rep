package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class SyntacticAnalyzer {
    public static boolean isLinear(Set<TGD> rules) {
        for (TGD rule : rules) {
            if (!isLinear(rule))
                return false;
        }
        return true;
    }

    private static boolean isLinear(TGD tgd) {
        return tgd.body.atoms.size() == 1;
    }

    public static boolean isSticky(Set<TGD> rules) {
        findMarkedVariables(rules);
        for (TGD rule : rules) {
            for (Variable variable : rule.variables.values()) {
                if (variable.isBodyVariable() && isRepeated(variable) && variable.marked)
                    return false;
            }
        }
        return true;
    }

    public static boolean isWeaklyAcyclic(Set<TGD> rules) {
        return getInfinitePositions(rules).isEmpty();
    }

    public static boolean isWeaklySticky(Set<TGD> rules) {
        findMarkedVariables(rules);
        Set<Position> infinitePositions = getInfinitePositions(rules);
        for (TGD rule : rules) {
            for (Variable variable : rule.variables.values()) {
                if (!variable.isBodyVariable()) continue;
                Set<Position> positions = getBodyPositions(variable, rule);
                if (isRepeated(variable) && variable.marked && containsAll(infinitePositions, positions))
                    return false;
            }
        }
        return true;
    }

    private static Set<Position> getInfinitePositions(Set<TGD> rules) {
        for (TGD rule : rules) {
            for (Variable variable : rule.variables.values()) {
                if (!variable.isBodyVariable()) continue;
                Set<Node> bodyNodes = getNodes(getBodyPositions(variable, rule));
                Set<Node> headNodes = getNodes(getHeadPositions(variable, rule));
                for (Node b : bodyNodes) {
                    b.nexts.addAll(headNodes);
                    for (Variable evar : rule.existentialVars) {
                        b.nextSpecials.addAll(getNodes(getHeadPositions(evar, rule)));
                    }
                }
            }
        }
        HashSet<Position> positions = new HashSet<>();
        for (Node node : Node.nodes.values()) {
            if (specialPath(node, node)) {
                positions.add(node.p);
            }
        }
        return positions;
    }

    private static boolean specialPath(Node n1, Node n2) {
        Set<Node> visited = new HashSet<Node>();
        Node cnode = n1;
        boolean special = false;
        while (true) {
            Node next = null;
            visited.add(cnode);
            for (Node n : cnode.nexts) {
                if (n == n2 && special) return true;
                if (!visited.contains(n)) next = n;
            }
            for (Node n : cnode.nextSpecials) {
                if (n == n2 && special) return true;
                if (!visited.contains(n)) {
                    next = n;
                    special = true;
                }
            }
            if (next != null)
                cnode = next;
            else
                return false;
        }
    }

    private static Set<Node> getNodes(Set<Position> positions) {
        HashSet<Node> nodes = new HashSet<>();
        for (Position position : positions) {
            nodes.add(Node.fetchNode(position));
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
        for (Atom atom : rule.body.atoms) {
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
            for (Atom atom : rule.body.atoms) {
                pos = 0;
                for (Term term : atom.terms) {
                    if (!(term instanceof Variable)) continue;
                    Variable variable = (Variable) term;
                    Position position = new Position(pos, atom.predicate);
                    if (!rule.headVariables.contains(variable)) {
                        variable.marked = true;
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
                    if (!variable.isBodyVariable()) continue;
                    if (containsAll(markedPositions, getHeadPositions(variable, rule)) && !variable.marked) {
                        markedPositions.addAll(getBodyPositions(variable, rule));
                        variable.marked = true;
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

    private static boolean isRepeated(Variable variable) {
        boolean appeared = false;
        for (Atom atom : ((TGD)variable.rule).body.atoms) {
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

    public static void main(String[] args) throws IOException {
        File in = new File("C:\\Users\\Mostafa\\Desktop\\omd_prj\\omd_rep\\omd\\program.txt");
        Program program = Parser.parseProgram(in);
        System.out.println("isLinear(program.rules) = " + SyntacticAnalyzer.isLinear(program.tgds));
        System.out.println("isSticky(program.rules) = " + SyntacticAnalyzer.isSticky(program.tgds));
        System.out.println("isWeaklyAcyclic(program.rules) = " + SyntacticAnalyzer.isWeaklyAcyclic(program.tgds));
        System.out.println("isWeaklySticky(program.rules) = " + SyntacticAnalyzer.isWeaklySticky(program.tgds));
    }
}
