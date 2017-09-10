package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static Program parseProgram(File file) throws IOException {
        Program program = new Program();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while ((line = reader.readLine()) != null) {
            if (line.contains("?") || line.contains("%")) continue;
            if (line.contains(":-")) {
                Rule rule = parseRule(line);
                rule.addProgram(program);
            }
            else
                program.edb.facts.add((Fact) Atom.parse(line.substring(0, line.length() - 1), false));
        }
        return program;
    }

    private static Rule parseRule(String line) {
        String head = "";
        line = line.replaceAll(
                " ", "");
        Rule rule;
        if (line.startsWith(":-")) {
            rule = new NC();
        } else if (line.contains("=")) {
            rule = new EGD();
        } else {
            rule = new TGD();
        }
        rule.body = parseBody(line.substring(line.indexOf(":-") + 2, line.length() - 1), rule);
        if (rule instanceof EGD || rule instanceof TGD) {
            head = line.substring(0, line.indexOf(":-"));
            rule.head = Atom.parse(head, false, rule);
        }
        return rule;
    }

    private static Conjunct parseBody(String body, Rule... rule) {
        Conjunct conjunct = new Conjunct();
        conjunct.atoms = new ArrayList<>();
        while (body.length() > 1) {
            String atom = body.substring(0, body.indexOf(")") + 1);
            body = body.substring(body.indexOf(")") + 1, body.length());
            conjunct.atoms.add(Atom.parse(atom, true, rule));
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
        query.body = parseBody(line.substring(0, line.indexOf("?")), query);
        String head = line.substring(0, line.indexOf("?-"));
        query.head = Atom.parse(head, false, query);
        return query;
    }
}
