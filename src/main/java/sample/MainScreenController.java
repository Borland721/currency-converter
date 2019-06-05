package sample;

import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class MainScreenController {
    @FXML
    private AnchorPane quotationPane;
    @FXML
    private AnchorPane dynamicsPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private VBox leftVBox;

    @FXML
    private void initialize() {

    }

    public VBox getLeftVBox() {
        return leftVBox;
    }

    public AnchorPane getQuotationPane() {
        return quotationPane;
    }

    public AnchorPane getDynamicsPane() {
        return dynamicsPane;
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }
}
