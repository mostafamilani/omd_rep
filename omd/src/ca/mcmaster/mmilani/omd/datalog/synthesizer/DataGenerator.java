package ca.mcmaster.mmilani.omd.datalog.synthesizer;

import ca.mcmaster.mmilani.omd.datalog.engine.PersistantDatabase;
import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.engine.Schema;
import ca.mcmaster.mmilani.omd.datalog.executer.AnalyzerExec;
import ca.mcmaster.mmilani.omd.datalog.primitives.Predicate;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DataGenerator {
    private static int domainSize;

    public static void main(String[] args) throws IOException, SQLException {
        if (AnalyzerExec.checkOption(args, "-s"))
            createDatabases();
        if (AnalyzerExec.checkOption(args, "-d"))
            fillDatabases();
        if (AnalyzerExec.checkOption(args, "-p"))
            printDBStats();
    }

    private static void fillDatabases() throws SQLException, IOException {
        Properties prop = new Properties();
        prop.load(DataGenerator.class.getResourceAsStream("..\\..\\..\\..\\..\\..\\db-config.properties"));
        domainSize = Integer.parseInt(prop.get("domainsize").toString());

        /* create db schema */
        String user = prop.get("user").toString();
        String pass = prop.get("password").toString();
        for (Object o : prop.keySet()) {
            String dbname = (String) o;
            if (dbname.endsWith("db")) {
                String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass;
                Connection conn = DriverManager.getConnection(url, user, pass);
                Schema schema = Schema.loadSchema(conn);
                System.out.println("Loaded database schema for " + dbname);
                fillDatabases(conn, Integer.parseInt((String) prop.get(dbname)), schema);
                System.out.println("Filled database " + dbname);
                conn.close();
            }
        }
    }

    private static void printDBStats() throws SQLException, IOException {
        Properties prop = new Properties();
        prop.load(DataGenerator.class.getResourceAsStream("..\\..\\..\\..\\..\\..\\db-config.properties"));

        for (Object o : prop.keySet()) {
            String dbname = (String) o;
            if (dbname.endsWith("db")) {
                String user = prop.get("user").toString();
                String pass = prop.get("password").toString();
                String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass;
                Connection conn = DriverManager.getConnection(url, user, pass);
                Schema schema = Schema.loadSchema(conn);
                Program program = new Program();
                program.schema = schema;
                PersistantDatabase edb = new PersistantDatabase();
                edb.program = program;
                program.edb = edb;
                edb.connect(conn);
                System.out.println("Printing information about " + dbname);
                int total = 0;
                for (Predicate predicate : schema.predicates.values()) {
                    System.out.println(predicate.name + "\t" + predicate.arity + "\t" + edb.recordCount.get(predicate.name));
                    total += edb.recordCount.get(predicate.name);
                }
                System.out.println("Total records = " + total);
            }
        }
    }

    private static void createDatabases() throws IOException, SQLException {
        Properties prop = new Properties();
        prop.load(DataGenerator.class.getResourceAsStream("..\\..\\..\\..\\..\\..\\db-config.properties"));

        /* generate schema information */
        Map<String, Integer> schemaToGenerate = generateSchemaInfo(ProgramGenerator.getRange(prop.getProperty("arity")), Integer.parseInt(prop.get("relations").toString()));
        Set<String> ddlQueries = generateDDLQueries(schemaToGenerate);

        /* create db schema */
        String user = prop.get("user").toString();
        String pass = prop.get("password").toString();
        for (Object o : prop.keySet()) {
            String dbname = (String) o;
            if (dbname.endsWith("db")) {
                String url = "jdbc:postgresql://localhost/" + dbname + "?user=" + user + "&password=" + pass;
                Connection conn = DriverManager.getConnection(url, user, pass);
                createSchema(conn, ddlQueries);
                System.out.println("Created database schema for " + dbname);
                conn.close();
            }
        }
    }

    private static void fillDatabases(Connection conn, int no_records, Schema schema) throws SQLException {
        Map<Predicate, Integer> recordCounts = new HashMap<>();
        int pCount = schema.predicates.values().size();
        Predicate[] predicates = new Predicate[pCount];
        int i = 0;
        for (Predicate predicate : schema.predicates.values()) {
            predicates[i] = predicate; i++;
            recordCounts.put(predicate, 0);
        }
        for (int j = 0; j < no_records; j++) {
            Predicate predicate = predicates[ProgramGenerator.randomInRange(new int[]{0, pCount-1})];
            Integer count = recordCounts.get(predicate); count++;
            recordCounts.put(predicate, count);
        }
        for (Predicate predicate : recordCounts.keySet()) {
            insertRandomRecords(conn, predicate, recordCounts.get(predicate));
            System.out.println("Finished table: " + predicate.name);
        }
    }

    private static Set<String> generateDDLQueries(Map<String, Integer> schema) {
        HashSet<String> queries = new HashSet<>();
        for (String relation : schema.keySet()) {
            queries.add(createTableQuery(relation, schema.get(relation)));
        }
        return queries;
    }

    private static String createTableQuery(String tableName, int arity) {
        String columnDefinitions = "";
        for (int i = 0; i < arity; i++) {
            columnDefinitions += "c_" + i + " TEXT,";
        }
        columnDefinitions = columnDefinitions.substring(0, columnDefinitions.length()-1);
        String query = "create table " + tableName + " (" +
                columnDefinitions +
                ");";
        return query;
    }

    private static Map<String, Integer> generateSchemaInfo(int[] arity, int relations) {
        HashMap<String, Integer> result = new HashMap<>();
        for (int i = 0; i < relations; i++) {
            result.put("P_" + i, ProgramGenerator.randomInRange(arity));
        }
        return result;
    }

    private static void insertRandomRecords(Connection conn, Predicate predicate, int size) throws SQLException {
        String query = "insert into " + predicate.name + " (";
        for (int j = 0; j < predicate.arity; j++) {
            query += "c_" + j + ",";
        }
        query = query.substring(0, query.length()-1) + ") values ";
        for (int i = 0; i < size; i++) {
            query += "(";
            for (int j = 0; j < predicate.arity; j++) {
                query += ProgramGenerator.randomInRange(new int[]{0, domainSize}) + ",";
            }
            query = query.substring(0, query.length()-1) + "),";
        }
        query = query.substring(0, query.length()-1) + ";";
        Statement statement = conn.createStatement();
        statement.executeUpdate(query);
        statement.close();
    }

    private static Schema createSchema(Connection conn, Set<String> ddlQueries) throws SQLException, IOException {
        Statement statement = conn.createStatement();
        for (String query : ddlQueries) {
            statement.addBatch(query);
        }
        statement.executeBatch();
        return Schema.loadSchema(conn);
    }
}
