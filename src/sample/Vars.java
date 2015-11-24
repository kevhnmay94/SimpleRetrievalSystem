package sample;

import Utils.Experiment;
import Utils.RelevanceFeedbackExperiment;
import Utils.RelevanceFeedbackInteractive;
import javafx.stage.Stage;
import model.indexTabel;
import model.normalTabel;

/**
 * Created by User on 26/10/2015.
 */
public class Vars {
    public static int documenttf = 1;
    public static int documentidf = 0;
    public static boolean norm = false;
    public static boolean documentstem = false;
    public static int querytf = 1;
    public static int queryidf = 0;
    public static boolean querystem = false;
    public static String documentlocation ="";
    public static String querylocation ="";
    public static String stoplocation = "";
    public static String rellocation = "";
    public static Stage savedstage;
    public static indexTabel documentinvertedfile;
    public static indexTabel queryinvertedfile;
    public static normalTabel documentnormalfile;
    public static normalTabel querynormalfile;
    public static Experiment exp;
    public static RelevanceFeedbackExperiment rexp;
    public static boolean isPseudo;
    public static int topS;
    public static int topN;
    public static boolean useQueryExpansion;
    public static boolean useSameCollection;
}
