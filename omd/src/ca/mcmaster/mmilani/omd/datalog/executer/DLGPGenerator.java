package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.primitives.Fact;
import ca.mcmaster.mmilani.omd.datalog.primitives.TGD;
import ca.mcmaster.mmilani.omd.datalog.synthesizer.ProgramGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class DLGPGenerator {

    public static void main(String[] args) throws IOException {
        File dir = new File("C:\\Users\\mmilani7\\IdeaProjects\\omd_rep\\omd\\dataset\\synthetic");
        File[] dlFiles = dir.listFiles((dir1, name) -> name.endsWith(".txt"));

        for (File dlFile : dlFiles) {
            try {
                Program program = Parser.parseProgram(dlFile);
                program.addDummies();
                String name = dir.getAbsolutePath() + "\\" + dlFile.getName().substring(0, dlFile.getName().lastIndexOf(".")) + ".dlgp";
                printProgram(name, program, false);
                System.out.println(dlFile.getName() + " processed! \n");
            } catch (Throwable e) {
                e.printStackTrace();
                System.out.println("Cannot parse " + dlFile.getName() + "! \n");
            }
        }

    }

    public static void printProgram(String path, Program program, boolean printParameters) throws IOException {
        File file = new File(path);
        file.createNewFile();
        FileWriter out = new FileWriter(file);

        out.write("@facts\n");
        if (program.edb != null) {
            for (Fact fact : program.edb.getFacts()) {
                out.write(fact + ".\n");
            }
        }
        out.write("@rules\n");
        for (TGD tgd : program.tgds) {
            out.write(tgd.toString().replaceAll(":-", " :- ") + ".\n");
        }
//        @parameters
        if (printParameters) {
            for (String key : program.externalParams.keySet()) {
                out.write("@parameters " + key + "=" + program.externalParams.get(key) + "\n");
            }
        }
        out.close();
    }
}
