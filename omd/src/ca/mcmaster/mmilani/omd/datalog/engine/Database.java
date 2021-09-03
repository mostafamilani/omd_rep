package ca.mcmaster.mmilani.omd.datalog.engine;

import ca.mcmaster.mmilani.omd.datalog.primitives.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public abstract class Database {
    public Program program;
    public Map<String,Integer> recordCount = new HashMap<>();

    abstract Database copy();

    public abstract Fact[] getFacts();

    public abstract Set<Assignment> evaluate(UCQ ucq);

    public abstract boolean isEmpty();

    public abstract void addFact(Fact fact);

    public abstract Fact addFact(Predicate predicate, List<Term> terms, int level);

    public abstract boolean isEmpty(Predicate predicate);
}
