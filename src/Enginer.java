
import alu.ufc.file.ModelReader;
import alu.ufc.regression.ModelChecker;

import java.io.File;
import java.io.IOException;
//import alu.ufc.data;

public class Enginer {
    public static void main(String[] args) throws IOException {
        String fileName = "file10NaoDeterministicoComPreferenciasVerssaoAlterada.txt";

        String fileCanonicalPath = new File(/*PATH +*/ fileName /*+ "_" + cont + ".txt"*/).getCanonicalPath();

        int nodenum = 999999; //Integer.parseInt(args[2]);
        int cachesize =  999999; //Integer.parseInt(args[3]);


        ModelReader model = new ModelReader();
        model.fileReader(fileCanonicalPath, nodenum, cachesize);

        System.out.println("File: " +
                fileCanonicalPath.substring(fileCanonicalPath.lastIndexOf("/") + 1, fileCanonicalPath.lastIndexOf(".")));

        ModelChecker mdc = new ModelChecker(model);



        mdc.run();


    }
}
