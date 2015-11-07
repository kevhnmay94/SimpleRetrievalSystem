package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by steve on 03/11/2015.
 */
public class ThreadExperiment extends Thread {
    int i,j,k,l;

    int[] tfcode = {0, 1, 2, 3, 4};
    String[] stringTfcode = {"no", "raw", "log", "bin", "aug"};

    int[] idfcode = {0, 1};
    String[] stringIdfcode = {"noidf", "idf"};

    boolean[] stemcode = {true, false};
    String[] stringStemcode = {"stem", "nostem"};

    boolean[] normcode = {true, false};
    String[] stringNormCode = {"norm", "no-norm"};

    public ThreadExperiment(int i, int j, int k, int l) {
        this.i = i;
        this.j = j;
        this.k = k;
        this.l = l;
    }

    @Override
    public void run() {
        Experiment exp = new Experiment();
        exp.processDocuments(tfcode[i], idfcode[j], stemcode[k]);
        exp.processQueries(tfcode[i], idfcode[j], stemcode[k]);
        exp.evaluate(normcode[l]);

        try {
            String filename = stringTfcode[i]+"_"+stringIdfcode[j]+"_"+stringStemcode[k]+"_"+stringNormCode[l]+".txt";
            File file = new File("D:\\Eksperiment\\" + filename);

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
