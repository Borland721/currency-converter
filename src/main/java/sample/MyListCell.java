package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class MyListCell extends ListCell<CbrValute> {
    @FXML
    private HBox hBox;
    @FXML
    private Label name;
    @FXML
    private Label value;

    @Override
    protected void updateItem(CbrValute item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("listCell.fxml"));
            loader.setController(this);
            try {
                loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            name.setText(item.getCharCode() + " " + item.getName());
            value.setText(item.getValue() + "");
            setGraphic(hBox);
        }
    }

    public HBox gethBox() {
        return hBox;
    }
}

