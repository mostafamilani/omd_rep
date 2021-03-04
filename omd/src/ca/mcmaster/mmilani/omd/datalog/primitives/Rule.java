package ca.mcmaster.mmilani.omd.datalog.primitives;

//import ca.mcmaster.mmilani.omd.datalog.Program;

import ca.mcmaster.mmilani.omd.datalog.Program;

import java.util.*;

public abstract class Rule<B,H> {
    public B body;
    public H head;
    public Map<String, Variable> variables = new HashMap<>();


    public abstract void addProgram(Program program);

    public abstract Variable fetchVariable(String s, boolean body);
}
