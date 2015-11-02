package Utils;

import model.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Created by steve on 07/10/2015.
 */
public class EksternalFile {
    private static ArrayList<String> listPartStringBetweenTokens = new ArrayList<String>();
    private static ArrayList<Integer> listPartIndexInQrels = new ArrayList<Integer>();
    public static final String INDEX_TOKEN = "Index : ";
    public static final String JUDUL_TOKEN = "Topic : ";
    public static final String KONTEN_TOKEN = "Content : ";
    public static final String AUTHOR_TOKEN = "Author : ";
    private static String pathDocumentsFile = "D:\\Informatika\\Materi Semester 7\\IF4042 Sistem Temu Balik Informasi\\Tugas\\Tubes 2\\Test Collection\\ADI\\adi.all";
    private static String pathQueriesFile = "D:\\Informatika\\Materi Semester 7\\IF4042 Sistem Temu Balik Informasi\\Tugas\\Tubes 2\\Test Collection\\ADI\\query.text";
    private static String pathQrelsFile = "D:\\Informatika\\Materi Semester 7\\IF4042 Sistem Temu Balik Informasi\\Tugas\\Tubes 2\\Test Collection\\ADI\\qrels.text";
    private static String pathStopWordsFile = "D:\\Informatika\\Materi Semester 7\\IF4042 Sistem Temu Balik Informasi\\Tugas\\Tubes 2\\Test Collection\\stopwords_en.txt";

    public static ArrayList<String> getListPartStringBetweenTokens() {
        return listPartStringBetweenTokens;
    }
    public static ArrayList<Integer> getListPartIndexInQrels() {
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

    /**
     * Check token in rawFileContent Document File
     * @param line
     * @return boolean
     */
    private static boolean isStringToken(String line) {
        return (line.equals(".T") || line.contains(".I ") || line.equals(".A") || line.equals(".W") || line.equals(".X") || line.equals(".B"));
    }

    /**
     * Scan file from external source based on type of content file
     * @param fileType
     * @return rawFileContent
     */
    public static String readDocuments(String fileType) {
        String rawFileContent = "";
        Path path = null;
        if (fileType.equals("documents")) {
            path = Paths.get(pathDocumentsFile);
        } else if (fileType.equals("queries")) {
            path = Paths.get(pathQueriesFile);
        } else if (fileType.equals("qrels")) {
            path = Paths.get(pathQrelsFile);
        } else if (fileType.equals("stopwords")) {
            path = Paths.get(pathStopWordsFile);
        }
        try {
            Scanner scanner = new Scanner(path);
            while (scanner.hasNextLine()) {
                rawFileContent += scanner.nextLine() + "\n";
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
    private static String addIdentifierDocumentsPart (String partDocument) {
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
    public static void loadListOfDocumentsPart(String rawFileContent) {
        listPartStringBetweenTokens.clear();
        String partStringBetweenTokens = "";
        int counterDocument = 1;
        String tokenType = "";
        String[] lines = rawFileContent.split("\\r?\\n");
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
    public static void loadQueryRelevances(String rawFileContent) {
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
    public static void writeInvertedFile(String path, indexTabel invertedFile) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (Map.Entry m : invertedFile.getListTermWeights().entrySet()) {
            String keyTerm = (String) m.getKey();
            int indexDocument = 0;
            double weightTermInDocument = 0.0;
            for (int i=0; i<((termWeightingDocument) m.getValue()).getDocumentPerTerm().size(); i++) {
                indexDocument = ((termWeightingDocument) m.getValue()).getDocumentPerTerm().get(i).getIndex();
                weightTermInDocument = ((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm().get(i);
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
    public static void writeInvertedFileQuery(String path,  indexTabelQuery invertedFileQuery) {
        FileOutputStream fout = null;
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
        }
    }

    /**
     * Read inverted file from external source
     * @param path
     * @return
     */
    public static indexTabel loadInvertedFile(String path) {
        String rawContent = "";
        indexTabel invertedFile = new indexTabel();
        PreprocessWords word = new PreprocessWords();
        word.loadDocumentsFinal();
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
                    ArrayList<document> documents = word.getListDocumentsFinal();
                    for (int i=0; i<documents.size(); i++) {
                        if (documents.get(i).getIndex() == indexDocument) {
                            weightTermInDocument = Double.parseDouble(tokenString);
                            String judul = documents.get(i).getJudul();
                            String author = documents.get(i).getAuthor();
                            String konten = documents.get(i).getKonten();
                            document Document = new document(indexDocument,judul,author,konten);
                            invertedFile.insertRowTable(keyTerm,Document,weightTermInDocument);
                        }
                    }
                }
                if (counter == 3) {
                    counter = 1;
                } else {
                    counter++;
                }
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
    public static indexTabelQuery loadInvertedFileQuery(String path) {
        PreprocessWords word = new PreprocessWords();
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
        return invertedFile;
    }

    public static void main(String[] arg) {
        loadListOfDocumentsPart(readDocuments("queries"));
        for (int i=0; i<EksternalFile.getListPartStringBetweenTokens().size(); i++) {
            System.out.println(EksternalFile.getListPartStringBetweenTokens().get(i));
            System.out.println("============================================================");
        }

        /*loadQueryRelevances(readDocuments("qrels"));
        for (int i=0; i<EksternalFile.getListPartIndexInQrels().size(); i++) {
            System.out.println(EksternalFile.getListPartIndexInQrels().get(i));
        }*/
    }
}
