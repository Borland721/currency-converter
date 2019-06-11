package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import org.mariuszgromada.math.mxparser.Expression;

import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class CalculatorController {
    private Stage calculatorStage;
    private List<CbrValute> valutes;
    private CbrValute valute1;
    private CbrValute valute2;
    private CbrValute valute3;

    private DecimalFormat decimalFormat;

    private ListView<String> historyListView;

    @FXML
    public void initialize() {
        setFieldSettings(calculatorField1, calculatorField2, calculatorField3);
        highlightedField = calculatorField1;

        decimalFormat = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US));
    }

    private void setFieldSettings(TextField... fields) {
        for (TextField field : fields) {
            field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    highlightedField.setStyle("-fx-text-inner-color: black;");
                    highlightedField = field;
                    highlightedField.setStyle("-fx-text-inner-color: red;");
                }
            });
            field.textProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    if (field == highlightedField) {
                        setFields();
                    }
                });
            });
            field.setOnDragOver(event -> {
                if (event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.ANY);
                }
                event.consume();
            });
            field.setOnDragEntered(event -> {
//                визуальное представление перетаскивания
                if (event.getGestureSource() != field &&
                        event.getDragboard().hasString()) {
                    field.setStyle("-fx-background-color: #0096c9");
                }
                event.consume();
            });
            field.setOnDragExited(event -> {
                /* mouse moved away, remove the graphical cues */
                field.setStyle("-fx-background-color: white");
                event.consume();
            });
            field.setOnDragDropped(event -> {
                field.setText(event.getDragboard().getString());
                field.requestFocus();
                event.consume();
            });
        }
    }

    public void setCalculatorStage(Stage calculatorStage) {
        this.calculatorStage = calculatorStage;
    }

    public void setValutes(List<CbrValute> valutes) {
        this.valutes = valutes;
        //назначаем 3 валюты по умолчанию
        valute1 = valutes.get(valutes.size() - 1);
        valute2 = valutes.get(10);
        valute3 = valutes.get(11);
    }

    @FXML
    private Button valuteButton1;
    @FXML
    private Button valuteButton2;
    @FXML
    private Button valuteButton3;

    @FXML
    private TextField calculatorField1;
    @FXML
    private TextField calculatorField2;
    @FXML
    private TextField calculatorField3;
    private TextField highlightedField;

    @FXML
    private Label valuteLabel1;
    @FXML
    private Label valuteLabel2;
    @FXML
    private Label valuteLabel3;

    @FXML
    //Так как в ЦБ России разработчики назвали валюту valute,
    // было принято решение соответствовать api и использовать их наименование.
    private void valuteButtonClick(MouseEvent event) {
        Button clickedButton = (Button) event.getSource();
        CbrValute chosenValute = ValutePickerController.showValutePicker(
                "Выбор валюты",
                event.getScreenX(),
                event.getScreenY(),
                valutes,
                false
        );
        if (chosenValute != null) {
            if (clickedButton == valuteButton1) {
                valuteButton1.setText(chosenValute.getCharCode());
                valuteLabel1.setText(chosenValute.getName());
                valute1 = chosenValute;
            } else if (clickedButton == valuteButton2) {
                valuteButton2.setText(chosenValute.getCharCode());
                valuteLabel2.setText(chosenValute.getName());
                valute2 = chosenValute;
            } else if (clickedButton == valuteButton3) {
                valuteButton3.setText(chosenValute.getCharCode());
                valuteLabel3.setText(chosenValute.getName());
                valute3 = chosenValute;
            }
        }
    }

    @FXML
    private void setFields() {
        String fieldText = highlightedField.getText();
        if (fieldText.matches("([+-]\\d)?\\d+[.]?\\d*")) {
            float value = Float.parseFloat(fieldText.replace(",", "."));
            if (highlightedField == calculatorField1) {
                calculatorField2.setText(decFormat(convert(value, valute1, valute2)));
                calculatorField3.setText(decFormat(convert(value, valute1, valute3)));
            } else if (highlightedField == calculatorField2) {
                calculatorField1.setText(decFormat(convert(value, valute2, valute1)));
                calculatorField3.setText(decFormat(convert(value, valute2, valute3)));
            } else if (highlightedField == calculatorField3) {
                calculatorField1.setText(decFormat(convert(value, valute3, valute1)));
                calculatorField2.setText(decFormat(convert(value, valute3, valute2)));
            }
        }
    }

    private String decFormat(float number) {
        return decimalFormat.format(number);
    }

    @FXML
    private void calcExpression() {
        Platform.runLater(() -> {
            String expString = highlightedField.getText();
            Expression expression = new Expression(expString);
            if (expression.checkSyntax()) {
                highlightedField.setText(decFormat((float) expression.calculate()));
                if (!historyListView.getItems().contains(expString)) {
                    historyListView.getItems().add(expString);
                }
                if (historyListView.getItems().size() > 10) {
                    historyListView.getItems().remove(0);
                }
            }
        });
    }

    @FXML
    private void addButtonText(ActionEvent event) {
        Platform.runLater(() -> {
            String text = ((Button) event.getSource()).getText();
            int caretPos = highlightedField.getCaretPosition();
            highlightedField.replaceSelection(text);
            highlightedField.positionCaret(caretPos + text.length());
        });
    }

    @FXML
    private void clearField() {
        Platform.runLater(() -> {
            calculatorField1.clear();
            calculatorField2.clear();
            calculatorField3.clear();
        });
    }

    @FXML
    private void deleteChar() {
        Platform.runLater(() -> {
            int caretPos = highlightedField.getCaretPosition();
            if (caretPos == 0) {
                return;
            } else {
                highlightedField.replaceText(caretPos - 1, caretPos, "");
            }
        });
    }

    private float convert(float amount, CbrValute from, CbrValute to) {
        return amount * from.getValue() / (from.getNominal() * (to.getValue() / to.getNominal()));
    }

    public void setHistoryListView(ListView<String> historyListView) {
        historyListView.setCellFactory(param -> {
            ListCell<String> cell = new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                }
            };

            cell.setOnDragDetected(event -> {
                Dragboard dragboard = cell.startDragAndDrop(TransferMode.ANY);

                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString(cell.getItem());

                dragboard.setContent(clipboardContent);

                WritableImage cellImage = new WritableImage((int) cell.getWidth(), (int) cell.getHeight());
                SnapshotParameters spa = new SnapshotParameters();
                cell.snapshot(spa, cellImage);

                dragboard.setDragView(cellImage);
                event.consume();
            });
            return cell;
        });
        historyListView.setPrefWidth(Control.USE_COMPUTED_SIZE);
        historyListView.setPrefHeight(Control.USE_COMPUTED_SIZE);
        this.historyListView = historyListView;
    }

    public void loadHistoryFromFile() throws IOException {
        File file = new File("history.txt");
        if (file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Platform.runLater(() -> {
                try {
                    reader.lines().forEach(s -> historyListView.getItems().add(s));
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void saveHistoryToFile() {
        new Thread(() -> {
            try {
                String separator = System.lineSeparator();
                File file = new File("history.txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                for (String item : historyListView.getItems()) {
                    writer.write(item + separator);
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
