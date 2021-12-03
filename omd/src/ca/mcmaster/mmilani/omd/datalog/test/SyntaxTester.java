package ca.mcmaster.mmilani.omd.datalog.test;

import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer;
import ca.mcmaster.mmilani.omd.datalog.executer.TerminationAnalyzer;
import ca.mcmaster.mmilani.omd.datalog.primitives.Node;
import ca.mcmaster.mmilani.omd.datalog.primitives.Position;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SyntaxTester {
    public static Program program;
    public static Map<Position, Node> dgraph;
    public static Set<Node> cycleNodes;
    public static Set<Position> infiniteRankPositions;
    public static Node p_0, p_1, s_0, s_1, r_0, r_1, t_0, t_1, u_0;

    @BeforeClass
    public static void setup() throws IOException {
        program = Parser.parseProgram(new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\test_data\\syntax-test.txt"));
        dgraph = SyntacticAnalyzer.buildDependencyGraph(program.tgds);
        p_0 = dgraph.get(new Position(0, program.schema.fetchPredicate("p", 2)));
        p_1 = dgraph.get(new Position(1, program.schema.fetchPredicate("p", 2)));
        s_0 = dgraph.get(new Position(0, program.schema.fetchPredicate("s", 2)));
        s_1 = dgraph.get(new Position(1, program.schema.fetchPredicate("s", 2)));
        r_0 = dgraph.get(new Position(0, program.schema.fetchPredicate("r", 2)));
        r_1 = dgraph.get(new Position(1, program.schema.fetchPredicate("r", 2)));
        t_0 = dgraph.get(new Position(0, program.schema.fetchPredicate("t", 2)));
        t_1 = dgraph.get(new Position(1, program.schema.fetchPredicate("t", 2)));
        u_0 = dgraph.get(new Position(0, program.schema.fetchPredicate("u", 1)));
        cycleNodes = SyntacticAnalyzer.getNodesInSpecialCycle(dgraph, program);
        infiniteRankPositions = SyntacticAnalyzer.getInfiniteRankPositions(dgraph, cycleNodes);
    }

    @Test
    public void testDepGraph() {
//        Assert.assertTrue("p0 has a special edge to s1", p_0.isNext(s_1) != null);
//        HashSet<Object> nexts = new HashSet<>(); nexts.add(s_0); nexts.add(u_0);
//        Assert.assertTrue("p0 has edges to s0 and u0", p_0.nexts.containsAll(nexts));
//        Assert.assertTrue("u0 has a especial edge to r1", u_0.isNext(r_1) != null);
//        Assert.assertTrue("u0 has an edge to r1", u_0.nexts.contains(r_0));
//        Assert.assertTrue("t1 has an edge to u0", t_1.nexts.contains(u_0));
//        Assert.assertTrue("r1 has an edge to t0", r_1.nexts.contains(t_0));
//        Assert.assertTrue("r1 has a special edge to t1", r_1.nextSpecials.contains(t_1));
//        Assert.assertTrue("r0 has a special edge to t1", r_0.nextSpecials.contains(t_1));
    }

    @Test
    public void testNodesInSpecialCycles() {
        HashSet<Object> nodes = new HashSet<>();
        nodes.add(p_1);
        nodes.add(s_1);
        nodes.add(u_0);
        nodes.add(r_0);
        nodes.add(r_1);
        nodes.add(t_1);
        Assert.assertTrue("A node is wrongly decided to be in special cycles", cycleNodes.containsAll(nodes));
        Assert.assertTrue("A node in special cycles is missing!", nodes.containsAll(cycleNodes));
    }

    @Test
    public void testInfiniteRankPositions() {
        HashSet<Object> positions = new HashSet<>();
        positions.add(p_1.p);
        positions.add(s_1.p);
        positions.add(u_0.p);
        positions.add(r_0.p);
        positions.add(r_1.p);
        positions.add(t_1.p);
        positions.add(t_0.p);
        Assert.assertTrue("A position is wrongly decided as infinite rank!", infiniteRankPositions.containsAll(positions));
        Assert.assertTrue("An infinite rank position is missing!", positions.containsAll(infiniteRankPositions));
    }

    @Test
    public void testDescendants() {

    }

    @Test
    public void testTermination() {
        boolean terminates = TerminationAnalyzer.terminates(program, dgraph, infiniteRankPositions);
        Assert.assertTrue("Program does not terminate but it is reported terminating!", !terminates);
    }
}
