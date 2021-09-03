package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ScenarioGenerator {
    public static void main(String[] args) throws IOException {
        File inputDir = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl-dl");
        File outputDir = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\owl-dl-scenarios");
        generateScenarios(inputDir, outputDir);
    }

    public static void generateScenarios(File in, File out) throws IOException {
        if (!out.exists()) out.mkdir();
        File[] files = in.listFiles();
        for (File file : files) {
            try {
                String filename = file.getName();
                String outfile = out.getAbsolutePath() + "\\" + filename.substring(0, filename.indexOf(".")) + ".xml";
                System.out.println("reading... " + file.getName());
                Program program = Parser.parseProgram(file);
                if (program.isEmpty()) {
                    System.out.println("did not translate file... " + file.getName() + " [no facts]");
                    continue;
                } else if (program.tgds.isEmpty()) {
                    System.out.println("did not translate file... " + file.getName() + " [no rules]");
                    continue;
                } else if (!SyntacticAnalyzer.isSimpleLinear(program.tgds)) {
                    System.out.println("did not translate file... " + file.getName() + " [not simple linear]");
                    continue;
                }
                File outputfile = new File(outfile);
                System.out.println("generating... " + file.getName());
                generateScenario(program, outputfile);
                System.out.println("finish file... " + file.getName());
            } catch (Exception e) {
                System.out.println("could not translate file... " + file.getName());
                e.printStackTrace();
            }
        }
    }

    public static void generateScenario(Program program, File outputfile) throws IOException {
        FileWriter out = new FileWriter(outputfile);
        Set<Predicate> edbPredicates = getExtensionalPredicates(program);
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<scenario>\n" +
                "    <source>\n" +
                "        <type>GENERATE</type>\n" +
                "        <generate>\n" +
                "<![CDATA[\n" +
                "SCHEMA:\n");
        writeSchema(edbPredicates, true, out);
        out.write("INSTANCE:\n");
        writeSourceInstance(edbPredicates, out);
        out.write("\n" +
                "]]>\n" +
                "        </generate>\n" +
                "    </source>\n" +
                "    <target>\n" +
                "        <type>GENERATE</type>\n" +
                "        <generate>\n" +
                "<![CDATA[\n" +
                "SCHEMA:\n");
        HashSet<Predicate> intentionalPredicates = new HashSet<>(program.schema.predicates.values());
        writeSchema(intentionalPredicates, false, out);
        out.write("\n" +
                "]]>\n" +
                "        </generate>\n" +
                "    </target>\n" +
                "    <dependencies>\n" +
                "<![CDATA[\n" +
                "STTGDs:\n");
        writeSTTGDs(edbPredicates, out);
        out.write("\n" +
                "ExtTGDs:\n");
        for (TGD tgd : program.tgds) {
            out.write(convertToScenarioTGD(tgd) + "\n");
        }
        out.write("\n" +
                "]]>\n" +
                "    </dependencies>\n" +
                "</scenario>\n");
/*

        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<scenario>\n" +
                "    <source>\n" +
                "        <type>GENERATE</type>\n" +
                "        <generate>\n" +
                "<![CDATA[\n" +
                "SCHEMA:\n" +
//                "R(a)\n" +
//                "U(a)\n" +
                sourceSchema + "\n" +
                "INSTANCE:\n" +
//                "R(a: \"a\")\n" +
//                "U(a: \"x\")\n" +
                sourceInstance + "\n" +
                "]]>\n" +
                "        </generate>\n" +
                "    </source>\n" +
                "    <target>\n" +
                "        <type>GENERATE</type>\n" +
                "        <generate>\n" +
                "<![CDATA[\n" +
                "SCHEMA:\n" +
//                "S(a, b)\n" +
//                "T(a, b)\n" +
                targetSchema + "\n" +
                "]]>\n" +
                "        </generate>\n" +
                "    </target>\n" +
                "    <dependencies>\n" +
                "<![CDATA[\n" +
                "STTGDs:\n" +
//                "U(a: $a) -> T(a: $a, b: $b).\n" +
//                "R(a: $a) -> S(a: $a, b: $b).\n" +
                stTGDs + "\n" +
                "ExtTGDs:\n" +
//                "S(a: $a, b: $b) -> S(a: $b, b: $c).\n" +
                targetTGDs + "\n" +
                "]]>\n" +
                "    </dependencies>\n" +
                "</scenario>\n";*/
        out.close();
    }

    private static void writeSTTGDs(Set<Predicate> edbPredicates, FileWriter out) throws IOException {
        for (Predicate predicate : edbPredicates) {
            String res = "";
            res += Parser.sanitizePredicateName(predicate.name) + "_DUMMY(";
            for (int i = 0; i < predicate.arity; i++) {
                res += "a" + i + ":$v" + i + ",";
            }
            res = res.substring(0, res.length()-1) + ")\n";
            res = res.substring(0, res.length()-1) + " -> ";
            res += Parser.sanitizePredicateName(predicate.name) + "(";
            for (int i = 0; i < predicate.arity; i++) {
                res += "a" + i + ":$v" + i + ",";
            }
            res = res.substring(0, res.length()-1) + ").\n";
            out.write(res);
        }
    }

    private static void writeSourceInstance(Set<Predicate> edbPredicates, FileWriter out) throws IOException {
        for (Predicate predicate : edbPredicates) {
            String res = "";
            res += Parser.sanitizePredicateName(predicate.name) + "_DUMMY(";
            for (int i = 0; i < predicate.arity; i++) {
                res += "a" + i + ": \"A\",";
            }
            res = res.substring(0, res.length()-1) + ")\n";
            out.write(res);
        }
    }

    private static Set<Predicate> getExtensionalPredicates(Program program) {
        Set<Predicate> predicates = new HashSet<>();
        for (Fact fact : program.edb.getFacts()) {
            predicates.add(fact.predicate);
        }
        return predicates;
    }

    private static void writeSchema(Set<Predicate> predicates, boolean source, FileWriter out) throws IOException {
        for (Predicate predicate : predicates) {
            out.write(generatePredicateSchema(predicate, source) + "\n");
        }
    }

    private static String generatePredicateSchema(Predicate predicate, boolean source) {
        String res = Parser.sanitizePredicateName(predicate.name) + (source ? "_DUMMY" : "") + "(";
        for (int i = 0; i < predicate.arity; i++) {
            res += "a" + i + ",";
        }
        return res.substring(0, res.length()-1) + ")";
    }

    private static String convertToScenarioTGD(TGD tgd) {
        String res = "";
        for (PositiveAtom atom : tgd.body.getAtoms()) {
            res += convertToScenarioAtom(atom) + ",";
        }
        res = res.substring(0, res.length()-1) + " -> ";
        for (PositiveAtom atom : tgd.head.getAtoms()) {
            res += convertToScenarioAtom(atom) + ",";
        }
        return res.substring(0, res.length()-1) + ".";
    }

    private static String convertToScenarioAtom(PositiveAtom atom) {
        String res = Parser.sanitizePredicateName(atom.predicate.name) + "(";
        int i = 0;
        for (Term term : atom.terms) {
            res += "a" + i + ":$v" + term.toString().replaceAll("@", "") + ",";
            i++;
        }
        return res.substring(0, res.length()-1) + ")";
    }
}
