package vocabulary.view;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class SelectTableController {
    
    @FXML
    private ListView<String> chooseTable;
    
    private Stage stage;
    private ObservableList<String> tableNames;
    private String tableName;
    
    public SelectTableController() { }
    
    @FXML
    private void initialize() { }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setTableNames(ObservableList<String> tableNames) {
        this.tableNames = tableNames;
        chooseTable.setItems(tableNames);
    }
    
    @FXML
    private void handleCancel() {
        tableName = null;
        stage.close();
    }
    
    @FXML
    private void handleOK() {
        tableName = chooseTable.getSelectionModel().getSelectedItem();
        stage.close();
    }

    public String getTableName() {
        return tableName;
    }
}
