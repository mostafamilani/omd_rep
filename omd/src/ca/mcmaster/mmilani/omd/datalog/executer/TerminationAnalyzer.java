package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.primitives.*;
import ca.mcmaster.mmilani.omd.datalog.synthesizer.DataGenerator;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer.*;

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

    public static boolean terminatesLinear(Program program, String dbname, Map<String, Object> result) throws IOException, SQLException {
        Set<Predicate> predicates = new HashSet<>(program.schema.predicates.values());
//        System.out.print("Finding db shapes...");

        long startTime = System.nanoTime();
        Map<Predicate, Set<String>> deltaShapes = DataGenerator.findShapes(dbname, predicates, result);
        long endTime = System.nanoTime();
        float t_shapes = (endTime - startTime) / 1000000F;
        result.put(OntologyAnalyzer.TIME_FIND_SHAPES, t_shapes);


        startTime = System.nanoTime();
//        System.out.println(" finished!");
//        System.out.println("dbshapes = " + deltaShapes);
        Map<Predicate, Map<String, Set<TGD>>>  shapeRules = new HashMap<>();
//        System.out.print("Constructing index...");
        for (TGD tgd : program.tgds) {
            Predicate p = tgd.body.getAtoms().get(0).predicate;
            String shape = generateShape(tgd);
            if (!shapeRules.containsKey(p)) shapeRules.put(p, new HashMap<>());
            if (!shapeRules.get(p).containsKey(shape)) shapeRules.get(p).put(shape, new HashSet<>());
            shapeRules.get(p).get(shape).add(tgd);
        }
//        System.out.println(" finished!");

        Map<Position, Node> graph = new HashMap<>();
        Set<String> fired = new HashSet<>();
        Program newProgram = new Program();
        Map<Predicate, Set<String>> newShapes = new HashMap<>();
        while(!deltaShapes.isEmpty()) {
//            System.out.print("Starting an iteration...[#rules " + program.tgds.size() + "]");
//            System.out.println("deltaShapes.predicates = " + deltaShapes.keySet());
            for (Predicate p : deltaShapes.keySet()) {
                if (!shapeRules.keySet().contains(p)) {
//                    System.out.println("Predicate " + p.name + " does not have any rule!");
                    continue;
                }
//                System.out.println("deltaShapes.shapes for " + p + " = " + deltaShapes.get(p));
                for (String shape : deltaShapes.get(p)) {
//                    System.out.println("Get compatible shapes for " + shape);
                    Set<String> compatibleShapes = SyntacticAnalyzer.generateCompatibleAnnotations(shape);
//                    System.out.println("compatibleShapes = " + compatibleShapes);
                    for (String targetShape : compatibleShapes) {
                        if (!shapeRules.get(p).containsKey(targetShape)) {
//                            System.out.println("Predicate+Shape " + p.name  + "@" + targetShape + " does not have any rule!");
                            continue;
                        }
                        for (TGD tgd : shapeRules.get(p).get(targetShape)) {
//                            System.out.println("Simplifying tgd " + tgd + " with annotation " + targetShape);
                            if (!fired.contains(targetShape + "@" + tgd.hashCode())) {
                                fired.add(targetShape + "@" + tgd.hashCode());
                                TGD sTGD = simplify(newProgram, tgd, targetShape);
                                SyntacticAnalyzer.updateGraph(graph, sTGD);
                                Predicate hAnn = sTGD.head.getAtoms().get(0).predicate;
                                String headAnnotation = hAnn.name.substring(hAnn.name.indexOf("@") + 1);
                                Predicate h = program.schema.predicates.get(hAnn.name.substring(0, hAnn.name.indexOf("@")));
                                if (!newShapes.containsKey(h)) newShapes.put(h, new HashSet<>());
                                newShapes.get(h).add(headAnnotation);
//                                System.out.println("Shape+tgd " + shape + "@" + tgd.hashCode() + " is applied!");
                            }
                        }
                    }
                }
            }
            deltaShapes = new HashMap<>(newShapes);
            newShapes = new HashMap<>();
//            System.out.println("finished the iteration...[#predicates with new shapes " + deltaShapes.size() + "]");
        }
        endTime = System.nanoTime();
        float t_graph_d = (endTime - startTime) / 1000000F;
        result.put(OntologyAnalyzer.TIME_GENERATE_DEP_GRAPH_D, t_graph_d);

        startTime = System.nanoTime();
        Set<Node> nodes = SyntacticAnalyzer.getNodesInSpecialCycle(graph, newProgram);
        endTime = System.nanoTime();
        float t_components = (endTime - startTime) / 1000000F;
        result.put(OntologyAnalyzer.TIME_CONNECTED_COMPONENT, t_components);
        result.put(OntologyAnalyzer.TIME_TERMINATES_GRAPH_D, (t_components+t_graph_d+t_shapes));
//        Set<Position> positions = SyntacticAnalyzer.getPositions(nodes);
//        boolean terminates = terminates(newProgram, graph, positions);
        boolean terminates = nodes.isEmpty();

        result.put(OntologyAnalyzer.NO_GRAPH_NODES_D, graph.keySet().size());
        result.put(OntologyAnalyzer.NO_GRAPH_EDGES_D, StatGeneratorSimple.countEdges(graph));
        result.put(OntologyAnalyzer.NO_GRAPH_SPECIAL_EDGES_D, StatGeneratorSimple.countSepcialEdges(graph));
        result.put(OntologyAnalyzer.NO_CONNECTED_COMPONENTS_D, newProgram.nComponents);
        result.put(OntologyAnalyzer.NO_SPECIAL_CONNECTED_COMPONENTS_D, newProgram.nSpecialComponents);

//        System.out.println("terminates = " + terminates);
        return terminates;
    }

    private static Set<String> getCompatibleShapes(String shape) {
        HashSet<String> res = new HashSet<>();
        /*HashSet<String> delta = new HashSet<>();
        delta.add("1");
        int current = 1;
        for (int i = 1; i < shape.length()+1; i++) {
            char c = shape.charAt(i);
            if (Integer.parseInt(c+"") == current) {
                for (String s : res) {
                    delta.add(s + current);
                }
            }
        }*/
        res.add(shape);
        return res;
    }



    private static String generateShape(TGD tgd) {
        PositiveAtom a = tgd.body.getAtoms().get(0);
        String[] vars = new String[a.predicate.arity];
        StringBuilder ann = new StringBuilder();
        int max = 1;
        int i = 0;
        for (Term term : a.terms) {
            vars[i] = term.toString();
            boolean repeated = false;
            int j;
            for (j = 0; j < i; j++) {
                if (vars[j].equals(vars[i])) {
                    repeated = true;
                    break;
                }
            }
            if (repeated) {
                ann.append(j+1);
            } else {
                ann.append(max);
                max++;
            }
            i++;
        }
        return ann.toString();
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
//        String pathname = "/home/cqadev/IdeaProjects/omd_rep/omd/dataset/test_data/test-complex.txt";
        String pathname = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\linear-test\\linear-2.txt";
        File in = new File(pathname);
        Program program = Parser.parseProgram(in);
//        if (!SyntacticAnalyzer.isSimpleLinear(program.tgds)) {
//            System.out.println("Not simple linear.");
//            return;
//        }
        Map<Position, Node> graph = buildDependencyGraph(program.tgds);
        Set<Node> nodes = SyntacticAnalyzer.getNodesInSpecialCycle(graph, program);
        Set<Position> positions = SyntacticAnalyzer.getPositions(nodes);
        boolean terminates = terminates(program, graph, positions);
        System.out.println("terminates = " + terminates);
    }

    public static void main1(String[] args) throws IOException, SQLException, ClassNotFoundException {
        long startTime;
        long endTime;
        Class.forName("ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer");
//        Program program = Parser.parseProgram(new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\linear-test\\linear-1.txt"));
        String pathname = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\linear\\";
        File dir = new File(pathname);
        String[] dbnames = {"smalldb", "mediumdb", "largedb"};
        File[] files = dir.listFiles((dir1, name) -> name.contains("program-shape"));
        Arrays.sort(files, Comparator.comparing(File::getName));
        for (String dbname : dbnames) {
            System.out.println("dbname = " + dbname);
            for (File file : files) {
                System.out.println("File = " + file.getName());
    //            System.out.println("Parsing the program...");
                startTime = System.nanoTime();
                Program program = Parser.parseProgram(file);
                endTime = System.nanoTime();
//                System.out.println("Parsing finished! Time " + ((endTime - startTime) / 1000000000F) + "\n\t");
                startTime = System.nanoTime();
                Map<String, Object> result = new HashMap<>();
                terminatesLinear(program, dbname, result);
                endTime = System.nanoTime();
                System.out.println("Time (sec) for termination check:" + ((endTime - startTime) / 1000000000F) + "\n\t");
            }
        }
    }
}
