package sample;

import javafx.stage.Stage;
import model.indexTabel;
import model.indexTabelQuery;

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
    public static indexTabelQuery queryinvertedfile;
}
