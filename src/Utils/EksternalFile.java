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
                new PrintStream(fout).print("~" + keyTerm + "~" + indexDocument + "~" + weightTermInDocument + "\n");
            }
        }
    }

    /**
     * Write inverted file for query (experiment) into external file
     * Assumption : inverted file must have been created well
     * @param path
     * @param invertedFileQuery
     */
    public  void writeInvertedFileQuery(String path,  indexTabel invertedFileQuery) {
        writeInvertedFile(path,invertedFileQuery);
        /* FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (termWeightingQuery q : invertedFileQuery.getListQueryWeighting()) {
            String indexQuery = String.valueOf(q.getCurrentQuery().getIndex()) + "^";
            String weight = "";
            for (Map.Entry m : q.getTermWeightInOneQuery().entrySet()) {
                String currentTerm = (String) m.getKey();
                double weightTerm = (Double) m.getValue();
                weight += "@" + currentTerm + ";*" + weightTerm + "^";
            }
            new PrintStream(fout).print("~" + indexQuery + weight + "\n");
        } */
    }

    /**
     * Read inverted file from external source
     * @param path
     * @return
     */
    public indexTabel loadInvertedFile(String path) {
        String rawContent = "";
        indexTabel invertedFile = new indexTabel();
        Path pathInvertedFile = Paths.get(path);
        try {
            Scanner scanner = new Scanner(pathInvertedFile);
            while (scanner.hasNextLine()) {
                rawContent += scanner.nextLine() + "\n";
            }
            StringTokenizer token = new StringTokenizer(rawContent,"~\n");
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
    public  indexTabel loadInvertedFileQuery(String path) {
        return loadInvertedFile(path);
       /* PreprocessWords word = new PreprocessWords();
        word.loadQueriesFinal();
        String rawContent = "";
        indexTabelQuery invertedFile = new indexTabelQuery();
        Path pathInvertedFile = Paths.get(path);
        try {
            Scanner scanner = new Scanner(pathInvertedFile);
            while (scanner.hasNextLine()) {
                rawContent += scanner.nextLine();
            }
            StringTokenizer token1 = new StringTokenizer(rawContent,"~");
            while (token1.hasMoreTokens()) {
                int indexQuery = 0;
                termWeightingQuery relation = new termWeightingQuery();
                String currentWordToken1 = token1.nextToken();
                StringTokenizer token2 = new StringTokenizer(currentWordToken1,"^");
                while (token2.hasMoreTokens()) {
                    String currentWordToken2 = token2.nextToken();
                    if (!currentWordToken2.contains(";")) {
                        indexQuery = Integer.parseInt(currentWordToken2);
                    } else {
                        String keyTerm = "";
                        double weightTerm = 0.0;
                        StringTokenizer token3 = new StringTokenizer(currentWordToken2,";");
                        while (token3.hasMoreTokens()) {
                            String currentWordToken3 = token3.nextToken();
                            if (currentWordToken3.contains("@")) {
                                keyTerm = currentWordToken3.replace("@","");
                            } else if (currentWordToken3.contains("*")) {
                                weightTerm = Double.parseDouble(currentWordToken3.replace("*",""));
                                relation.getTermWeightInOneQuery().put(keyTerm,weightTerm);
                            }
                        }
                    }
                    ArrayList<query> listQueries = word.getListQueriesFinal();
                    for (query Query : listQueries) {
                        if (Query.getIndex() == indexQuery) {
                            query currentQuery = new query(indexQuery,Query.getQueryContent());
                            relation.setCurrentQuery(currentQuery);
                        }
                    }
                }
                invertedFile.getListQueryWeighting().add(relation);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return invertedFile; */
    }

    public static void main(String[] arg) {
        EksternalFile file = new EksternalFile();
        file.loadListOfDocumentsPart(file.readDocuments("documents"));
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
