package sample;

import Utils.TableResult;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

/**
 * Created by User on 25/11/2015.
 */
public class CheckBoxCellFactory implements Callback {
    @Override
    public TableCell call(Object param) {
        CheckBoxTableCell<TableResult,Boolean> checkBoxCell = new CheckBoxTableCell();
        return checkBoxCell;
    }
}
