package ca.mcmaster.mmilani.omd.datalog;

import ca.mcmaster.mmilani.omd.datalog.primitives.Atom;
import ca.mcmaster.mmilani.omd.datalog.primitives.Fact;
import ca.mcmaster.mmilani.omd.datalog.primitives.Query;
import ca.mcmaster.mmilani.omd.datalog.primitives.Rule;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Parser {
    File file;

    public Program parseProgram() throws IOException {
        Program program = new Program();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while((line = reader.readLine()) != null) {
            if (line.contains("?") || line.contains("%")) continue;
            if (line.contains(":-"))
                program.rules.add(parseRule(line));
            else
                program.edb.facts.add((Fact) Atom.parse(line.substring(0, line.length()-1)));
        }
        return program;
    }

    public void init(File file) throws FileNotFoundException {
        this.file = file;
    }

    public static Rule parseRule(String line) {
        Rule rule = new Rule();
        String head = "";
        line = line.replaceAll(
                " ", "");
        rule.body = parseBody(line.substring(line.indexOf(":-")+2, line.length()-1), rule);
        if (!line.contains("!")) {
            head = line.substring(0,line.indexOf(":-"));
            rule.head = Atom.parse(head, rule);
        }
        return rule;
    }

    public static List<Atom> parseBody(String body, Rule... rule) {
        ArrayList<Atom> atoms = new ArrayList<>();
        while(body.length() > 1) {
            String atom = body.substring(0,body.indexOf(")")+1);
            body = body.substring(body.indexOf(")")+1, body.length());
            atoms.add(Atom.parse(atom, rule));
        }
        return atoms;
    }

    public List<Query> parseQueries() throws IOException {
        ArrayList<Query> queries = new ArrayList<>();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        while((line = reader.readLine()) != null) {
            if (line.contains("%")) continue;
            if (line.contains("?")) {
                queries.add(parseQuery(line));
            }
        }
        return queries;

    }

    private Query parseQuery(String line) {
        Query query = new Query();
        line = line.replaceAll(
                " ", "");
        query.body = parseBody(line.substring(0, line.indexOf("?")), query);
        return query;
    }
}
