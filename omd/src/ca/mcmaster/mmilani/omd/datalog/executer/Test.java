package ca.mcmaster.mmilani.omd.datalog.executer;

import ca.mcmaster.mmilani.omd.datalog.synthesizer.ProgramGenerator;

public class Test {

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(ProgramGenerator.randomInRange(new int[]{0,10}));
        }
    }
}
