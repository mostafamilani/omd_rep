package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.engine.PersistantDatabase;
import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.primitives.Node;
import ca.mcmaster.mmilani.omd.datalog.primitives.Position;
import ca.mcmaster.mmilani.omd.datalog.primitives.Predicate;
import ca.mcmaster.mmilani.omd.datalog.primitives.TGD;
/*import it.unibas.lunatic.LunaticConfiguration;
import it.unibas.lunatic.Scenario;
import it.unibas.lunatic.model.chase.chasede.DEChaserFactory;
import it.unibas.lunatic.model.chase.chasede.operators.mainmemory.TerminationException;
import it.unibas.lunatic.model.chase.chaseded.DEDChaserFactory;
import it.unibas.lunatic.model.chase.commons.ChaseStats;
import it.unibas.lunatic.model.chase.commons.operators.ChaserFactoryMC;
import it.unibas.lunatic.persistence.DAOConfiguration;
import it.unibas.lunatic.persistence.DAOMCScenario;
import it.unibas.lunatic.utility.LunaticUtility;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import speedy.utility.PrintUtility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer.*;
import static ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer.isWeaklyAcyclic;
import static ca.mcmaster.mmilani.omd.datalog.executer.TerminationAnalyzer.terminates;

public class OntologyAnalyzer {
    public static Logger logger = LoggerFactory.getLogger(OntologyAnalyzer.class);

//    private final static DAOMCScenario daoScenario = new DAOMCScenario();


    public static final String AVG_ARITY = "avg_arity";
    public static final String MIN_ARITY = "min_arity";
    public static final String MAX_ARITY = "max_arity";
    public static final String NO_EVARS = "n_exist_vars";
    public static final String TIME_GENERATE_DEP_GRAPH = "t_graph";
    public static final String TIME_GENERATE_DEP_GRAPH_D = "t_graph_d";
    public static final String TIME_PARSING = "t_parse";
    public static final String TIME_CONNECTED_COMPONENT = "t_component";
    public static final String TIME_TERMINATES_GRAPH = "t_terminate_graph";
    public static final String TIME_TERMINATES_CHASE = "t_lunatic";
    public static final String TIME_FIND_SHAPES = "t_shapes";
    public static final String TIME_TERMINATES_GRAPH_D = "t_terminate_graph_d";

    public static final String NO_RULES = "n_rules";
    public static final String NO_PREDICATES = "n_predicates";
    public static final String NO_DATA_SIZE = "n_facts";
    public static final String NO_DATA_SHAPES = "n_shapes";
    public static final String NO_CONNECTED_COMPONENTS = "n_components";
    public static final String NO_SPECIAL_CONNECTED_COMPONENTS = "n_spacial_components";
    public static final String NO_GRAPH_NODES = "n_nodes";
    public static final String NO_GRAPH_EDGES = "n_edges";
    public static final String NO_GRAPH_SPECIAL_EDGES = "n_special_edges";
    public static final String NO_CONNECTED_COMPONENTS_D = "n_components_d";
    public static final String NO_SPECIAL_CONNECTED_COMPONENTS_D = "n_spacial_components_d";
    public static final String NO_GRAPH_NODES_D = "n_nodes_d";
    public static final String NO_GRAPH_EDGES_D = "n_edges_d";
    public static final String NO_GRAPH_SPECIAL_EDGES_D = "n_special_edges_d";
    public static final String TERMINATES_GRAPH = "terminates_graph";
    public static final String TERMINATES_CHASE = "terminates_lunatic";
    public static final String LINEAR = "linear";
    public static final String SIMPLE_LINEAR = "s_linear";
    public static final String WEAKLY_ACYCLIC = "weakly_acyclic";

    public static void main(String[] args) throws Exception {
        String filepath = args[0];
        System.out.println("filepath = " + filepath);
        String dirpath = filepath.substring(0, filepath.lastIndexOf("\\"));

        String ontologyName = filepath.substring(filepath.lastIndexOf("\\") + 1, filepath.lastIndexOf("."));
        String resultFileName = dirpath + "\\" + ontologyName + ".res";
        boolean exists = new File(resultFileName).exists();
        if (!AnalyzerExec.checkOption(args, "-r") && exists)
        {
            System.out.println(ontologyName + " is already processed!");
            return;
        }

        Map<String, Object> result = new HashMap<>();

        /* Parsing */
        long endTime, startTime = System.nanoTime();
        Program program = Parser.parseProgram(new File(filepath));
        result.putAll(program.externalParams);
        endTime = System.nanoTime();
        result.put(TIME_PARSING, (endTime - startTime) / 1000000F);

        /* Loading EDB information */
        /*if (AnalyzerExec.checkOption(args, "-d")) {
            String dbname = AnalyzerExec.getOptionValue(args, "-d", 1);
            String configFile = AnalyzerExec.getOptionValue(args, "-d", 2);
            program.loadRecordCounts(dbname, configFile);
        }*/

        /* Termination analysis only works for simple linear onotlogies.
           The result is trivial if the ontology's ABox or TBox is empty */
        if (!isSimpleLinear(program.tgds)) {
            System.out.println(ontologyName + " is not simple linear!");
            return;
        } else if (program.isEmpty() || program.tgds.isEmpty()) {
            System.out.println(ontologyName + " trivially terminates!");
            return;
        }

        /* termination analysis */
        System.out.println("Start graph-based analysis! " + program.name);
        processSyntax(result, program);
//        System.out.println("Start chase-based analysis! (Lunatic) " + program.name);
//        runChaseLunatic(result, program, dirpath, args);


        /* Exporting the results */
        exportResults(resultFileName, result, AnalyzerExec.checkOption(args, "-a"));
        System.out.println("Processing " + program.name + " completed!");
    }

    /* Write the result map as key values in the file specified by "filename"
    * */
    public static void exportResults(String filename, Map<String, Object> result, boolean append) throws IOException {
        File outfile = new File(filename);
        if (!outfile.exists())
            outfile.createNewFile();
        FileWriter out = new FileWriter(outfile, append);
        List<String> keys = new ArrayList(result.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            if (!append || key.equals(TIME_TERMINATES_GRAPH))
                out.write(key + ": " + result.get(key) + "\n");
        }
        out.close();
    }

//    public static void runChaseLunatic(Map<String, Object> result, Program program, String dirpath, String[] inOptions) throws Exception {
//        System.out.println("Running the chase for " + program.name + "...");
//        String scenarioFileName = dirpath + "\\" + program.name + ".xml";
//        File outputfile = new File(scenarioFileName);
//        if (!AnalyzerExec.checkOption(inOptions, "-r") || !new File(scenarioFileName).exists())
//            ScenarioGenerator.generateScenario(program, outputfile);
//
//        try {
//            List<String> options = new ArrayList<String>();
//            options.add(scenarioFileName);
//            options.add("-printTargetStats=true");
//            options.add("-chaseMode=unrestricted-skolem");
//            options.add("-printsteps=true");
//            String fileScenario = options.get(0);
//            String chaseMode = LunaticUtility.getChaseMode(options);
//            DAOConfiguration daoConfig = new DAOConfiguration();
//            daoConfig.setImportData(false);
//            daoConfig.setUseEncodedDependencies(true);
//            daoConfig.setUseCompactAttributeName(true);
//            daoConfig.setChaseMode(chaseMode);
//            LunaticUtility.applyCommandLineOptions(daoConfig, options);
//            Scenario scenario = daoScenario.loadScenario(fileScenario, daoConfig);
//            LunaticConfiguration conf = scenario.getConfiguration();
//            LunaticUtility.applyCommandLineOptions(conf, options);
//            conf.setCleanSchemasOnStartForDEScenarios(false);
//            conf.setRecreateDBOnStart(false);
//            conf.setExportSolutions(false);
//            conf.setExportChanges(false);
//            conf.setPrintResults(false);
//            scenario.getQueries().clear();//Handled in MainExpExport
//            scenario.getSQLQueries().clear(); //Handled in MainExpExport
//            if (scenario.isDEDScenario()) {
//                DEDChaserFactory.getChaser(scenario).doChase(scenario);
//            } else if (scenario.isDEScenario()) {
//                DEChaserFactory.getChaser(scenario).doChase(scenario);
//            } else if (scenario.isMCScenario()) {
//                ChaserFactoryMC.getChaser(scenario).doChase(scenario);
//            } else {
//                throw new IllegalArgumentException("Scenario non supported!");
//            }
//            if (LunaticConfiguration.isPrintSteps()) System.out.println(ChaseStats.getInstance().toString());
//            PrintUtility.printMessage("-> ST-TGD time: " + ChaseStats.getInstance().getStat(ChaseStats.STTGD_TIME) + " ms");
//            result.put(TERMINATES_CHASE, true);
//        } catch (TerminationException e) {
//            result.put(TERMINATES_CHASE, false);
//        }
//        result.put(TIME_TERMINATES_CHASE, ChaseStats.getInstance().getStat(ChaseStats.TGD_TIME));
//    }

    public static void processSyntax(Map<String, Object> result, Program program) throws IOException {
            long endTime, startTime;
            startTime = System.nanoTime();
            Map<Position, Node> graph = buildDependencyGraph(program.tgds);
            endTime = System.nanoTime();
            float t_graph = (endTime - startTime) / 1000000F;
            result.put(TIME_GENERATE_DEP_GRAPH, t_graph);

            startTime = System.nanoTime();
            Set<Node> nodesInSpecialCycle = getNodesInSpecialCycle(graph, program);
            Set<Position> loopPositions = getPositions(nodesInSpecialCycle);
            endTime = System.nanoTime();
            float t_components = (endTime - startTime) / 1000000F;
            result.put(TIME_CONNECTED_COMPONENT, t_components);

            Set<Position> infiniteRankPositions = getInfiniteRankPositions(graph, nodesInSpecialCycle);

            startTime = System.nanoTime();
            boolean terminates = terminates(program, graph, loopPositions);
            endTime = System.nanoTime();
            result.put(TIME_TERMINATES_GRAPH, ((endTime - startTime) / 1000000F) + t_graph + t_components);

            result.put(NO_RULES, program.tgds.size());
            result.put(NO_DATA_SIZE, computeDBSize(program));
            result.put(NO_GRAPH_NODES, graph.keySet().size());
            result.put(NO_GRAPH_EDGES, StatGeneratorSimple.countEdges(graph));
            result.put(NO_GRAPH_SPECIAL_EDGES, StatGeneratorSimple.countSepcialEdges(graph));
            result.put(NO_CONNECTED_COMPONENTS, program.nComponents);
            result.put(NO_SPECIAL_CONNECTED_COMPONENTS, program.nSpecialComponents);
            result.put(NO_EVARS, computeTotalExistsVars(program));
            result.put(WEAKLY_ACYCLIC, isWeaklyAcyclic(program.tgds, infiniteRankPositions));
            result.put(TERMINATES_GRAPH, terminates);
            computeArityInfo(program, result);
            System.out.println("Graph-based analysis " + result.get(TIME_TERMINATES_GRAPH) + " ms\n\n");
    }

    public static int computeTotalExistsVars(Program program) {
        int count = 0;
        for (TGD tgd : program.tgds) {
            count += tgd.existentialVars.size();
        }
        return count;
    }

    public static int computeDBSize(Program program) {
        int size = 0;
        for (Integer count : program.edb.recordCount.values()) {
            size += count;
        }
        return size;
    }

    public static void computeArityInfo(Program program, Map<String, Object> result) {
        double sum = 0, count = 0, min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (Predicate predicate : program.schema.predicates.values()) {
            sum += predicate.arity;
            count++;
            min = Math.min(predicate.arity, min);
            max = Math.max(predicate.arity, max);
        }
        result.put(AVG_ARITY, sum/count);
        result.put(NO_PREDICATES, count);
        result.put(MIN_ARITY, min);
        result.put(MAX_ARITY, max);
    }
}
