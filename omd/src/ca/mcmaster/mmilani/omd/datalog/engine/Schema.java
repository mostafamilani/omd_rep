package ca.mcmaster.mmilani.omd.datalog.engine;

import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.primitives.Constant;
import ca.mcmaster.mmilani.omd.datalog.primitives.Predicate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Schema {
    public Map<String, Constant> constants = new HashMap<>();
    public Map<String, Predicate> predicates = new HashMap<>();
    public Program program;

    public static Schema loadSchema(Connection conn) throws SQLException {
        Schema schema = new Schema();
        Statement statement = conn.createStatement();
        statement.execute("select c.relname as table_name, c.reltuples as reltuples, c.relnatts as relnatts\n" +
                "from pg_class c\n" +
                "join pg_namespace n on n.oid = c.relnamespace\n" +
                "where c.relkind = 'r'\n" +
                "      and n.nspname not in ('information_schema','pg_catalog')\n" +
                "order by table_name;");
        ResultSet resultSet = statement.getResultSet();
        while(resultSet.next()) {
            String name = resultSet.getString("table_name");
            int arity = Integer.parseInt(resultSet.getString("relnatts"));
            Predicate predicate = schema.fetchPredicate(name, arity);
            schema.predicates.put(name, predicate);
        }
        return schema;
    }

    public Constant fetchConstant(String label) {
        if (!constants.containsKey(label))
            constants.put(label, new Constant(label));
        return constants.get(label);
    }

    public Predicate fetchPredicate(String name, int arity) {
        name = Parser.sanitizePredicateName(name);
        if (!predicates.containsKey(name))
            predicates.put(name, new Predicate(name, arity, program));
        Predicate predicate = predicates.get(name);
        if (predicate.arity != arity)
            throw new RuntimeException("Invalid Arity! " + predicate);
        return predicate;
    }

    public int maxArity() {
        int max = Integer.MIN_VALUE;
        for (Predicate predicate : predicates.values()) {
            if (predicate.arity > max)
                max = predicate.arity;
        }
        return max;
    }

    public Predicate fetchAdornedPredicate(Predicate predicate, String adornment) {
        if (predicate.isAdorned())
            return null;
        return fetchPredicate(predicate.name + "^" + adornment, predicate.arity);
    }

    public Predicate fetchSimplePredicate(Predicate predicate) {
        if (!predicate.isAdorned())
            return null;
        return fetchPredicate(predicate.name.substring(0, predicate.name.indexOf("^")), predicate.arity);
    }

    public Set<Predicate> allAdorned() {
        HashSet<Predicate> result = new HashSet<>();
        for (Predicate predicate : predicates.values()) {
            if (predicate.name.contains(predicate.name + "^")) {
                result.add(predicate);
            }
        }
        return result;
    }

    public Predicate fetchMagicPredicate(Predicate predicate) {
        if (!predicate.isAdorned())
            return null;
        Predicate p = fetchSimplePredicate(predicate);

        String adornment = predicate.getAdornment();
        return fetchPredicate("m_" + p.name + "^" + adornment, adornment.replaceAll("b", "").length());
    }
}
