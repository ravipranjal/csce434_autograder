package edu.tamu.csce434;

import java.io.*;

import edu.tamu.csce434.Compiler;
import edu.tamu.csce434.DLX;

public class TestCompiler {
    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("Usage: TestCompiler <code file> <data file>");
            return;
        }
        int[] sampleProg;
        String val1 = args[0], val2 = args[1]; 
        try {
            // Redirect System.in from DLX to data file
            // YYY
            InputStream origIn = System.in, newIn = new BufferedInputStream(new FileInputStream(val2));
            System.setIn(newIn);

            Compiler p = new Compiler(val1);
            int prog[] = p.getProgram();
            if (prog == null) {
                System.err.println("Error compiling program!");
                return;
            }

            // DLX dlx = new DLX();
            DLX.load(prog);
            DLX.execute();

            System.setIn(origIn);
            newIn.close();

            // Test just test 2013/01/11
        } catch (IOException e) {
            System.err.println("Error reading input files!" + e);
        }
    }

}
