package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;

public class ValutePickerController {
    private Stage valutePickerStage;
    private CbrValute valute;
    private ObservableList<CbrValute> valuteObservableList = FXCollections.observableArrayList();
    private SelectionModel listViewSelectionModel;

    @FXML
    private TextField searchTextField;
    @FXML
    private ListView<CbrValute> valuteListView;
    @FXML
    Button chooseButton;
    @FXML
    Button cancelButton;

    @FXML
    public void initialize() {
        listViewSelectionModel = valuteListView.getSelectionModel();
        valuteListView.setOnMouseClicked(event -> {
            if(event.getButton() == MouseButton.PRIMARY &&
                    event.getClickCount() == 2 &&
                    listViewSelectionModel.getSelectedItem() != null) {
                choose();
            }
        });

        valuteListView.setCellFactory(param -> new ListCell<CbrValute>() {
            @Override
            protected void updateItem(CbrValute item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getCharCode() + " " + item.getName());
                }
            }
        });

        searchTextField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP:
                    listViewSelectionModel.selectPrevious();
                    valuteListView.requestFocus();
                    break;
                case DOWN:
                    listViewSelectionModel.selectNext();
                    valuteListView.requestFocus();
                    break;
                case ENTER:
                    choose();
                    break;
                case ESCAPE:
                    cancel();
                    break;
            }
        });
        valuteListView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    choose();
                    break;
                case ESCAPE:
                    cancel();
                    break;
                default:
                    if (event.getCode().isLetterKey()) {
                        searchTextField.appendText(event.getText());
                        searchTextField.requestFocus();
                    }
            }
        });
    }

    public CbrValute getValute() {
        return valute;
    }

    public void setValuteObservableList(List<CbrValute> valutes, boolean filterRuble) {
        this.valuteObservableList.setAll(valutes);
        if (filterRuble) {
            this.valuteObservableList.remove(this.valuteObservableList.size() - 1);
        }
        valuteListView.setItems(valuteObservableList);
        SearchableListView.set(searchTextField, valuteListView);
        valuteListView.getSelectionModel().selectFirst();
    }

    public void setValutePickerStage(Stage valutePickerStage, double x, double y) {
        this.valutePickerStage = valutePickerStage;
        valutePickerStage.setOnShown(event -> {
            valutePickerStage.setX(x);
            valutePickerStage.setY(y);
        });
    }

    @FXML
    private void choose() {
        valute = valuteListView.getSelectionModel().getSelectedItem();
        valutePickerStage.close();
    }

    @FXML
    private void cancel() {
        valutePickerStage.close();
    }

    public static CbrValute showValutePicker(String title, double x, double y, List<CbrValute> valutes, boolean filterRuble) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ValutePickerController.class.getClassLoader().getResource("valutePicker.fxml"));
            Parent valutePickerLayout = fxmlLoader.load();
            ValutePickerController valutePickerController = fxmlLoader.getController();
            valutePickerController.setValuteObservableList(valutes, filterRuble);

            Stage valutePickerStage = new Stage();
            valutePickerController.setValutePickerStage(valutePickerStage, x, y);

            Scene valutePickerScene = new Scene(valutePickerLayout);
            valutePickerStage.setScene(valutePickerScene);

            valutePickerStage.initModality(Modality.APPLICATION_MODAL);
            valutePickerStage.setTitle(title);
            valutePickerStage.showAndWait();

            return valutePickerController.getValute();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
