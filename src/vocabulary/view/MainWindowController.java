package vocabulary.view;

import java.sql.SQLException;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import vocabulary.MainApp;
import vocabulary.data.DatabaseHandler;
import vocabulary.util.CustomString;

public class MainWindowController {
    
    @FXML
    private ChoiceBox<String> chooseTable;
    @FXML
    private Button editTable;
    @FXML
    private Button removeTable;
    @FXML
    private TextField numberOfWordsField;
    
    private MainApp mainApp;
    private ObservableList<String> tables;
    private Stage stage;
    
    public MainWindowController() { }
    
    @FXML
    private void initialize() {
        try {
            tables = DatabaseHandler.getTableNames();
            chooseTable.setItems(tables);
            chooseTable.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> updateWindowState());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    
    private void updateWindowState() {
        if(chooseTable.getSelectionModel().getSelectedItem() != null) {
            editTable.setDisable(false);
            removeTable.setDisable(false);
        } else {
            editTable.setDisable(true);
            removeTable.setDisable(true);
        }
    }
    
    @FXML
    private void handleAddTable() {
        CustomString tableName = new CustomString();
        boolean okClicked = mainApp.showTableCreationDialog(tableName);
        if(okClicked) {
            tables.add(tableName.get());
        }
    }
    
    @FXML 
    private void handleEditTable() {
        String tableName = chooseTable.getSelectionModel().getSelectedItem();
        mainApp.showViewTable(tableName);
    }
    
    @FXML
    private void handleRemoveTable() {
        try {
            String tableName = chooseTable.getSelectionModel().getSelectedItem();
            if(showConfiramtionAlert(tableName)) {
                DatabaseHandler.deleteTranslationsFromTable(tableName);
            }
            tables.remove(tableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAllWords() {
        numberOfWordsField.clear();
        numberOfWordsField.setDisable(true);
    }
    
    @FXML
    private void handleNumberOfWords() {
        numberOfWordsField.setDisable(false);
    }
    
    @FXML
    private void handleStart() {
        
    }
    
    private boolean showConfiramtionAlert(String tableName) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Remove table");
        alert.setHeaderText("Are you sure you want to remove table " + tableName + "?");
        alert.setContentText("This will remove the table and all of its content. Once it is done"
                + " it can't be reverted.");
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }
}
