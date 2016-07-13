package vocabulary;

import java.io.IOException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import vocabulary.data.DatabaseHandler;
import vocabulary.util.CustomString;
import vocabulary.view.MainWindowController;
import vocabulary.view.TableCreationDialogController;
import vocabulary.view.ViewTableController;

public class MainApp extends Application {

    private Stage primaryStage;
    private AnchorPane mainWindow;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Vocabulary Test");
        
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
}