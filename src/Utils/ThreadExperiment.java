package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by steve on 03/11/2015.
 */
public class ThreadExperiment extends Thread {
    int i;

    int[] tfcode = {0, 1, 2, 3, 4};
    String[] stringTfcode = {"no", "raw", "log", "bin", "aug"};

    int[] idfcode = {0, 1};
    String[] stringIdfcode = {"noidf", "idf"};

    boolean[] stemcode = {true, false};
    String[] stringStemcode = {"stem", "nostem"};

    boolean[] normcode = {true, false};
    String[] stringNormCode = {"norm", "no-norm"};

    public ThreadExperiment(int i) {
        this.i = i;
    }

    @Override
    public void run() {
        for(int j=0; j<idfcode.length; j++) {
            for(int k=0; k<stemcode.length; k++) {
                for(int l=0; l<normcode.length; l++) {
                    Experiment exp = new Experiment();
                    exp.processDocuments(tfcode[i], idfcode[j], stemcode[k]);
                    exp.processQueries(tfcode[i], idfcode[j], stemcode[k]);
                    exp.evaluate(normcode[l]);

                    try {
                        String filename = stringTfcode[i]+"_"+stringIdfcode[j]+"_"+stringStemcode[k]+"_"+stringNormCode[l]+".txt";
                        File file = new File("D:\\Eksperiment2\\" + filename);

                        // if file doesnt exists, then create it
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(exp.getSummary());
                        bw.close();
                        System.out.println("Done");
                        System.out.println("Thread ke-" + i + ", " + j + ", " + k + ", " + l + " sudah berjalan");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
