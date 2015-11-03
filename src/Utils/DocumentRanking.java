package Utils;

import model.*;

import java.util.*;

/**
 * Created by steve on 13/10/2015.
 */
public class DocumentRanking {

    /**
     * Count similarity of document given string of query, document, and inverted file / termWeightQuery contains weight each term
     * Boolean isNormalized is given to calculate document similarity considering length of query and document
     * Assumption : weight each term in query and document has been calculated before based on various weighting method
     * @param Query
     * @param invertedFileQuery
     * @param Document
     * @param invertedFile
     * @param isNormalized
     * @return double
     */
    public static double countSimilarityDocument(query Query, indexTabelQuery invertedFileQuery, document Document, indexTabel invertedFile, boolean isNormalized) {
        double dotProduct = 0.0;
        for (int i=0; i<invertedFileQuery.getListQueryWeighting().size(); i++) {
            query currentQuery = invertedFileQuery.getListQueryWeighting().get(i).getCurrentQuery();
            if (currentQuery.getIndex() == Query.getIndex()) {
                for (Map.Entry m : invertedFileQuery.getListQueryWeighting().get(i).getTermWeightInOneQuery().entrySet()) {
                    double weightThisTermQuery = (Double) m.getValue();
                    double weightThisTermDocument = getWeightTermInDocument(Document.getIndex(),(String) m.getKey(), invertedFile);
                    dotProduct += weightThisTermDocument * weightThisTermQuery;
                }
            }
        }
        double similarityDocument;
        double lengthOfQuery = lengthOfQuery(Query,invertedFileQuery);
        double lengthOfDocument = lengthOfDocument(Document,invertedFile);
        if (isNormalized) {
            similarityDocument = dotProduct / (lengthOfQuery * lengthOfDocument);
        } else {
            similarityDocument = dotProduct;
        }
        return similarityDocument;
    }

    /**
     * Return weight a term contained in a document with index : indexDocument
     * @param indexDocument
     * @param term
     * @param invertedFile
     * @return weightTermInDocument
     */
    private static double getWeightTermInDocument(int indexDocument, String term, indexTabel invertedFile) {
        double weightTermInDocument = 0.0;
        for (Map.Entry m : invertedFile.getListTermWeights().entrySet()) {
            if (((String) m.getKey()).equals(term)) {
                ArrayList<document> listDocumentInThisTerm = ((termWeightingDocument) m.getValue()).getDocumentPerTerm();
                for (int i=0; i<listDocumentInThisTerm.size(); i++) {
                    if (listDocumentInThisTerm.get(i).getIndex() == indexDocument) {
                        weightTermInDocument = ((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm().get(i); break;
                    }
                }
            }
        }
        return weightTermInDocument;
    }

    /**
     * Compute length of query in Normalization Formula
     * @param Query
     * @param invertedFileQuery
     * @return
     */
    private static double lengthOfQuery(query Query, indexTabelQuery invertedFileQuery) {
        double sumSquareElement = 0.0;
        for (int i=0; i<invertedFileQuery.getListQueryWeighting().size(); i++) {
            int currentQueryIndex = invertedFileQuery.getListQueryWeighting().get(i).getCurrentQuery().getIndex();
            if (currentQueryIndex == Query.getIndex()) {
                for (Map.Entry m : invertedFileQuery.getListQueryWeighting().get(i).getTermCounterInOneQuery().entrySet()) {
                    sumSquareElement += Math.pow((Integer) m.getValue(),2);
                }
            }
        }
        return Math.sqrt(sumSquareElement);
    }

    /**
     * Compute length of Document in Normalization Formula
     * @param Document
     * @param invertedFile
     * @return
     */
    private static double lengthOfDocument(document Document, indexTabel invertedFile) {
        double sumSquareElement = 0.0;
        for (Map.Entry m : invertedFile.getListTermWeights().entrySet()) {
            int numDocumentInThisTerm = invertedFile.getListTermWeights().get(m.getKey()).getDocumentPerTerm().size();
            for (int i=0; i<numDocumentInThisTerm; i++) {
                document currentDocument = invertedFile.getListTermWeights().get(m.getKey()).getDocumentPerTerm().get(i);
                if (currentDocument.getIndex() == Document.getIndex()) {
                    int counterTermInCurrentDocument = invertedFile.getListTermWeights().get(m.getKey()).getDocumentCountersPerTerm().get(i);
                    sumSquareElement += Math.pow(counterTermInCurrentDocument,2);
                }
            }
        }
        return Math.sqrt(sumSquareElement);
    }

    /**
     * Return sorted HashMap of document and its weight
     * @param weightedDocs unsorted HashMap of document and its weight
     * @return
     */
    public static HashMap<document, Double> rankDocuments(HashMap<document, Double> weightedDocs) {
        return (HashMap<document, Double>) sortByComparator(weightedDocs);
    }

    private static Map<document, Double> sortByComparator(Map<document, Double> unsortMap) {

        // Convert Map to List
        List<Map.Entry<document, Double>> list =
                new LinkedList<Map.Entry<document, Double>>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<document, Double>>() {
            public int compare(Map.Entry<document, Double> o1,
                               Map.Entry<document, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Convert sorted map back to a Map
        Map<document, Double> sortedMap = new LinkedHashMap<document, Double>();
        int counter=0;
        double val;
        for (Iterator<Map.Entry<document, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<document, Double> entry = it.next();
            if (entry.getValue() > 0.00000000000000f) {
                counter++;
                sortedMap.put(entry.getKey(), entry.getValue());
            } else
                break;
        }

        return sortedMap;
    }


    public static void main(String[] arg) {
        // PENTING DIBUAT DULU KELASNYA
        PreprocessWords wordProcessor = new PreprocessWords();
        EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathQrelsFile("test\\ADI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");
       /* EksternalFile.setPathDocumentsFile("test\\CISI\\cisi.all");
        EksternalFile.setPathQueriesFile("test\\CISI\\query.text");
        EksternalFile.setPathQrelsFile("test\\CISI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt"); */

        // PROSES BIKIN INVERTED FILE BUAT DOCUMENT
        wordProcessor.loadIndexTabel(false); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeighting(1, wordProcessor.getInvertedFile()); // TF dengan logarithmic TF (khusus dokumen)
        TermsWeight.inverseDocumentWeighting(0, wordProcessor.getInvertedFile()); // IDS dengan with IDS (log N/Ntfi) (khusus dokumen)

        // PROSES BUAT INVERTED FILE BUAT QUERY
        wordProcessor.loadIndexTabelForQueries(false); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(1, wordProcessor.getInvertedFileQuery()); // TF dengan logarithmic TF (khusus query)
        TermsWeight.inverseDocumentWeightingQuery(0, wordProcessor.getInvertedFileQuery(), wordProcessor.getInvertedFile()); // IDS khusus query

        // SIMILARITY DOCUMENT QUERY KE-1 (INDEX 0) DENGAN DOKUMEN 1-82 ADI.ALL
        for (int j=0; j<wordProcessor.getListDocumentsFinal().size(); j++) {
            System.out.println("SIMILARITY QUERY KE-" + wordProcessor.getListQueriesFinal().get(13).getIndex()
            + " DENGAN DOKUMEN KE-" + wordProcessor.getListDocumentsFinal().get(j).getIndex() + " : ");
            System.out.println(countSimilarityDocument(wordProcessor.getListQueriesFinal().get(13),wordProcessor.getInvertedFileQuery(),
                    wordProcessor.getListDocumentsFinal().get(j),wordProcessor.getInvertedFile(),false));
            System.out.println("========================================================================================");
        }

        // TEST WRITE INVERTED FILE QUERY EKSTERNAL
        String path = "test\\invertedFile.txt";
        String path2 = "test\\invertedFileQuery.txt";
        EksternalFile file = new EksternalFile();
        file.writeInvertedFile(path, wordProcessor.getInvertedFile());
        file.writeInvertedFileQuery(path2,wordProcessor.getInvertedFileQuery());

        // TEST LOAD INVERTED FILE EKSTERNAL
        /* indexTabel anotherInvertedFile = EksternalFile.loadInvertedFile(path);
        for(Map.Entry m : anotherInvertedFile.getListTermWeights().entrySet()) {
             System.out.println("Key : " + m.getKey().toString());
             for (document Document:((termWeightingDocument) m.getValue()).getDocumentPerTerm()) {
                 System.out.println("Id : " + Document.getIndex());
                 System.out.println("Judul : " + Document.getJudul());
                 // System.out.println("Author : " + Document.getAuthor());
                 // System.out.println("Konten : " + Document.getKonten());
             }
             System.out.println("BOBOT TERM INI PER DOKUMEN DI ATAS : ");
             for (double weights :((termWeightingDocument) m.getValue()).getDocumentWeightingsPerTerm()) {
                 System.out.println("Bobot : " + weights);
             }
             // System.out.println("COUNTER TERM INI PER DOKUMEN DI ATAS : ");
             // for (int counter:((termWeightingDocument) m.getValue()).getDocumentCountersPerTerm()) {
                // System.out.println("Counter : " + counter);
             // }
             System.out.println("====================================================================================");
        }*/

        // TEST LOAD INVERTED FILE QUERY
       /* indexTabelQuery indexTabel = EksternalFile.loadInvertedFileQuery(path2);
        for (int i=0; i<indexTabel.getListQueryWeighting().size(); i++) {
            termWeightingQuery relation = indexTabel.getListQueryWeighting().get(i);
            System.out.println("QUERY DIPROSES : " + relation.getCurrentQuery().getQueryContent());
            System.out.println("COUNTER PER TERM DARI QUERY DI ATAS : ");
            //for (Map.Entry m : relation.getTermCounterInOneQuery().entrySet()) {
              //  System.out.println("Term : " + (String) m.getKey());
                //System.out.println("Counter : " + (Integer) m.getValue());
            //}
            for (Map.Entry m : relation.getTermWeightInOneQuery().entrySet()) {
                System.out.println("Term : " + (String) m.getKey());
                System.out.println("Weight : " + (Double) m.getValue());
            }
            System.out.println("====================================================================================");
        } */
    }
}
