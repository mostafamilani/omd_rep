package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static ca.mcmaster.mmilani.omd.datalog.SyntacticAnalyzer.buildDependencyGraph;
import static ca.mcmaster.mmilani.omd.datalog.SyntacticAnalyzer.getInfiniteRankPositions;

public class TerminationAnalyzer {
    public static boolean terminates(Program program) {
        Map<Position, Node> dGraph = buildDependencyGraph(program.tgds);
        Set<Position> infinitePositions = getInfiniteRankPositions(dGraph);
        Set<Position> ancestors = SyntacticAnalyzer.findAncestors(new HashSet<>(dGraph.values()), infinitePositions);
        Set<Predicate> ePredicates = fetchExtensionalPredicates(program);

        String sql = generateSQL(ancestors);
        System.out.println("sql = " + sql);
        for (Position position : ancestors) {
            if (ePredicates.contains(position.predicate)) {
                System.out.println(position.predicate + " supports an infinite loop.");
                return false;
            }
        }
        return true;
    }

    private static String generateSQL(Set<Position> ancestors) {
        StringBuilder tableCheck = new StringBuilder();
        Set<Predicate> predicates = new HashSet<>();
        for (Position position : ancestors) {
            predicates.add(position.predicate);
        }
        for (Predicate predicate : predicates) {
            tableCheck.append("EXISTS(SELECT 1 FROM ").append(predicate).append(") AND");
        }
        if (!tableCheck.toString().equals("")) tableCheck.substring(0,tableCheck.length()-4);
        return "SELECT CASE WHEN " + tableCheck + " THEN 1 ELSE 0 END";
    }

    private static Set<Predicate> fetchExtensionalPredicates(Program program) {
        HashSet<Predicate> predicates = new HashSet<>();
        for (Fact fact : program.edb.facts) {
            predicates.add(fact.predicate);
        }
        return predicates;
    }

    public static void main(String[] args) throws IOException {
        String pathname = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\simple-linear.txt";
        File in = new File(pathname);
        Program program = Parser.parseProgram(in);
        if (!SyntacticAnalyzer.isSimpleLinear(program.tgds)) {
            System.out.println("Not simple linear.");
            return;
        }
        if(terminates(program))
            System.out.println("The program terminates.");
        else
            System.out.println("The program does not terminate.");
    }
}
