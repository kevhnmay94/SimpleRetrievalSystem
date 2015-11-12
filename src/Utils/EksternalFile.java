package Utils;

import model.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by steve on 07/10/2015.
 */
public class EksternalFile {
    private ArrayList<String> listPartStringBetweenTokens;
    private ArrayList<Integer> listPartIndexInQrels;
    public static final String INDEX_TOKEN = "Index : ";
    public static final String JUDUL_TOKEN = "Topic : ";
    public static final String KONTEN_TOKEN = "Content : ";
    public static final String AUTHOR_TOKEN = "Author : ";
    private static String pathDocumentsFile = "test\\CISI\\cisi.all";
    private static String pathQueriesFile = "test\\CISI\\query.text";
    private static String pathQrelsFile = "test\\CISI\\qrels.text";
    private static String pathStopWordsFile = "test\\stopwords_en.txt";
   /* private static String pathDocumentsFile = "test\\ADI\\adi.all";
    private static String pathQueriesFile = "test\\ADI\\query.text";
    private static String pathQrelsFile = "test\\ADI\\qrels.text";
    private static String pathStopWordsFile = "test\\stopwords_en.txt"; */

    public ArrayList getListPartStringBetweenTokens() {
        return listPartStringBetweenTokens;
    }
    public ArrayList getListPartIndexInQrels() {
        return listPartIndexInQrels;
    }
    public static void setPathDocumentsFile(String pathDocumentsFile) {
        EksternalFile.pathDocumentsFile = pathDocumentsFile;
    }
    public static void setPathQueriesFile(String pathQueriesFile) {
        EksternalFile.pathQueriesFile = pathQueriesFile;
    }
    public static void setPathQrelsFile(String pathQrelsFile) {
        EksternalFile.pathQrelsFile = pathQrelsFile;
    }
    public static void setPathStopWordsFile(String pathStopWordsFile) {
        EksternalFile.pathStopWordsFile = pathStopWordsFile;
    }

    // KONSTRUKTOR
    public EksternalFile() {
        listPartStringBetweenTokens = new ArrayList<>();
        listPartIndexInQrels = new ArrayList<>();
    }

    /**
     * Check token in rawFileContent Document File
     * @param line
     * @return boolean
     */
    private boolean isStringToken(String line) {
        return (line.equals(".T") || line.contains(".I ") || line.equals(".A") || line.equals(".W") || line.equals(".X") || line.equals(".B"));
    }

    /**
     * Scan file from external source based on type of content file
     * @param fileType
     * @return rawFileContent
     */
    public StringBuffer readDocuments(String fileType) {
        StringBuffer rawFileContent = new StringBuffer();
        String path = "";
        if (fileType.equals("documents")) {
            path = pathDocumentsFile;
        } else if (fileType.equals("queries")) {
            path = pathQueriesFile;
        } else if (fileType.equals("qrels")) {
            path = pathQrelsFile;
        } else if (fileType.equals("stopwords")) {
            path = pathStopWordsFile;
        }
        String  thisLine;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while ((thisLine = br.readLine()) != null) {
                rawFileContent.append(thisLine + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawFileContent;
    }

    /**
     * Give identifier to document segment separated by tokens README.txt
     * @param partDocument
     * @return identifier
     */
    private String addIdentifierDocumentsPart (String partDocument) {
        String identifier = "";
        if (partDocument.contains(".I ")) {
            identifier = INDEX_TOKEN;
        } else if (partDocument.equals(".A")) {
            identifier = AUTHOR_TOKEN;
        } else if (partDocument.equals(".W")) {
            identifier = KONTEN_TOKEN;
        } else if (partDocument.equals(".T")) {
            identifier = JUDUL_TOKEN;
        }
        return identifier;
    }

    /**
     * Break apart document file based token README.TXT
     * Special case : documents or queries
     * @param rawFileContent
     */
     void loadListOfDocumentsPart(StringBuffer rawFileContent) {
        listPartStringBetweenTokens.clear();
        String partStringBetweenTokens = "";
        int counterDocument = 1;
        String tokenType = "";
        String[] lines = rawFileContent.toString().split("\\r?\\n");
        for (String line : lines) {
            if (isStringToken(line)) {
                if (tokenType.equals(INDEX_TOKEN)) {
                    listPartStringBetweenTokens.add(INDEX_TOKEN + String.valueOf(counterDocument));
                    counterDocument++;
                } else {
                    listPartStringBetweenTokens.add(tokenType + partStringBetweenTokens);
                }
                partStringBetweenTokens = "";
                if (addIdentifierDocumentsPart(line).equals(KONTEN_TOKEN) && (tokenType.equals(JUDUL_TOKEN))) {
                    listPartStringBetweenTokens.add(AUTHOR_TOKEN + "Unknown\n");
                }
                tokenType = addIdentifierDocumentsPart(line);
            } else {
                partStringBetweenTokens += line + "\n";
            }
        }
        listPartStringBetweenTokens.add(tokenType + partStringBetweenTokens);
    }

    /**
     * Break apart qrels.txt into sequence of number
     * Special case : query relevances
     * @param rawFileContent
     */
    public  void loadQueryRelevances(String rawFileContent) {
        listPartIndexInQrels.clear();
        ArrayList<String> rawQrelsSegmented = new ArrayList<String>();
        String[] lines = rawFileContent.split("\\r?\\n");
        for (String line : lines) {
            StringTokenizer token = new StringTokenizer(line," ");
            while (token.hasMoreTokens()) {
                rawQrelsSegmented.add(token.nextToken());
            }
        }
        int sequenceCounter = 1;
        for (int i=0; i<rawQrelsSegmented.size(); i++) {
            if ((sequenceCounter % 4 != 3) && (sequenceCounter % 4 != 0)) {
                listPartIndexInQrels.add(Integer.parseInt(rawQrelsSegmented.get(i)));
            }
            sequenceCounter++;
        }
    }

    /**
     * Write inverted file that has been created into external text file
     * Assumption : inverted file completed with weight each term
     * @param path
     * @param invertedFile
     */
    public  void writeInvertedFile(String path, indexTabel invertedFile) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (Map.Entry m : invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            for (Map.Entry n : ((termWeightingDocument) m.getValue()).getDocumentWeightCounterInOneTerm().entrySet()) {
                int indexDocument = (Integer) n.getKey();
                double weightTermInDocument = ((counterWeightPair) n.getValue()).getWeight();
                new PrintStream(fout).print(/*"~" + */keyTerm + "," + indexDocument + "," + weightTermInDocument + "," + "\n");
            }
        }
    }

    /**
     * Write inverted file for query (experiment) into external file
     * Assumption : inverted file must have been created well
     * @param path
     * @param invertedFileQuery
     */
    public void writeInvertedFileQuery(String path,indexTabel invertedFileQuery) {
        writeInvertedFile(path,invertedFileQuery);
    }

    public void writeNormalFileQuery(String path,normalTabel normalFileQuery) {
        writeNormalFile(path,normalFileQuery);
    }

    public void writeNormalFile(String path, normalTabel normalFile) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (Map.Entry m : normalFile.getNormalFile().entrySet()) {
            Integer indexDocument = (Integer) m.getKey();
            HashSet<String> listTerms = (HashSet<String>) m.getValue();
            Iterator iterator = listTerms.iterator();
            while (iterator.hasNext()) {
                String thisTerm = (String) iterator.next();
                new PrintStream(fout).print(/*"~" + */indexDocument + "," + thisTerm + "," + "\n");
            }
        }
    }

    /**
     * Read inverted file from external source
     * @param path
     * @return
     */
    public indexTabel loadInvertedFile(String path) {
        StringBuffer rawContent = new StringBuffer();
        indexTabel invertedFile = new indexTabel();
        Path pathInvertedFile = Paths.get(path);
        try {
            Scanner scanner = new Scanner(pathInvertedFile);
            while (scanner.hasNextLine()) {
                rawContent.append(scanner.nextLine());
            }
            StringTokenizer token = new StringTokenizer(rawContent.toString(),",");
            int counter = 1;
            String keyTerm = null;
            int indexDocument = 0;
            double weightTermInDocument;
            while (token.hasMoreTokens()) {
                String tokenString = token.nextToken();
                if ((counter % 3) == 1) {
                    keyTerm = tokenString;
                } else if ((counter % 3) == 2) {
                    indexDocument = Integer.parseInt(tokenString);
                } else if ((counter % 3) == 0) {
                    weightTermInDocument = Double.parseDouble(tokenString);
                    invertedFile.insertRowTable(keyTerm,indexDocument,weightTermInDocument);
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return invertedFile;
    }

    /**
     * Read inverted file query (experimental) from external source
     * @param path
     * @return
     */
    public indexTabel loadInvertedFileQuery(String path) {
        return loadInvertedFile(path);
    }

    public normalTabel loadNormalFile(String path) {
        StringBuffer rawContent = new StringBuffer();
        normalTabel normalFile = new normalTabel();
        Path pathInvertedFile = Paths.get(path);
        try {
            Scanner scanner = new Scanner(pathInvertedFile);
            while (scanner.hasNextLine()) {
                rawContent.append(scanner.nextLine());
            }
            StringTokenizer token = new StringTokenizer(rawContent.toString(),",");
            int counter = 1;
            int indexDocument = 0;
            String thisTerm = "";
            while (token.hasMoreTokens()) {
                String tokenString = token.nextToken();
                if ((counter % 2) == 1) {
                    indexDocument = Integer.parseInt(tokenString);
                } else if ((counter % 2) == 0) {
                    thisTerm = tokenString;
                    normalFile.insertElement(indexDocument,thisTerm);
                }
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return normalFile;
    }

    public normalTabel loadNormalFileQuery(String path) {
        return (loadNormalFile(path));
    }

    public static void main(String[] arg) {
        EksternalFile file = new EksternalFile();
        file.loadListOfDocumentsPart(file.readDocuments("queries"));
        Iterator listPart = file.getListPartStringBetweenTokens().iterator();
        while (listPart.hasNext()) {
            System.out.println(listPart.next());
            System.out.println("============================================================");
        }

      //  System.out.println(file.readDocuments("documents"));
        /*loadQueryRelevances(readDocuments("qrels"));
        for (int i=0; i<EksternalFile.getListPartIndexInQrels().size(); i++) {
            System.out.println(EksternalFile.getListPartIndexInQrels().get(i));
        }*/
    }
}
