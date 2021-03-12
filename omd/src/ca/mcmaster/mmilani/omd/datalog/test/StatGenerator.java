package ca.mcmaster.mmilani.omd.datalog.test;

import ca.mcmaster.mmilani.omd.datalog.Parser;
import ca.mcmaster.mmilani.omd.datalog.Program;
import ca.mcmaster.mmilani.omd.datalog.primitives.Node;
import ca.mcmaster.mmilani.omd.datalog.primitives.Position;
import ca.mcmaster.mmilani.omd.datalog.primitives.Predicate;
import no.s11.owlapi.ProfileChecker;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.Profiles;

import java.io.*;
import java.util.*;

import static ca.mcmaster.mmilani.omd.datalog.SyntacticAnalyzer.*;
import static ca.mcmaster.mmilani.omd.datalog.TerminationAnalyzer.terminates;

public class StatGenerator {
    public static void main(String[] args) throws IOException {
        String in = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl-dl";
        String out = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\results\\stats.csv";
        String ontologyPath = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl";
        generateStats(in, out, ontologyPath);
//        String out = processOntology("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\test_data\\", "new-test.txt");
//        String out = processOntology("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl-dl",
//                "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl",
//                "00006.txt");
//        System.out.println("out = " + out);
    }

    private static void generateStats(String pathname, String outputFile, String ontologyPath) throws IOException {
        File dir = new File(pathname);
        String[] list = dir.list();
        BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
        out.write("ont_name, #rule, #facts, t_parse, t_dgraph, #nodes, #predicates, #edges, #e_edges, #e_vars, #c_components, #c_special_comp, t_cyc_pos, #cyc_pos, " +
                "linear, slinear, wacyclic, #inf_rank, t_cycle," +
                "terminates, t_terminates, owl2_dl, owl2_ql, owl2_el, owl2_rl, owl2_full, t_ontology");
        out.newLine();
        for (String filename : list) {
            String output = processOntology(pathname, ontologyPath, filename);
            out.write(output);
            out.newLine();
            out.flush();
        }
        out.close();
    }

    private static String processOntology(String path, String ontologyPath, String filename) throws IOException {
        String output = filename;
        ProfileChecker profileChecker = new ProfileChecker();
        Profiles[] var4 = Profiles.values();
        try {
            long endTime, startTime;
            File in = new File(path + "\\" + filename);
            startTime = System.nanoTime();
            Program program = Parser.parseProgram(in);
            endTime = System.nanoTime();
            output += "," + program.tgds.size() + ", " + program.edb.facts.size() + ", " + ((endTime - startTime) / 1000000000F);

            startTime = System.nanoTime();
            Map<Position, Node> graph = buildDependencyGraph(program.tgds);
            endTime = System.nanoTime();
            output += ", " + ((endTime - startTime) / 1000000000F) + ", " + graph.keySet().size() + ", " +
                    Predicate.predicates.keySet().size() + ", " + countEdges(graph) + ", " + countSepcialEdges(graph) + ", " +
                    program.nExistential;

            startTime = System.nanoTime();
            Set<Node> nodesInSpecialCycle = getNodesInSpecialCycle(graph, program);
            Set<Position> loopPositions = getPositions(nodesInSpecialCycle);
            endTime = System.nanoTime();
            output += ", " + program.nComponents + ", " + program.nSpecialComponents + ", " + ((endTime - startTime) / 1000000000F) + ", " + loopPositions.size();

//            startTime = System.nanoTime();
//            findMarkedVariables(program.tgds);
//            endTime = System.nanoTime();
//            output += ", " + ((endTime - startTime) / 1000000000F) + ", " + getNumberMarkedVariables(program);

            startTime = System.nanoTime();
            Set<Position> infiniteRankPositions = getInfiniteRankPositions(graph, nodesInSpecialCycle);
            endTime = System.nanoTime();

            output += ", " + (isLinear(program.tgds) ? "true" : "false") + ", " + (isSimpleLinear(program.tgds) ? "true" : "false");
            output += ", " + (isWeaklyAcyclic(program.tgds, infiniteRankPositions) ? "true" : "false") + ", " + infiniteRankPositions.size();
            output += ", " + ((endTime - startTime) / 1000000000F);
//            output += ", " + (isGuarded(program.tgds) ? "true" : "false");
//            output += ", " + (isSticky(program.tgds) ? "true" : "false") + ", " + (isWeaklySticky(program.tgds, infiniteRankPositions) ? "true" : "false");

            startTime = System.nanoTime();
            output += ", " + (terminates(program, graph, loopPositions) ? "true" : "false");
            endTime = System.nanoTime();
            output += ", " + ((endTime - startTime) / 1000000000F);

            startTime = System.nanoTime();
            OWLOntology owlOntology = profileChecker.loadOntology(ontologyPath + "\\" + filename.replace(".txt", ".xml"));
            for (Profiles p : var4) {
                OWLProfileReport report = p.checkOntology(owlOntology);
                if (report.isInProfile()) {
                    output += ", " + p.name();
                } else {
                    output += ", ";
                }
            }
            endTime = System.nanoTime();
            output += ", " + ((endTime - startTime) / 1000000000F);

            System.out.println(filename + " processed.");
            return output;
        } catch (Exception e) {
            e.printStackTrace();
            output = "Could not parse " + filename + "!";
        }
        return output;
    }

    private static int countEdges(Map<Position, Node> graph) {
        int count = 0;
        for (Node node : graph.values()) {
            count += node.nexts.size() + node.nextSpecials.size();
        }
        return count;
    }


    private static int countSepcialEdges(Map<Position, Node> graph) {
        int count = 0;
        for (Node node : graph.values()) {
            count += node.nextSpecials.size();
        }
        return count;
    }

}
