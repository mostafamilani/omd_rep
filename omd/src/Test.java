import ca.mcmaster.mmilani.omd.datalog.Answer;
import ca.mcmaster.mmilani.omd.datalog.Parser;
import ca.mcmaster.mmilani.omd.datalog.Program;
import ca.mcmaster.mmilani.omd.datalog.primitives.Query;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Test {

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\Mostafa\\IdeaProjects\\omd\\prg.txt");
        Parser pa = new Parser();
        pa.init(file);
        Program p = pa.parseProgram();
        List<Query> queries = pa.parseQueries();
        for (Query query : queries) {
            System.out.println("query = " + query);
            for (Answer answer : p.evaluate(query)) {
                System.out.println(answer);
            }
        }
    }
}
