package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;

import java.io.IOException;

public class FavValute {
    private int indexInList;

    @FXML
    private Label name;
    @FXML
    private Label value;
    @FXML
    private Node node;

    public FavValute() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("favValute.fxml"));
        loader.setController(this);
        node = loader.load();
    }

    public void setValute(CbrValute valute, int indexInList) {
        this.indexInList = indexInList;
        Platform.runLater(() -> {
            name.setText(valute.getCharCode() + " " + valute.getName());
            value.setText(valute.getValue() + "");
        });
    }

    @FXML
    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasString()) {
            event.acceptTransferModes(TransferMode.ANY);
        }

        event.consume();
    }

    @FXML
    private void handleDragEntered(DragEvent event) {
        if (event.getDragboard().hasString()) {
            node.setStyle("-fx-background-color: #0096c9");
        }

        event.consume();
    }
    @FXML
    private void handleDragExited(DragEvent event) {
        node.setStyle("-fx-background-color: white");

        event.consume();
    }



    public Node getNode() {
        return node;
    }


    public int getIndexInList() {
        return indexInList;
    }

    public void setIndexInList(int indexInList) {
        this.indexInList = indexInList;
    }
}
