package sample;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.*;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

public class QuotationController {
    private FavValute[] favValutes;
    private CbrValute selectedValute;
    private HostServices hostServices;

    @FXML
    private ListView<CbrValute> valuteListView;
    @FXML
    private TextField searchField;
    @FXML
    private VBox favValutesContainer;
    @FXML
    private Text title;
    @FXML
    private Hyperlink cbrLink;

    @FXML
    private void initialize() {
        valuteListView.setCellFactory(param -> {
            MyListCell cell = new MyListCell();
            cell.setOnDragDetected(event -> {
                CbrValute valute = cell.getItem();
                if (valute != null) {
                    Dragboard dragboard = cell.startDragAndDrop(TransferMode.ANY);

                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(valute.getName());
                    dragboard.setContent(clipboardContent);
                    WritableImage cellImage = new WritableImage((int) cell.getWidth(), (int) cell.getHeight());
                    cell.snapshot(new SnapshotParameters(), cellImage);
                    dragboard.setDragView(cellImage);

                    selectedValute = valute;
                }
                event.consume();
            });
            return cell;
        });
    }

    @FXML
    private void openLink() {
        hostServices.showDocument("https://www.cbr.ru/");
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void setValutes(List<CbrValute> valutes, String date) {
        valuteListView.setItems(FXCollections.observableArrayList(valutes));
        ObservableList items = valuteListView.getItems();
        items.remove(items.size() - 1);
        SearchableListView.set(searchField, valuteListView);
        this.title.setText("Курс валют на " + date);
    }

    public void saveFavValutes() {
        new Thread(() -> {
            try {
                String separator = System.lineSeparator();
                File file = new File("favValutes.txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (FavValute valute : favValutes) {
                    writer.write(valute.getIndexInList() + separator);
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setFavValuteDropHandler(FavValute... valutes) {
        for (FavValute valute : valutes) {
            valute.getNode().setOnDragDropped(event -> {
                valute.setValute(selectedValute, valuteListView.getSelectionModel().getSelectedIndex());

                event.consume();
            });
        }
    }

    public void loadFavValutes() {
        Platform.runLater(() -> {
            try {
                ObservableList<CbrValute> valutes = valuteListView.getItems();
                int[] defaultIndexes = {10, 11, valutes.size() - 1};
                File file = new File("favValutes.txt");
                String[] lines = new String[3];
                favValutes = new FavValute[3];
                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    for (int i = 0; i < lines.length; i++) {
                        lines[i] = reader.readLine();
                    }
                    reader.close();
                }

                for (int i = 0; i < lines.length; i++) {
                    if (lines[i] == null) {
                        favValutes[i] = new FavValute();
                        favValutes[i].setValute(valutes.get(defaultIndexes[i]), defaultIndexes[i]);
                    } else {
                        int index = Integer.parseInt(lines[i]);
                        favValutes[i] = new FavValute();
                        favValutes[i].setValute(valutes.get(index), index);
                    }
                }
                setFavValuteDropHandler(favValutes);
                Platform.runLater(() -> favValutesContainer.getChildren().addAll(
                        favValutes[0].getNode(),
                        favValutes[1].getNode(),
                        favValutes[2].getNode()
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
