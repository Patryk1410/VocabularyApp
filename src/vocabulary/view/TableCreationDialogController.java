package vocabulary.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import vocabulary.util.CustomString;

public class TableCreationDialogController {
    
    @FXML
    private TextField nameField;
    
    private Stage dialogStage;
    private CustomString name;
    private boolean okClicked;
    
    @FXML
    private void initialize() { }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public void setTableName(CustomString name) {
        this.name = name;
    }
    
    public boolean isOkClicked() {
        return okClicked;
    }
    
    @FXML
    private void handleOK() {
        if(isInputValid()) {
            name.set(nameField.getText());
            okClicked = true;
            dialogStage.close();
        }
    }
    
    @FXML
    private void handleCancel() {
        okClicked = false;
        dialogStage.close();
    }
    
    private boolean isInputValid() {
        String errorMessage = "";
        if(nameField.getText() == null) {
            errorMessage += "Field is empty\n";
        }
        if(nameField.getText().length() > 50) {
            errorMessage += "Name too long(it must be under 50 signs)\n";
        }
        
        if(errorMessage.length() == 0) {
            return true;
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Incorrect input");
            alert.setHeaderText("Please correct inserted data");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }
}
