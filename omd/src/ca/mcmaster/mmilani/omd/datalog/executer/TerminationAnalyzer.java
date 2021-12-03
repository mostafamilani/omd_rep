package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer.buildDependencyGraph;

public class TerminationAnalyzer {
    public static boolean terminates(Program program, Map<Position, Node> graph, Set<Position> cyclicPositions) {
        if (!cyclicPositions.isEmpty()) {
            return false;
        }
        if (cyclicPositions.isEmpty()) {
//            System.out.println("There is no position with infinite rank!");
            return true;
        } /*else {
            String positions = "";
            for (Position position : infiniteRankPositions) { positions += position + ",";
            }
            System.out.println("Positions with infinite rank: " + positions.substring(0, positions.length() - 1));
        }*/
//        Set<Position> ancestors = SyntacticAnalyzer.findAncestors(dGraph, infiniteRankPositions);
        Set<Position> ancestors = SyntacticAnalyzer.findAncestors(graph, cyclicPositions);
//        Set<Predicate> ePredicates = fetchExtensionalPredicates(program);

//        String sql = generateSQL(ancestors);
//        System.out.println("sql = " + sql);
//        if (!ePredicates.isEmpty())
            for (Position position : ancestors) {
                if (!program.edb.isEmpty(position.predicate)) {
//                System.out.println(position.predicate + " supports an infinite loop.");
                    return false;
                }
            }
        return true;
    }

    private static String generateSQL(Set<Position> ancestors) {
        String tableCheck = "";
        Set<Predicate> predicates = new HashSet<>();
        for (Position position : ancestors) {
            predicates.add(position.predicate);
        }
        for (Predicate predicate : predicates) {
            tableCheck += "EXISTS(SELECT 1 FROM " + predicate + ") OR ";
        }
        if (!tableCheck.equals("")) tableCheck = tableCheck.substring(0, tableCheck.length() - 4);
        return "SELECT CASE WHEN " + tableCheck + " THEN TRUE ELSE FALSE END";
    }

    private static Set<Predicate> fetchExtensionalPredicates(Program program) {
        HashSet<Predicate> predicates = new HashSet<>();
        for (Fact fact : program.edb.getFacts()) {
            predicates.add(fact.predicate);
        }
        for (Predicate predicate : program.schema.predicates.values()) {
            if (!program.edb.isEmpty(predicate))
                predicates.add(predicate);
        }
        return predicates;
    }

    public static void main(String[] args) throws IOException {
//        String pathname = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\slinear-1.txt";
//        String pathname = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\test_data\\component-test-2.txt";
//        String pathname = "/home/cqadev/Desktop/chase-termination/programs/synthetic-at-rules/program-8.txt";
        String pathname = "/home/cqadev/IdeaProjects/omd_rep/omd/dataset/test_data/test-complex.txt";
        File in = new File(pathname);
        Program program = Parser.parseProgram(in);
        if (!SyntacticAnalyzer.isSimpleLinear(program.tgds)) {
            System.out.println("Not simple linear.");
            return;
        }
        Map<Position, Node> graph = buildDependencyGraph(program.tgds);
        Set<Node> nodes = SyntacticAnalyzer.getNodesInSpecialCycle(graph, program);
        Set<Position> positions = SyntacticAnalyzer.getPositions(nodes);
        boolean terminates = terminates(program, graph, positions);
        System.out.println("terminates = " + terminates);
    }
}
