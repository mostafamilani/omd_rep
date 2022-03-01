package ca.mcmaster.mmilani.omd.datalog.synthesizer;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.engine.Schema;
import ca.mcmaster.mmilani.omd.datalog.executer.AnalyzerExec;
import ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer;
import ca.mcmaster.mmilani.omd.datalog.primitives.*;
import ca.mcmaster.mmilani.omd.datalog.executer.DLGPGenerator;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ProgramGenerator {
    private static void generateRules(Program program, int n_rules, double r_exist) {
        System.out.println("The number of rules: " + n_rules);
        for (int i = 1; i <= n_rules; i++) {
//            generateSimpleLinearRule(program, r_exist);
            generateLinearRule(program, r_exist, true);
            if (i%500000==0)
                System.out.println(i + " rules are added!");
        }
    }

    private static TGD generateSimpleLinearRule(Program program, double r_exist) {
        TGD tgd = new TGD();
        tgd.body = generateRandomConjunct(program, 1, tgd);
        tgd.head = generateRandomConjunct(program, 1, tgd);

        int n_head_vars = tgd.head.getVariables().size();
        int existVars = randomInRange(new int[]{0, (int) Math.ceil(r_exist * n_head_vars)});
        fixExistentialVariables(tgd, existVars);
        program.tgds.add(tgd);
        return tgd;
    }

    private static TGD generateLinearRule(Program program, double r_exist, boolean policy) {
        TGD tgd = generateSimpleLinearRule(program, r_exist);
//        System.out.println("first tgd = " + tgd);
        PositiveAtom b = tgd.body.getAtoms().get(0);
        if (policy) {
            int index = new Random().nextInt(SyntacticAnalyzer.annotationCounts[b.predicate.arity-1]);
//            new Random().nextInt(max - min + 1) + min
            String ant = SyntacticAnalyzer.annotations[index];
            List<Variable> vs = new ArrayList<>(b.getVariables());
//            System.out.println("ant = " + ant);
//            System.out.println("b.predicate.arity = " + b.predicate.arity);
            for (int i = 0; i < b.predicate.arity; i++) {
//                System.out.println("i = " + i);
                int j = Integer.parseInt(ant.charAt(i) + "", 16)-1;
//                System.out.println("j = " + j);
                if (j != i) {
                    System.out.println("Replace " + vs.get(j) + " with " + vs.get(i));
                    replaceVarInConjunct(tgd.body, vs.get(i), vs.get(j));
                    replaceVarInConjunct(tgd.head, vs.get(i), vs.get(j));
                }
            }
//            System.out.println("tgd = " + tgd);
        } else {
            List vars = b.getVariables();
            b.terms = new ArrayList<>();
            for (int i = 0; i < vars.size(); i++) {
                b.terms.add((Term) vars.get(new Random().nextInt(vars.size())));
            }
        }
        return tgd;
    }

    private static double getRatio(String ratio) {
        return Double.parseDouble(ratio.substring(1,ratio.indexOf(")")));
    }

    private static void fixJoinVariables(TGD tgd, int nJoinVars) {
        List<Variable> bodyVars = tgd.body.getVariables();
        while(nJoinVars > 0 && tgd.body.getVariables().size() > 1) {
            Variable targetVar = (Variable) getRandomMember(bodyVars, nJoinVars);
            resolveVariable(tgd, targetVar);
            nJoinVars--;
        }
    }

    private static void fixExistentialVariables(TGD tgd, int nEVars) {
        List<Variable> headVars = tgd.head.getVariables();
        if (headVars.size() < nEVars) return;
        Set<Variable> targetVars = getRandomSubset(headVars, headVars.size() - nEVars);
        for (Variable variable : targetVars) {
            resolveVariable(tgd, variable);
        }
        setExistentialVariables(tgd);
    }

    public static void setExistentialVariables(TGD tgd) {
        for (Variable hVar : tgd.head.getVariables()) {
            if (!tgd.body.getVariables().contains(hVar))
                tgd.existentialVars.add(hVar);
        }
    }

    private static void resolveVariable(TGD tgd, Variable variable) {
        List<Variable> bVars = tgd.body.getVariables();
        Variable bVar = (Variable) getRandomMember(bVars, variable);
        if (bVar == null) return;
        replaceVarInConjunct(tgd.body, variable, bVar);
        replaceVarInConjunct(tgd.head, variable, bVar);
        tgd.variables.remove(variable.toString());
    }

    private static void replaceVarInConjunct(Conjunct conjunct, Variable v1, Variable v2) {
        for (PositiveAtom atom : conjunct.getAtoms()) {
            if (atom.terms.contains(v1))
                atom.terms = replaceVarInTerms(atom.terms, v1, v2);
        }
    }

    private static List<Term> replaceVarInTerms(List<Term> terms, Variable v1, Variable v2) {
        ArrayList<Term> result = new ArrayList<>();
        for (Term term : terms) {
            if (term.equals(v1))
                result.add(v2);
            else
                result.add(term);
        }
        return result;
    }

    public static int randomInRange(int[] range) {
        return ThreadLocalRandom.current().nextInt(range[0], range[1] + 1);
    }

    private static Conjunct generateRandomConjunct(Program program, int nAtoms, TGD tgd) {
        Conjunct conjunct = new Conjunct();
        for (int i = 0; i < nAtoms; i++) {
            ArrayList<Term> terms = new ArrayList<>();
            Predicate predicate = (Predicate) getRandomMember(program.schema.predicates.values());
            for (int j = 0; j < predicate.arity; j++) {
                terms.add(tgd.fetchNewVariable());
            }
            conjunct.add(new PositiveAtom(predicate, terms));
        }
        return conjunct;
    }

    private static Object getRandomMember(Collection set) {
        int size = set.size();
        int item = new Random().nextInt(size);
        int i = 0;
        for (Object obj : set) {
            if (i == item)
                return obj;
            i++;
        }
        return null;
    }

    private static Object getRandomMember(Collection set, Object exclude) {
        if (set.size() == 1 && set.contains(exclude))
            return null;
        Object randomMember = exclude;
        while(exclude.equals(randomMember)) {
            randomMember = getRandomMember(set);
        }
        return randomMember;
    }

    private static Set getRandomSubset(Collection set, int size) {
        HashSet subset = new HashSet();
        while(subset.size() < size) {
            Object randomMember = getRandomMember(set);
            if (subset.contains(randomMember)) continue;
            subset.add(randomMember);
        }
        return subset;
    }

    public static int[] getRange(String range) {
        if (!range.contains("[")) {
            int a = Integer.parseInt(range);
            return new int[]{a, a};
        }
        int a = Integer.parseInt(range.substring(range.indexOf("[")+1, range.indexOf("-")));
        int endIndex = range.indexOf("]");
        if (endIndex == -1) endIndex = range.indexOf(")");
        int b = Integer.parseInt(range.substring(range.indexOf("-")+1, endIndex));
        return new int[]{a, b};
    }

    private static void createProgramSchema(Program program, Schema schema, int n_predicates, int[] arityRange) {
        Collection<Predicate> predicates = new HashSet<>();
        predicates.addAll(schema.predicates.values());
        while (n_predicates > 0) {
            String name = ((Predicate) getRandomMember(predicates)).name;
            int arity = schema.predicates.get(name).arity;
            if (isInRange(arity, arityRange) && !program.schema.predicates.containsKey(name)) {
                n_predicates--;
                Predicate predicate = program.schema.fetchPredicate(name, arity);
                predicates.remove(predicate);
            }
        }
    }

    private static String printRange(int[] range) {
        return "[" + range[0] + "," + range[1] + "]";
    }

    private static boolean isInRange(int i, int[] range) {
        return i <= range[1] && i >= range[0];
    }

    public static String randomProfile(String property) {
        int profileIndex = ThreadLocalRandom.current().nextInt(1, countChars(property, ",") + 2);
        String profile = "";
        while (profileIndex > 0) {
            if (property.contains(",")) {
                profile = property.substring(0, property.indexOf(","));
                property = property.substring(property.indexOf(",") + 1);
                profileIndex--;
            } else {
                return property;
            }
        }
        return profile;
    }

    private static int countChars(String property, String c) {
        int count = 0;
        for (int i = 0; i < property.length(); i++) {
            if (property.charAt(i) == c.charAt(0)) count++;
        }
        return count;
    }

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("Loading schema information...");
        Properties dbprop = new Properties();
        dbprop.load(DataGenerator.class.getResourceAsStream("..\\..\\..\\..\\..\\..\\db-config.properties"));

        String user = dbprop.get("user").toString();
        String pass = dbprop.get("password").toString();
        String url = "jdbc:postgresql://localhost/newdb?user=" + user + "&password=" + pass;
        Connection conn = DriverManager.getConnection(url, user, pass);
        Schema schema = Schema.loadSchema(conn);

        /*InputStream input = new FileInputStream("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\src\\rules.properties");*/
        InputStream input = new FileInputStream(AnalyzerExec.getOptionValue(args, "-g", 1));
        Properties prop = new Properties();
        prop.load(input);

        int n_programs = Integer.parseInt(prop.get("programs").toString());

        System.out.println("The number of programs in each profile: " + n_programs);

        String[] predicateProfiles = getProfiles(prop.get("predicates").toString());
        String[] arityProfiles = getProfiles(prop.get("arity").toString());
        String[] ruleProfiles = computeProfiles(prop.get("rules").toString());

        int i = 1;
        for (String p_profile : predicateProfiles) {
            System.out.println("Generating programs in predicate profile " + p_profile);
                for (String a_profile : arityProfiles) {
                    System.out.println("Generating programs in arity profile " + a_profile);
                    for (String r_profile : ruleProfiles) {
                        System.out.println("Generating programs in rules profile " + r_profile);
                    for (int j = 0; j < n_programs; j++) {
                        int[] per_range = getRange(p_profile);
                        per_range[1] = per_range[1] - 1;
                        int n_predicates = randomInRange(per_range);
                        int[] arity_range = getRange(a_profile);
                        int[] rules_range = getRange(r_profile);
                        rules_range[1] = rules_range[1] - 1;
                        int n_rules = randomInRange(rules_range);
                        String exist_vars = randomProfile(prop.get("exist_vars").toString());
                        double r_exist = getRatio(exist_vars);

                        System.out.println("Program #" + i +
                                ", n_predicate " + n_predicates +
                                ", arity range [" + arity_range[0] + "," + arity_range[1] + "]" +
                                ", n_rule " + n_rules);
                        String outfilename = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\synthetic-linear\\program-" + i + ".txt";
                        try {
                            generateProgram(schema, outfilename, p_profile, a_profile, r_profile, n_predicates, arity_range, n_rules, r_exist);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        i++;
                    }
                }
            }
        }
    }

    private static void generateProgram(Schema schema, String outfilename, String p_profile, String a_profile, String r_profile, int n_predicates, int[] arity_range, int n_rules, double r_exist) throws IOException {
        Program program = new Program();
        program.schema = new Schema();
        program.externalParams.put("rule_profile", r_profile);
        program.externalParams.put("predicate_profile", p_profile);
        program.externalParams.put("arity_profile", a_profile);
        Schema localSchema = pruneSchema(schema, arity_range, n_predicates);
        createProgramSchema(program, localSchema, n_predicates, arity_range);
        generateRules(program, n_rules, r_exist);

        File outfile = new File(outfilename);
        DLGPGenerator.printProgram(outfile, program, true);
    }

    private static Schema pruneSchema(Schema schema, int[] arity_range, int n_predicates) {
        Schema res = new Schema();
        for (Predicate predicate : schema.predicates.values()) {
            if (isInRange(predicate.arity, arity_range)) res.fetchPredicate(predicate.name, predicate.arity);
        }
        if (res.predicates.values().size() < n_predicates) {
            throw new RuntimeException("Not enough predicates!");
        }
        return res;
    }

    private static String[] computeProfiles(String configs) {
        int count = Integer.parseInt(configs.substring(configs.indexOf(":")+1));
        int begin = Integer.parseInt(configs.substring(1, configs.indexOf("-")));
        int end = Integer.parseInt(configs.substring(configs.indexOf("-")+1, configs.indexOf(")")));

        int size = (end-begin)/count;
        String[] result = new String[count];
        for(int i = 0; i < count; i++) {
            result[i] = "[" + (i*size) + "-" + ((i+1)*size) + ")";
        }
        return result;
    }

    private static String[] getProfiles(String configs) {
        return configs.split(",");
    }

    public static void main1(String[] args) throws IOException, SQLException {
        System.out.println("Loading schema information...");
        Properties dbprop = new Properties();
        dbprop.load(DataGenerator.class.getResourceAsStream("..\\..\\..\\..\\..\\..\\db-config.properties"));

        String user = dbprop.get("user").toString();
        String pass = dbprop.get("password").toString();
        String url = "jdbc:postgresql://localhost/smalldb?user=" + user + "&password=" + pass;
        Connection conn = DriverManager.getConnection(url, user, pass);
        Schema schema = Schema.loadSchema(conn);

//        int[] arities = {2, 5, 10};
//        String[] policies = {"shape", "indp-vars"};
//        int[] ruleCounts = {100, 1000, 10000, 100000, 1000000};

        int[] arities = {10};
        String[] policies = {"shape"};
        int[] ruleCounts = {1000000};
        for (int arity : arities) {
            for (int n_rules : ruleCounts) {
                for (String policy : policies) {
                    String outfilename = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\linear\\program-" +
                            policy + "-rules" + n_rules + "-arity" + arity + ".txt";
                    Program program = new Program();
                    program.schema = new Schema();
                    int[] arity_range = {1, arity};
                    int n_predicates = 50;
                    Schema localSchema = pruneSchema(schema, arity_range, n_predicates);
                    createProgramSchema(program, localSchema, n_predicates, arity_range);
                    System.out.println("The number of rules: " + n_rules);
                    double r_exist = 0.1;
                    for (int i = 1; i <= n_rules; i++) {
                        generateLinearRule(program, r_exist, policy.equals("shape"));
                    }
                    File outfile = new File(outfilename);
                    outfile.createNewFile();
                    DLGPGenerator.printProgram(outfile, program, true);
                }
            }
        }
    }
}
