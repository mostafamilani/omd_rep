package ca.mcmaster.mmilani.omd.datalog.engine;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class PersistantDatabase extends Database {
    Connection conn;

    @Override
    Database copy() {
        return null;
    }

    @Override
    public Fact[] getFacts() {
        throw new RuntimeException("This feature is not implemented!");
    }

    @Override
    public Set<Assignment> evaluate(UCQ ucq) {
        throw new RuntimeException("This feature is not implemented!");
    }

    public boolean isEmpty() {
        for (Integer count : recordCount.values()) {
            if (count > 0)
                return false;
        }
        return true;
    }

    @Override
    public void addFact(Fact fact) {
        throw new RuntimeException("This feature is not implemented!");
    }

    @Override
    public Fact addFact(Predicate predicate, List<Term> terms, int level) {
        throw new RuntimeException("This feature is not implemented!");
    }

    @Override
    public boolean isEmpty(Predicate predicate) {
        return recordCount.get(predicate.name) < 1;
    }

    public void connect(Connection conn) throws IOException, SQLException {
        this.conn = conn;
    }
}
