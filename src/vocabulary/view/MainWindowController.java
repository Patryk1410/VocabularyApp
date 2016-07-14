package vocabulary.view;

import java.sql.SQLException;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
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
    @FXML
    private RadioButton allTablesRdbtn;
    @FXML
    private RadioButton allWordsRdbtn;
    @FXML
    private RadioButton toPolishRdbtn;
    
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
        boolean allTables = allTablesRdbtn.isSelected();
        boolean allWords = allWordsRdbtn.isSelected();
        boolean toPolish = toPolishRdbtn.isSelected();
        String tableName = allTables ? null : chooseTable.getSelectionModel().getSelectedItem();
        if ((!allTables && tableName != null) && (!allWords && !numberOfWordsField.getText().isEmpty()) && checkIfNumberValid()) { 
            int numberOfWords = allWords ? 0 : Integer.parseInt(numberOfWordsField.getText());
            mainApp.startTest(allTables, allWords, toPolish, tableName, numberOfWords);
        } else {
            boolean b1 = !allTables && tableName == null;
            boolean b2 = !allWords && numberOfWordsField.getText().isEmpty();
            boolean b3 = !allWords && !numberOfWordsField.getText().isEmpty() && !checkIfNumberValid();
            showWarningAlert(b1, b2, b3);
        }
    }
    
    private boolean showConfiramtionAlert(String tableName) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Remove table");
        alert.setHeaderText("Are you sure you want to remove table " + tableName + "?");
        alert.setContentText("This will remove the table and its entire content. Once it is done"
                + " it can't be reverted.");
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }
    
    private void showWarningAlert(boolean chooseTable, boolean enterNumber, boolean invalidNumber) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.initOwner(stage);
        alert.setTitle("Starting error");
        alert.setHeaderText("Cannot start test for the following reasons:");
        String s1 = chooseTable ? "-You did not choose a table\n" : "";
        String s2 = enterNumber ? "-You did not enter a number of words\n" : "";
        String s3 = invalidNumber ? "-You did not enter a valid number of words" : "";
        alert.setContentText(s1 + s2 + s3);
        alert.showAndWait();
    }
    
    private boolean checkIfNumberValid() {
        try {
            Integer.parseInt(numberOfWordsField.getText());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
