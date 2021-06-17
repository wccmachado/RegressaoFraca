import alu.ufc.file.ModelReader;
import alu.ufc.regression.ModelChecker;

import java.io.File;
import java.io.IOException;

public class Enginer {
    public static void main(String[] args) throws IOException {

        //rovers-01-GROUNDED3.txt
        // String fileName = "ArquivoGoal.txt";
        String fileName = "outroVersaoTeste.txt";
       // String fileName = "ArquivoTestePreferencesAlways02.txt";
        String fileCanonicalPath = new File(/*PATH +*/ fileName /*+ "_" + cont + ".txt"*/).getCanonicalPath();


        int nodenum = 999999; //Integer.parseInt(args[2]);
        int cachesize =  999999; //Integer.parseInt(args[3]);


        ModelReader model = new ModelReader();
        model.fileReader(fileCanonicalPath, nodenum, cachesize);

        System.out.println("File: " +
                fileCanonicalPath.substring(fileCanonicalPath.lastIndexOf("/") + 1, fileCanonicalPath.lastIndexOf(".")));

        ModelChecker mdc = new ModelChecker(model);


       // System.out.println("Aqui está a solução: ");
        mdc.run();


    }
}
