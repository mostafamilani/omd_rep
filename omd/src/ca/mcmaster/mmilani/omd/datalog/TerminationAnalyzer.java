package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ca.mcmaster.mmilani.omd.datalog.SyntacticAnalyzer.buildDependencyGraph;

public class TerminationAnalyzer {
    public static boolean terminates(Program program, Map<Position, Node> graph, Set<Position> infiniteRankPositions) {
        if (infiniteRankPositions.isEmpty()) {
            System.out.println("There is no position with infinite rank!");
            return true;
        } else {
            String positions = "";
            for (Position position : infiniteRankPositions) { positions += position + ",";
            }
//            System.out.println("Positions with infinite rank: " + positions.substring(0, positions.length() - 1));
        }
//        Set<Position> ancestors = SyntacticAnalyzer.findAncestors(dGraph, infiniteRankPositions);
        Set<Position> ancestors = SyntacticAnalyzer.findAncestorsEfficiently(graph, infiniteRankPositions);
        Set<Predicate> ePredicates = fetchExtensionalPredicates(program);

        String sql = generateSQL(ancestors);
//        System.out.println("sql = " + sql);
        for (Position position : ancestors) {
            if (ePredicates.contains(position.predicate)) {
                System.out.println(position.predicate + " supports an infinite loop.");
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
        if (!tableCheck.equals("")) tableCheck = tableCheck.substring(0,tableCheck.length()-4);
        return "SELECT CASE WHEN " + tableCheck + " THEN TRUE ELSE FALSE END";
    }

    private static Set<Predicate> fetchExtensionalPredicates(Program program) {
        HashSet<Predicate> predicates = new HashSet<>();
        for (Fact fact : program.edb.facts) {
            predicates.add(fact.predicate);
        }
        return predicates;
    }

    public static void main(String[] args) throws IOException {
        String pathname = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\slinear-1.txt";
        File in = new File(pathname);
        Program program = Parser.parseProgram(in);
        if (!SyntacticAnalyzer.isSimpleLinear(program.tgds)) {
            System.out.println("Not simple linear.");
            return;
        }
        Map<Position, Node> graph = buildDependencyGraph(program.tgds);
    }
}
