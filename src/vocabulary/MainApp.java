package vocabulary;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import vocabulary.data.DatabaseHandler;

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
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void setUpDatabase() {
        File f = new File("Database");
        if(!f.exists()) {
            
        }
    }

    public static void main(String[] args) {
        try {
            DatabaseHandler.setUpDatabase();
            launch(args);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
