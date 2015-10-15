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
                    dotProduct = weightThisTermDocument * weightThisTermQuery;
                }
            }
        }
        double similarityDocument;
        double lengthOfQuery = lengthOfQuery(Query.getQueryContent(),invertedFileQuery);
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
     * @param query
     * @param invertedFileQuery
     * @return
     */
    private static double lengthOfQuery(String query, indexTabelQuery invertedFileQuery) {
        double sumSquareElement = 0.0;
        for (int i=0; i<invertedFileQuery.getListQueryWeighting().size(); i++) {
            String currentQuery = invertedFileQuery.getListQueryWeighting().get(i).getCurrentQuery().getQueryContent();
            if (currentQuery.equalsIgnoreCase(query)) {
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


    public static void main(String[] arg) {
        // PENTING DIBUAT DULU KELASNYA
        PreprocessWords wordProcessor = new PreprocessWords();
        EksternalFile.setPathDocumentsFile("test\\ADI\\adi.all");
        EksternalFile.setPathQueriesFile("test\\ADI\\query.text");
        EksternalFile.setPathQrelsFile("test\\ADI\\qrels.text");
        EksternalFile.setPathStopWordsFile("test\\stopwords_en.txt");

        // PROSES BIKIN INVERTED FILE BUAT DOCUMENT
        wordProcessor.loadIndexTabel(true); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeighting(2, wordProcessor.getInvertedFile()); // TF dengan logarithmic TF (khusus dokumen)
        TermsWeight.inverseDocumentWeighting(1, wordProcessor.getInvertedFile()); // IDS dengan with IDS (log N/Ntfi) (khusus dokumen)

        // PROSES BUAT INVERTED FILE BUAT QUERY
        wordProcessor.loadIndexTabelForQueries(true); // True : stemming diberlakukan
        TermsWeight.termFrequencyWeightingQuery(2, wordProcessor.getInvertedFileQuery()); // TF dengan logarithmic TF (khusus query)
        TermsWeight.termFrequencyWeightingQuery(1, wordProcessor.getInvertedFileQuery()); // IDS khusus query

        // SIMILARITY DOCUMENT QUERY KE-1 (INDEX 0) DENGAN DOKUMEN 1-82 ADI.ALL
        for (int j=0; j<wordProcessor.getListDocumentsFinal().size(); j++) {
            System.out.println("SIMILARITY QUERY KE-" + wordProcessor.getListQueriesFinal().get(0).getIndex()
            + " DENGAN DOKUMEN KE-" + wordProcessor.getListDocumentsFinal().get(j).getIndex() + " : ");
            System.out.println(countSimilarityDocument(wordProcessor.getListQueriesFinal().get(0),wordProcessor.getInvertedFileQuery(),
                    wordProcessor.getListDocumentsFinal().get(j),wordProcessor.getInvertedFile(),true));
            System.out.println("========================================================================================");
        }
    }
}
