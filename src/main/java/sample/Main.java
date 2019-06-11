package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class Main extends Application {
    public static final Parser parser = new Parser();
    private String cbrUrl = "http://www.cbr.ru/scripts/XML_daily.asp";
    private List<CbrValute> cbrValutes;
    private String date;
    private FileLoader fileLoader;

    private CalculatorController calculatorController;
    private QuotationController quotationController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        fileLoader = new FileLoader();
        File file = fileLoader.load(cbrUrl, "xmlDaily.xml");
        if (file == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка загрузки данных");
            alert.setContentText(
                    "Не найден резервный файл " +
                    "с последней загруженной информацией. " +
                    "Проверьте интернет соединение."
            );

            alert.showAndWait();
            return;
        }

        cbrValutes = parser.getCbrExchange(file);
        date = parser.getDate(file);

        CbrValute rub = new CbrValute();
        rub.setCharCode("RUB");
        rub.setName("Российский рубль");
        rub.setNominal(1);
        rub.setValue(1);
        rub.setId("");
        cbrValutes.add(rub);

        for (CbrValute valute : cbrValutes) {
            System.out.println(valute.toString());
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("calculator.fxml"));
        Parent calculatorNode = fxmlLoader.load();
        calculatorController = fxmlLoader.getController();
        calculatorController.setCalculatorStage(primaryStage);
        calculatorController.setValutes(cbrValutes);

        FXMLLoader mainScreenLoader = new FXMLLoader(getClass().getClassLoader().getResource("mainScreen.fxml"));
        Parent mainScreenNode = mainScreenLoader.load();
        MainScreenController mainScreenController = mainScreenLoader.getController();

        ListView<String> calcHistoryListView = new ListView<>();
        mainScreenController.getLeftVBox().getChildren().addAll(calculatorNode, calcHistoryListView);
        calculatorController.setHistoryListView(calcHistoryListView);
        calculatorController.loadHistoryFromFile();

        FXMLLoader chartsLoader = new FXMLLoader(getClass().getClassLoader().getResource("charts.fxml"));
        Parent chartsNode = chartsLoader.load();
        ChartsController chartsController = chartsLoader.getController();
        chartsController.setChartsStage(primaryStage);
        chartsController.setValutes(cbrValutes);
        setAnchors(chartsNode, 0d);
        mainScreenController.getDynamicsPane().getChildren().add(chartsNode);

        FXMLLoader quoutationLoader = new FXMLLoader(getClass().getClassLoader().getResource("quotationPage.fxml"));
        Parent quotationNode = quoutationLoader.load();
        quotationController = quoutationLoader.getController();
        quotationController.setValutes(cbrValutes, date);
        quotationController.loadFavValutes();
        quotationController.setHostServices(getHostServices());
        setAnchors(quotationNode, 0d);
        mainScreenController.getQuotationPane().getChildren().add(quotationNode);

        primaryStage.setTitle("Курсы валют");
        primaryStage.setScene(new Scene(mainScreenNode, 900, 610));
        primaryStage.sizeToScene();
        primaryStage.getIcons().add(
                new Image(getClass().getClassLoader().getResource("currency-converter.png").toString())
        );
        primaryStage.show();
    }

    private void setAnchors(Node node, double val) {
        AnchorPane.setLeftAnchor(node, val);
        AnchorPane.setTopAnchor(node, val);
        AnchorPane.setRightAnchor(node, val);
        AnchorPane.setBottomAnchor(node, val);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        calculatorController.saveHistoryToFile();
        quotationController.saveFavValutes();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
