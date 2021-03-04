import ca.mcmaster.mmilani.omd.datalog.Assignment;
import ca.mcmaster.mmilani.omd.datalog.Parser;
import ca.mcmaster.mmilani.omd.datalog.Program;
import ca.mcmaster.mmilani.omd.datalog.primitives.CQ;
import ca.mcmaster.mmilani.omd.datalog.primitives.Query;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Test {

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\test_data\\test.txt");
//        File file = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\paper_dataset\\00001.txt");
        Program p = Parser.parseProgram(file);
        System.out.println("p = " + p);
    }

    public static void main1(String[] args) throws IOException {
        File file = new File("C:\\Users\\Mostafa\\Desktop\\omd_prj\\omd_rep\\omd\\prg-2.txt");
        Program p = Parser.parseProgram(file);
        p.chase();
        List<CQ> queries = Parser.parseQueries(file);

        for (CQ query : queries) {
            System.out.println("query = " + query);
            for (Assignment answer : p.evaluate(query)) {
                System.out.println(answer);
            }
        }
    }
}
