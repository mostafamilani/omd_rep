package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    public static Program parseProgram(File file) throws IOException {
        Predicate.renew();
        Program program = new Program();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null) {
//            System.out.println("line = " + line);
            if (line.replaceAll(
                    " ", "").equals("") || line.contains("?") || line.contains("%")) continue;
            if (line.equals("Prefixes: [") || line.equals("Statistics: [")) line = skipToNextLine(reader);
            if (line == null) continue;
            if (line.equals("Deterministic DL-clauses: [") ||
                    line.equals("Disjunctive DL-clauses: [") ||
                    line.equals("ABox: [") ||
                    line.equals("]")) continue;
            if (line.contains(" v ")) line = line.replaceAll(" v ", ",");
            if (line.contains("=")) continue;
            if (line.replaceAll(" ", "").startsWith(":-")) continue;
            if (line.contains("atLeast")) {
                line = cleanRule(line);
            }
            try {
                if (line.contains(":-")) {
                    Rule rule = parseRule(line);
                    rule.addProgram(program);
                }
                else {
    //                program.edb.facts.add((Fact) Atom.parse(line.substring(0, line.length() - 1), false));

                        program.edb.facts.add((Fact) Atom.parse(line, false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return program;
    }

    private static String cleanRule(String line) {
        StringTokenizer t = new StringTokenizer(line, " ()\n\t");
        t.nextToken();t.nextToken();
        String p = t.nextToken().replaceAll(" ", "");
        String q = t.nextToken().replaceAll(" ", "");
        String x = t.nextToken().replaceAll(" ", "");
        return p + "(" + x + ",ZZ)," + q + "(" + x + ")" + line.substring(line.indexOf(":-"), line.length());
    }

    private static String skipToNextLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.equals("]")) return reader.readLine();
        }
        return line;
    }

    private static Rule parseRule(String line) {
//        System.out.println("line = " + line);
        String head = "";
//        line = line.replaceAll(" ", "");
        Rule rule;
        if (line.startsWith(":-")) {
            rule = new NC();
        } else if (line.contains("=")) {
            rule = new EGD();
        } else {
            rule = new TGD();
        }
        rule.body = parseConjunct(line.substring(line.indexOf(":-") + 2), true, rule);
        if (rule instanceof  TGD) {
            TGD tgd = (TGD) rule;
            rule.head = parseConjunct(line.substring(0, line.indexOf(":-")), false, rule);
            tgd.existentialVars = new HashSet<>(tgd.head.getVariables());
            tgd.existentialVars.removeAll(tgd.body.getVariables());
        } else if (rule instanceof EGD) {
            head = line.substring(0, line.indexOf(":-"));
            rule.head = Atom.parse(head, false, rule);
        }
        return rule;
    }

    public static Conjunct parseConjunct(String s, boolean body, Rule... rule) {
        Conjunct conjunct = new Conjunct();
        StringTokenizer tokenizer = new StringTokenizer(s, " \t\r(", true);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken(" \t\r(");
            if (!token.startsWith("!") && !token.equals(" ")) {
                if (token.startsWith(",")) token = token.substring(1);
                String atomStr = token;
                atomStr += tokenizer.nextToken(")");
                atomStr += tokenizer.nextToken();
                conjunct.add((PositiveAtom) Atom.parse(atomStr, body, rule));
            }
        }
        return conjunct;
    }

    public static List<CQ> parseQueries(File file) throws IOException {
        ArrayList<CQ> queries = new ArrayList<>();
        String line;
        BufferedReader in = new BufferedReader(new FileReader(file));
        while ((line = in.readLine()) != null) {
            if (line.contains("%")) continue;
            if (line.contains("?")) {
                queries.add(parseQuery(line));
            }
        }
        return queries;

    }

    private static CQ parseQuery(String line) {
        CQ query = new CQ();
        line = line.replaceAll(
                " ", "");
        String body = line.substring(line.indexOf("?-") + 2, line.length()-1);
        String head = line.substring(0, line.indexOf("?-"));
        query.body = parseConjunct(body, true, query);
        query.head = Atom.parse(head, false, query);
        return query;
    }

    public static void main2(String[] args) throws IOException {
        FileWriter writer = new FileWriter("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\run.bat");

        for (int i = 1; i <= 797; i++) {
            String fname = "00";
            if (i<10) fname += "00" + i; else
            if (i<100) fname += "0" + i; else
                fname += i;
            String in = "http://krr-nas.cs.ox.ac.uk/ontologies/UID/" + fname + ".owl";
            String out = "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\processed\\" + fname + ".txt";
            String command = "java -jar C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\lib\\HermiT.jar " + in + " --dump-clauses=" + out + "\n";
            writer.write(command);
        }
    }

    public static void main(String[] args) {
        String s = cleanRule("atLeast(1 acgt:reveals acgt:TotalBilirubin)(X) :- acgt:TotalBilirubinTest(X)");
        System.out.println("s = " + s);
    }
}
