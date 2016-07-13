package vocabulary.view;

import java.sql.SQLException;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
    
    private MainApp mainApp;
    private ObservableList<String> tables;
    
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
}
