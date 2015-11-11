package Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by khaidzir on 14/10/2015.
 */
public class QueryRelevancesLoader {

    public static final String ADI_PATH = "E:\\ASDF\\Kuliah\\IF\\Semester 7\\STBI\\Test Collection\\ADI\\qrels.text";
    public static final String CISI_PATH = "E:\\ASDF\\Kuliah\\IF\\Semester 7\\STBI\\Test Collection\\CISI\\qrels.text";

    public static ConcurrentHashMap<Integer, ArrayList<Integer> > qrelsCisi=null, qrelsAdi=null;
    public static boolean isLoaded = false;

    public static void ReadQrelsFile() {
        EksternalFile file = new EksternalFile();

        // Load ADI/qrels.txt
        EksternalFile.setPathQrelsFile(ADI_PATH);
        String adifile = file.readDocuments("qrels").toString();

        // Load CISI/qrels.txt
        EksternalFile.setPathQrelsFile(CISI_PATH);
        String cisifile = file.readDocuments("qrels").toString();

        // Buat qrels ADI
        qrelsAdi = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
        buildQrels(qrelsAdi, adifile);

        // Buat qrels CISI
        qrelsCisi = new ConcurrentHashMap<Integer, ArrayList<Integer>>();
        buildQrels(qrelsCisi, cisifile);

        // Set flag
        isLoaded = true;
    }

    private static void buildQrels(ConcurrentHashMap qrels, String rawqrels) {
        String[] lines = rawqrels.split("\\r?\\n");
        int numq, i=0;
        String[] line = lines[0].split(" ");
        numq = Integer.parseInt(line[0]);
        while(i < lines.length) {
            ArrayList<Integer> docsnum = new ArrayList<Integer>();
            line = lines[i].split(" +");
            boolean cek = Integer.parseInt(line[0]) == numq;
            while(cek) {
                docsnum.add(Integer.parseInt(line[1]));
                i++;
                if (i < lines.length) {
                    line = lines[i].split(" +");
                    cek = Integer.parseInt(line[0]) == numq;
                } else {
                    cek = false;
                }
            }
            qrels.put(numq, docsnum);
            if (i < lines.length)
                numq = Integer.parseInt(line[0]);
        }
    }

}
