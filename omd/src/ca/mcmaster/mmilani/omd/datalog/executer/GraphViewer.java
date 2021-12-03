package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.primitives.Node;
import ca.mcmaster.mmilani.omd.datalog.primitives.Position;
import javafx.scene.paint.Color;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.fx_viewer.FxViewer;
import org.graphstream.ui.graphicGraph.GraphicGraph;
import org.graphstream.ui.view.Viewer;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer.getNodesInSpecialCycle;
import static ca.mcmaster.mmilani.omd.datalog.executer.SyntacticAnalyzer.getPositions;

public class GraphViewer {
    public static void main(String[] args) throws IOException {
        System.setProperty("org.graphstream.ui", "javafx");


        Program program = Parser.parseProgram(new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\synthetic\\program-32.txt"));
        displayGraph(program);
    }

    private static void displayGraph(Program program) {
        Map<Position, Node> dGraph = SyntacticAnalyzer.buildDependencyGraph(program.tgds);
        Set<Node> nodesInSpecialCycle = getNodesInSpecialCycle(dGraph, program);
        Set<Position> loopPositions = getPositions(nodesInSpecialCycle);

        TerminationAnalyzer.terminates(program, dGraph, loopPositions);
        Graph graph = new SingleGraph(program.name);
        for (Position position : dGraph.keySet()) {
            org.graphstream.graph.Node node = graph.addNode(position.toString());
            node.setAttribute("ui.style", "width: 1px; fill-color: blue;");
        }
        for (Position position : dGraph.keySet()) {
            int i = 1;
            Node node = dGraph.get(position);
//            if (!nodesInSpecialCycle.contains(node)) continue;
            try {
                for (Node next : node.nextSpecials) {
                    Edge edge = graph.addEdge(position.toString() + "S" + i, position.toString(), next.p.toString());
                    edge.setAttribute("ui.style", "stroke-width: 30px; fill-color: blue;");
                    i++;
                }
                for (Node next : node.nexts) {
                    Edge edge = graph.addEdge(position.toString() + i, position.toString(), next.p.toString());
                    edge.setAttribute("ui.style", "stroke-width: 30px; fill-color: red;");
                    i++;
                }
            } catch (EdgeRejectedException e) {
                System.out.println(node + " is already connected!");
            }
        }
//        graph.setAttribute("ui.stylesheet", "C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\g.css");
        graph.display();
    }
}
