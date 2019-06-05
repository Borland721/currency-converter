package sample;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChartsController {
    private Stage chartsStage;
    private List<CbrValute> valutes;

    private CbrValute targetValute;
    private CbrValute chartValute1;
    private CbrValute chartValute2;
    private CbrValute chartValute3;

    private AreaChart<String, Number> areaChart;

    private int valuesNumber = 15;
    private float lowerBound;
    private float upperBound;
    private int recordsSetted = 0;

    @FXML
    private BorderPane borderPane;
    @FXML
    private StackPane stackPane;

    @FXML
    private CheckBox valuteCheckbox1;
    @FXML
    private CheckBox valuteCheckbox2;
    @FXML
    private CheckBox valuteCheckbox3;

    @FXML
    private Button valuteButton1;
    @FXML
    private Button valuteButton2;
    @FXML
    private Button valuteButton3;
    @FXML
    private Text noDataText;

    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    private String fromDate = LocalDate.of(2019, 3, 3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    private String toDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    @FXML
    private Button reqButton;

    @FXML
    public void initialize() {
        //Defining the X axis
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setAnimated(false);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setAnimated(true);
        yAxis.setMinorTickCount(0);
        yAxis.setLabel("Цена");
        yAxis.setAutoRanging(false);
        yAxis.setTickUnit(0.5);

        areaChart = new AreaChart(xAxis, yAxis);
        areaChart.setTitle("Динамика котировок");

        AnchorPane.setLeftAnchor(areaChart, 5d);
        AnchorPane.setTopAnchor(areaChart, 5d);
        AnchorPane.setRightAnchor(areaChart, 5d);
        AnchorPane.setBottomAnchor(areaChart, 5d);
        stackPane.getChildren().add(0, areaChart);
        setDatePicker(fromDatePicker, toDatePicker);
    }

    private void setDatePicker(DatePicker... datePickers) {
        for (DatePicker datePicker : datePickers) {
            datePicker.setDayCellFactory(param -> new DateCell() {
                @Override
                public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    LocalDate minDate = LocalDate.of(1992, 7, 1);
                    if (item.isBefore(minDate) || item.isAfter(LocalDate.now())) {
                        setDisable(true);
                        setStyle("-fx-background-color: #ffc0cb;"); //To set background on different color
                    }
                }
            });
        }
    }

    @FXML
    private void valuteButtonClick(MouseEvent event) {
        Button clickedButton = (Button) event.getSource();
        CbrValute chosenValute = ValutePickerController.showValutePicker(
                "Выбор валюты",
                event.getScreenX(),
                event.getScreenY(),
                valutes,
                true
        );
        if (chosenValute != null) {
            if (clickedButton == valuteButton1) {
                valuteButton1.setText(chosenValute.getCharCode() + " " + chosenValute.getName());
                chartValute1 = chosenValute;
            } else if (clickedButton == valuteButton2) {
                valuteButton2.setText(chosenValute.getCharCode() + " " + chosenValute.getName());
                chartValute2 = chosenValute;
            } else if (clickedButton == valuteButton3) {
                valuteButton3.setText(chosenValute.getCharCode() + " " + chosenValute.getName());
                chartValute3 = chosenValute;
            }
        }
    }

    @FXML
    private void fromDateAction() {
        fromDate = fromDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @FXML
    private void toDateAction() {
        toDate = toDatePicker.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    @FXML
    private void request() {
        reqButton.setDisable(true);
        lowerBound = Float.MAX_VALUE;
        upperBound = 0;
        recordsSetted = 0;
        areaChart.getData().clear();
        Platform.runLater(() -> {
            noDataText.setVisible(false);
            areaChart.setEffect(null);
        });
        new Thread(() -> {
            if (valuteCheckbox1.isSelected()) {
                addRecords(chartValute1);
            }
            if (valuteCheckbox2.isSelected()) {
                addRecords(chartValute2);
            }
            if (valuteCheckbox3.isSelected()) {
                addRecords(chartValute3);
            }
            if (recordsSetted == 0) {
                Platform.runLater(() -> {
                    noDataText.setVisible(true);
                    areaChart.setEffect(new GaussianBlur(5));
                });
            }
            reqButton.setDisable(false);
        }).start();
    }

    private void addRecords(CbrValute valute) {
        String response = Main.parser.requestQuotationDynamics(
                "http://www.cbr.ru/scripts/XML_dynamic.asp?date_req1=" +
                        fromDate +
                        "&date_req2=" +
                        toDate +
                        "&VAL_NM_RQ=",
                valute.getId()
        );
        List<CbrRecord> records = Main.parser.getQuotationDynamics(response);
        if (records.size() > 0) {
            XYChart.Series series = new XYChart.Series();
            series.setName(valute.getName());
            ObservableList<XYChart.Data> data = series.getData();

            int interval = records.size() / valuesNumber;
            if (records.size() > 30) {
                for (int i = 0; i < valuesNumber; i++) {
                    float sum = 0;
                    for (int j = i * interval; j < i * interval + interval; j++) {
                        sum += records.get(j).getValue();
                    }
                    float avg = sum / interval;
                    if (avg > upperBound) {
                        upperBound = avg;
                    } else if (avg < lowerBound) {
                        lowerBound = avg;
                    }
                    data.add(new XYChart.Data(records.get(i * interval).getDate() + "", calc(avg, valute.getNominal(), targetValute)));
                }
            } else {
                for (int i = 0; i < records.size(); i++) {
                    CbrRecord record = records.get(i);
                    float value = record.getValue();
                    float nominal = record.getNominal();

                    if (value > upperBound) {
                        upperBound = value + 3;
                    } else if (value < lowerBound) {
                        lowerBound = value - 3;
                    }
                    data.add(new XYChart.Data(records.get(i).getDate(), calc(value, nominal, targetValute)));
                }
            }
            Platform.runLater(() -> {
                NumberAxis yAxis = (NumberAxis) areaChart.getYAxis();
                yAxis.setLowerBound(lowerBound - 1);
                yAxis.setUpperBound(upperBound + 1);
                areaChart.setTitle("Динамика котировок " + fromDate + " - " + toDate);
                areaChart.getData().add(series);
            });
            recordsSetted++;
        }
    }

    public void setValutes(List<CbrValute> valutes) {
        this.valutes = valutes;
        Platform.runLater(() -> {
            targetValute = valutes.get(valutes.size() - 1);
            chartValute1 = valutes.get(10);
            valuteButton1.setText(chartValute1.getCharCode() + " " + chartValute1.getName());
            chartValute2 = valutes.get(11);
            valuteButton2.setText(chartValute2.getCharCode() + " " + chartValute2.getName());
            chartValute3 = valutes.get(5);
            valuteButton3.setText(chartValute3.getCharCode() + " " + chartValute3.getName());
        });
    }

    public void setChartsStage(Stage chartsStage) {
        this.chartsStage = chartsStage;
    }

    private float calc(float from, float fromNominal, CbrValute to) {
        return (from / fromNominal) / ((to.getValue() / to.getNominal()));
    }
}
