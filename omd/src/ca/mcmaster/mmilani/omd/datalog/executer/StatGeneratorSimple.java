package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.primitives.Node;
import ca.mcmaster.mmilani.omd.datalog.primitives.Position;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer.*;
import static ca.mcmaster.mmilani.omd.datalog.executer.TerminationAnalyzer.terminates;

public class StatGeneratorSimple {
    public static void main(String[] args) throws IOException {
        String in = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl-dl";
        String out = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\results\\stats.csv";
        generateStats(in, out);
    }

    private static void generateStats(String pathname, String outputFile) throws IOException {
        File dir = new File(pathname);
        String[] list = dir.list();
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
        out.write("name, #rules, #facts, #nodes, #edges, #predicates, #c_components, #c_special_comp, wacyclic, terminate" +
                ", t_parse, t_terminates");
        out.newLine();
        for (String filename : list) {
            String output = processOntology(pathname, filename);
            if (output== null) continue;
            out.write(output);
            out.newLine();
            out.flush();
        }
        out.close();
    }

    private static String processOntology(String path, String filename) throws IOException {
        String output;
        try {
            long endTime, startTime;
            File in = new File(path + "\\" + filename);
            startTime = System.nanoTime();
            Program program = Parser.parseProgram(in);
            if (program.edb.isEmpty() || program.tgds.isEmpty() || !isSimpleLinear(program.tgds)) return null;
            endTime = System.nanoTime();
            float t_parse = (endTime - startTime) / 1000000F;
            startTime = System.nanoTime();
            Map<Position, Node> graph = buildDependencyGraph(program.tgds);
            endTime = System.nanoTime();
            float t_graph = (endTime - startTime) / 1000000F;

            startTime = System.nanoTime();
            Set<Node> nodesInSpecialCycle = getNodesInSpecialCycle(graph, program);
            Set<Position> loopPositions = getPositions(nodesInSpecialCycle);
            endTime = System.nanoTime();
            float t_component = (endTime - startTime) / 1000000F;

            startTime = System.nanoTime();
            Set<Position> infiniteRankPositions = getInfiniteRankPositions(graph, nodesInSpecialCycle);
            endTime = System.nanoTime();

            float t_inRank = (endTime - startTime) / 1000000F;

            startTime = System.nanoTime();
            boolean terminates = terminates(program, graph, loopPositions);
            endTime = System.nanoTime();

            float t_termination = (endTime - startTime) / 1000000F;

            output = filename + "," + program.tgds.size() + ", " + program.edb.getFacts().length + ", " + graph.keySet().size() +
                    ", " + countEdges(graph) + ", " + program.schema.predicates.keySet().size() + ", " + program.nComponents +
                    ", " + program.nSpecialComponents + ", " + (isWeaklyAcyclic(program.tgds, infiniteRankPositions) ? "true" : "false") +
                    ", " + (terminates ? "true" : "false") + ", " + t_parse + ", " + (t_graph+t_component+t_termination);

            System.out.println(filename + " processed.");
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int countEdges(Map<Position, Node> graph) {
        int count = 0;
        for (Node node : graph.values()) {
            count += node.nexts.size() + node.nextNodes(false).size();
        }
        return count;
    }

    static int countSepcialEdges(Map<Position, Node> graph) {
        int count = 0;
        for (Node node : graph.values()) {
            count += node.nextSpecialNodes().size();
        }
        return count;
    }
}
