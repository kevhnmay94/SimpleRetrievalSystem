package Utils;

import model.*;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by steve on 08/10/2015.
 */
public class PreprocessWords {
    private indexTabel invertedFile = new indexTabel();
    private indexTabelQuery invertedFileQuery = new indexTabelQuery();
    private queryRelevances listQueryRelevancesFinal = new queryRelevances();
    private ArrayList<query> listQueriesFinal = new ArrayList<query>();
    private ArrayList<document> listDocumentsFinal = new ArrayList<document>();
    private ArrayList<String> listStopWordsFinal = new ArrayList<String>();

    private ArrayList<Integer> listIndexDocuments = new ArrayList<Integer>();
    private ArrayList<String> listTopicDocuments = new ArrayList<String>();
    private ArrayList<String> listAuthorDocuments = new ArrayList<String>();
    private ArrayList<String> listContentDocuments = new ArrayList<String>();

    public indexTabelQuery getInvertedFileQuery() {
        return invertedFileQuery;
    }
    public ArrayList<document> getListDocumentsFinal() {
        return listDocumentsFinal;
    }
    public indexTabel getInvertedFile() {
        return invertedFile;
    }
    public queryRelevances getListQueryRelevancesFinal() {
        return listQueryRelevancesFinal;
    }
    public ArrayList<query> getListQueriesFinal() {
        return listQueriesFinal;
    }
    public ArrayList<String> getListStopWordsFinal() {
        return listStopWordsFinal;
    }

    public void setInvertedFileQuery(indexTabelQuery invertedFileQuery) { this.invertedFileQuery = invertedFileQuery; }

    public void setInvertedFile(indexTabel invertedFile) {
        this.invertedFile = invertedFile;
    }

    // Singel query
    public indexTabelQuery getInvertedFileManualQuery() {
        return invertedFileManualQuery;
    }

    // Single query
    private indexTabelQuery invertedFileManualQuery = new indexTabelQuery();

    /**
     * Check if a word is stop word or not
     * @return boolean
     */
    public boolean isStopWords(String word) {
        //loadStopWordsFinal();
        boolean isStopWords = false;
        for (String s : listStopWordsFinal) {
            if (s.equalsIgnoreCase(word.trim())) {
                isStopWords = true; break;
            }
        }
        return isStopWords;
    }

    /**
     * Create inverted file for document
     * Assumption : list of documents must have been loaded first
     * @param isStemmingApplied
     */
    public void loadIndexTabel(boolean isStemmingApplied) {
        loadDocumentsFinal();
        loadStopWordsFinal();
        for (document Document : listDocumentsFinal) {
            String preprocessWords = Document.getJudul() + Document.getKonten();
            StringTokenizer token = new StringTokenizer(preprocessWords," +=-1234567890'(){}[]/.:;?!,\n");
            while (token.hasMoreTokens()) {
                String currentWord = token.nextToken();
                if (!isStopWords(currentWord)) {
                    if (isStemmingApplied) {
                        invertedFile.insertRowTable(StemmingPorter.stripAffixes(currentWord), Document, 1.0);
                    } else {
                        invertedFile.insertRowTable(currentWord, Document, 1.0);
                    }
                }
            }
        }
    }

    /**
     * Create small inverted file for each query in experimental / interactive
     * Assumption : list of queries must have been loaded first
     * @param isStemmingApplied
     */
    public void loadIndexTabelForManualQuery(String query, boolean isStemmingApplied) {
        loadStopWordsFinal();
        query Query = new query(0, query);

        termWeightingQuery weightingQuery = new termWeightingQuery();
        String contentQuery = Query.getQueryContent().replace(EksternalFile.KONTEN_TOKEN,"");
        StringTokenizer token = new StringTokenizer(contentQuery, " +=-1234567890'(){}[]/.:;?!,\n");
        while (token.hasMoreTokens()) {
            String word = token.nextToken();
            if (!isStopWords(word)) {
                String filteredWord = "";
                if (isStemmingApplied) {
                    filteredWord = StemmingPorter.stripAffixes(word);
                } else {
                    filteredWord = word;
                }
                if (weightingQuery.getTermCounterInOneQuery().containsKey(filteredWord)) {
                    int currentCounter = weightingQuery.getTermCounterInOneQuery().get(filteredWord);
                    weightingQuery.getTermCounterInOneQuery().replace(filteredWord,currentCounter,currentCounter++);
                } else {
                    weightingQuery.getTermCounterInOneQuery().put(filteredWord, 1);
                }
                weightingQuery.getTermWeightInOneQuery().put(filteredWord, 1.0);
            }
        }
        query newQuery = new query(Query.getIndex(),contentQuery);
        weightingQuery.setCurrentQuery(newQuery);
        invertedFileManualQuery.getListQueryWeighting().add(weightingQuery);
    }

    public void loadIndexTabelForQueries(boolean isStemmingApplied) {
        loadQueriesFinal();
        loadStopWordsFinal();
        for (query Query : listQueriesFinal) {
            termWeightingQuery weightingQuery = new termWeightingQuery();
            String contentQuery = Query.getQueryContent().replace(EksternalFile.KONTEN_TOKEN,"");
            StringTokenizer token = new StringTokenizer(contentQuery, " +=-1234567890'(){}[]/.:;?!,\n");
            while (token.hasMoreTokens()) {
                String word = token.nextToken();
                if (!isStopWords(word)) {
                    String filteredWord = "";
                    if (isStemmingApplied) {
                        filteredWord = StemmingPorter.stripAffixes(word);
                    } else {
                        filteredWord = word;
                    }
                    if (weightingQuery.getTermCounterInOneQuery().containsKey(filteredWord)) {
                        int currentCounter = weightingQuery.getTermCounterInOneQuery().get(filteredWord);
                        weightingQuery.getTermCounterInOneQuery().replace(filteredWord,currentCounter,currentCounter++);
                    } else {
                        weightingQuery.getTermCounterInOneQuery().put(filteredWord, 1);
                    }
                    weightingQuery.getTermWeightInOneQuery().put(filteredWord, 1.0);
                }
            }
            query newQuery = new query(Query.getIndex(),contentQuery);
            weightingQuery.setCurrentQuery(newQuery);
            invertedFileQuery.getListQueryWeighting().add(weightingQuery);
        }
    }

    /**
     * Save documents segments from eksternal database into memory
     */
    private void loadDocumentsPerSegments() {
        EksternalFile file = new EksternalFile();
        file.loadListOfDocumentsPart(file.readDocuments("documents"));
        ArrayList<String> rawSegmentsDocuments = file.getListPartStringBetweenTokens();
        for (int i=0; i<rawSegmentsDocuments.size(); i++) {
            if (rawSegmentsDocuments.get(i).contains(file.INDEX_TOKEN)) {
                listIndexDocuments.add(Integer.parseInt(rawSegmentsDocuments.get(i).replace(file.INDEX_TOKEN, "")));
            } else if (rawSegmentsDocuments.get(i).contains(file.JUDUL_TOKEN)) {
                listTopicDocuments.add(rawSegmentsDocuments.get(i).replace(file.JUDUL_TOKEN, ""));
            } else if (rawSegmentsDocuments.get(i).contains(file.AUTHOR_TOKEN)) {
                listAuthorDocuments.add(rawSegmentsDocuments.get(i).replace(file.AUTHOR_TOKEN,""));
            } else if (rawSegmentsDocuments.get(i).contains(file.KONTEN_TOKEN)) {
                listContentDocuments.add(rawSegmentsDocuments.get(i).replace(file.KONTEN_TOKEN,""));
            }
        }
    }

    /**
     * Arrange list of documents before extracting words
     */
    public void loadDocumentsFinal() {
        loadDocumentsPerSegments();
        for (int i=0; i<listIndexDocuments.size(); i++) {
            int index = listIndexDocuments.get(i);
            String topic = listTopicDocuments.get(i);
            String author = listAuthorDocuments.get(i);
            String content = listContentDocuments.get(i);
            document Document = new document(index,topic,author,content);
            listDocumentsFinal.add(Document);
        }
    }

    /**
     * Load list of queries from eksternal source
     */
    public void loadQueriesFinal() {
        ArrayList<Integer> listQueryIndexes = new ArrayList<Integer>();
        ArrayList<String> listQueryContents = new ArrayList<String>();
        EksternalFile file = new EksternalFile();
        file.loadListOfDocumentsPart(file.readDocuments("queries"));
        for (int i=0; i<file.getListPartStringBetweenTokens().size(); i++) {
            if (file.getListPartStringBetweenTokens().get(i).contains(file.INDEX_TOKEN)) {
                String index = file.getListPartStringBetweenTokens().get(i).replace(file.INDEX_TOKEN,"");
                listQueryIndexes.add(Integer.parseInt(index));
            } else if (file.getListPartStringBetweenTokens().get(i).contains(file.KONTEN_TOKEN)) {
                listQueryContents.add(file.getListPartStringBetweenTokens().get(i));
            }
        }
        for (int i=0; i<listQueryIndexes.size(); i++) {
            int indexQuery = listQueryIndexes.get(i);
            String contentQuery = listQueryContents.get(i);
            listQueriesFinal.add(new query(indexQuery,contentQuery));
        }
    }

    /**
     * Load list of query relevances in documents from external source
     */
    public void loadQueryRelevancesFinal() {
        EksternalFile file = new EksternalFile();
        file.loadQueryRelevances(file.readDocuments("qrels"));
        ArrayList<Integer> listQueryIndexes = new ArrayList<Integer>();
        ArrayList<Integer> listDocumentIndexes = new ArrayList<Integer>();
        int sequenceCounter = 1;
        for (int i=0; i<file.getListPartIndexInQrels().size(); i++) {
            if (sequenceCounter == 1) {
                listQueryIndexes.add(file.getListPartIndexInQrels().get(i));
                sequenceCounter++;
            } else {
                listDocumentIndexes.add(file.getListPartIndexInQrels().get(i));
                if (sequenceCounter == 2) {sequenceCounter = 1;}
            }
        }
        for (int i=0; i<listQueryIndexes.size(); i++) {
            int queryIndex = listQueryIndexes.get(i);
            int documentIndex = listDocumentIndexes.get(i);
            listQueryRelevancesFinal.insertQueryRelevances(queryIndex,documentIndex);
        }
    }

    /**
     * Load list of stopwords from external source
     */
    private void loadStopWordsFinal() {
        EksternalFile file = new EksternalFile();
        String rawFileContent = file.readDocuments("stopwords");
        String[] lines = rawFileContent.split("\\r?\\n");
        for (String line : lines) {
            listStopWordsFinal.add(line);
        }
    }

    public static void main(String[] arg) {
       /* // CEK LOAD DOKUMEN DARI FILE ADI.ALL ATAU CISI.ALL
        PreprocessWords.loadDocumentsFinal();
        for (int i=0; i<PreprocessWords.getListDocumentsFinal().size(); i++) {
            System.out.println("No : " + PreprocessWords.getListDocumentsFinal().get(i).getIndex());
            System.out.println("Judul : " + PreprocessWords.getListDocumentsFinal().get(i).getJudul());
            System.out.println("Author : " + PreprocessWords.getListDocumentsFinal().get(i).getAuthor());
            System.out.println("Konten : " + PreprocessWords.getListDocumentsFinal().get(i).getKonten());
            System.out.println("================================================================================");
        }
        System.out.println("TOTAL DOKUMEN : " + PreprocessWords.getListDocumentsFinal().size());

        // CEK LOAD QUERY RELEVANCE DARI FILE QUERYRELEVANCE.TEXT
        PreprocessWords.loadQueryRelevancesFinal();
        for (int i=0; i<PreprocessWords.getListQueryRelevancesFinal().getListQueryRelevances().size(); i++) {
            for (ArrayList<Integer> j : PreprocessWords.getListQueryRelevancesFinal().getListQueryRelevances().values()) {
                System.out.println("Daftar dokumen relevan dengan query X");
                for (int k : j) {
                    System.out.println(k);
                }
                System.out.println("====================================================================================");
            }
        } */

        // CEK LOAD QUERY DARI FILE QUERY.TEXT
        /* PreprocessWords.loadQueriesFinal();
        for (int i=0; i<PreprocessWords.getListQueriesFinal().size(); i++) {
            System.out.println("Isi Query : " + PreprocessWords.getListQueriesFinal().get(i).getQueryContent());
            System.out.println("====================================================================================");
        } */

    }
}
