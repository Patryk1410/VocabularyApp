package vocabulary;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vocabulary.data.DatabaseHandler;
import vocabulary.data.StringsHolder;
import vocabulary.util.CustomString;
import vocabulary.view.AskQuestionController;
import vocabulary.view.MainWindowController;
import vocabulary.view.TableCreationDialogController;
import vocabulary.view.ViewTableController;

public class MainApp extends Application {

    private Stage primaryStage;
    private AnchorPane mainWindow;
    
    @Override
    public void start(Stage primaryStage) {
        StringsHolder.init();
        if (StringsHolder.fLanguage.equals("none") && StringsHolder.sLanguage.equals("none")) {
            setUpLanguages();
        }
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("VocabularyApp " + StringsHolder.version);
        this.primaryStage.setOnCloseRequest(event -> {
            DatabaseHandler.clearDatabase();
            saveAppData();
        });
        this.primaryStage.getIcons().add(new Image("file:resources/images/icon2.png"));
        showMainWindow();
    }
    
    public void showMainWindow() {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/MainWindow.fxml"));
        try {
            mainWindow = (AnchorPane) loader.load();
            Scene scene = new Scene(mainWindow);
            MainWindowController controller = loader.getController();
            controller.setMainApp(this);
            controller.setStage(primaryStage);
            primaryStage.resizableProperty().setValue(false);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            DatabaseHandler.setUpDatabase();
            launch(args);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean showTableCreationDialog(CustomString table) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/TableCreationDialog.fxml"));
        try {
            AnchorPane page = (AnchorPane) loader.load();
            
            Stage dialogStage = new Stage();
            dialogStage.resizableProperty().setValue(false);
            dialogStage.setTitle("New Table");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            dialogStage.getIcons().add(new Image("file:resources/images/icon2.png"));
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            
            TableCreationDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setTableName(table);
            dialogStage.showAndWait();
            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void showViewTable(String tableName) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/ViewTable.fxml"));
        try {
            AnchorPane page = (AnchorPane) loader.load();
            Scene scene = new Scene(page);
            Scene previousScene = primaryStage.getScene();
            ViewTableController controller = loader.getController();
            controller.setStage(primaryStage);
            controller.setPreviousScene(previousScene);
            controller.setTableName(tableName);
            controller.initData();
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void startTest(boolean allTables, boolean allWords, boolean toPolish, boolean learn, ObservableList<String> tableNames, int numberOfWords) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/AskQuestion.fxml"));
        try {
            AnchorPane page = (AnchorPane) loader.load();
            Stage stage = new Stage();
            stage.resizableProperty().setValue(false);
            stage.setTitle("Test");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(primaryStage);
            stage.getIcons().add(new Image("file:resources/images/icon2.png"));
            
            Scene scene = new Scene(page);
            stage.setScene(scene);
            
            AskQuestionController controller = loader.getController();
            controller.init(allTables, allWords, toPolish, learn, tableNames, numberOfWords);
            controller.setStage(stage);
            
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void setUpLanguages() {
        ObservableList<String> languages = FXCollections.observableArrayList();
        String[] isoLanguages = Locale.getISOLanguages();
        for (String s : isoLanguages) {
            Locale loc = new Locale(s, "");
            languages.add(loc.getDisplayLanguage(Locale.ENGLISH));
        }
        languages.sort(null);
        
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Set up language");
        alert.setHeaderText("Choose a language you speak");
            
        ListView<String> languageListView = new ListView<>();
        languageListView.setItems(languages);
        languageListView.setMaxHeight(Double.MAX_VALUE);
        languageListView.setMaxWidth(Double.MAX_VALUE);
        GridPane.setVgrow(languageListView, Priority.ALWAYS);
        GridPane.setHgrow(languageListView, Priority.ALWAYS);
        
        GridPane content = new GridPane();
        content.setMaxWidth(Double.MAX_VALUE);
        content.add(languageListView, 0, 0);
        
        alert.getDialogPane().setContent(content);
        
        alert.showAndWait();
        StringsHolder.fLanguage = languageListView.getSelectionModel().getSelectedItem();
        
        alert.setHeaderText("Choose language you want to learn");
        
        alert.showAndWait();
        StringsHolder.sLanguage = languageListView.getSelectionModel().getSelectedItem();
    }
    
    private void saveAppData() {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter("resources/app_data.txt"))) {
            bw.write(StringsHolder.fLanguage);
            bw.newLine();
            bw.write(StringsHolder.sLanguage);
            bw.newLine();
            bw.write(StringsHolder.version);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
