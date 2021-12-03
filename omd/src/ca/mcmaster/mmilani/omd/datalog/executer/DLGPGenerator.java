package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.parsing.Parser;
import ca.mcmaster.mmilani.omd.datalog.engine.Program;
import ca.mcmaster.mmilani.omd.datalog.primitives.Fact;
import ca.mcmaster.mmilani.omd.datalog.primitives.TGD;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DLGPGenerator {

    public static void main(String[] args) throws IOException {
//        String args0 = "/home/cqadev/Desktop/chase-termination/programs/synthetic-at-arity/";
//        String args0 = "/home/cqadev/Desktop/chase-termination/programs/synthetic-at-arity/";
//        String args0 = "/home/cqadev/Desktop/chase-termination/programs/synthetic-at-arity/";
        File dir = new File(args[0]);
        File[] dlFiles = dir.listFiles((dir1, name) -> name.endsWith(".txt"));

        for (File dlFile : dlFiles) {
            try {
                String name = dir.getAbsolutePath() + "/" + dlFile.getName().substring(0, dlFile.getName().lastIndexOf(".")) + ".dlgp";
                File outFile = new File(name);
                if (outFile.exists()) continue;
                outFile.createNewFile();
                Program program = Parser.parseProgram(dlFile);
                program.addDummies();
                printProgram(outFile, program, false);
                System.out.println(dlFile.getName() + " processed! \n");
            } catch (Throwable e) {
                e.printStackTrace();
                System.out.println("Cannot parse " + dlFile.getName() + "! \n");
            }
        }
    }

    public static void printProgram(File outFile, Program program, boolean printParameters) throws IOException {
        FileWriter out = new FileWriter(outFile);
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
