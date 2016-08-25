package vocabulary.view;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vocabulary.MainApp;
import vocabulary.data.DatabaseHandler;
import vocabulary.model.Translation;
import vocabulary.model.Word;

public class ViewTableController {
    
    @FXML
    private TableView<Translation> wordsTable;
    @FXML
    private TableColumn<Translation,String> pWordColumn;
    @FXML
    private TableColumn<Translation,String> fWordColumn;
    @FXML
    private Button removeBtn;
    @FXML
    private Button anotherTable;
    
    @FXML
    private TextField pField;
    @FXML
    private TextField fField;
    @FXML
    private TextField searchField;
    @FXML 
    private Label tableNameLbl;
    
    private Stage stage;
    private Scene previousScene;
    private String tableName;
    private ObservableList<Word> pWords;
    private ObservableList<Word> fWords;
    private ObservableList<Translation> translations;
    private ObservableList<String> tableNames;
    private List<Word> newWords;
    private List<Translation> newTranslations;
    private List<Translation> removedTranslations;
    private int idFromDb;
    private boolean changesSaved = true;
    
    public ViewTableController() { }
    
    @FXML
    private void initialize() {
        pWordColumn.setCellValueFactory(cellData -> {
            SimpleStringProperty prop = new SimpleStringProperty();
            String word = getWord(cellData.getValue().getPid(), 'P');
            prop.set(word);
            return prop;
        });
        fWordColumn.setCellValueFactory(cellData -> {
            SimpleStringProperty prop = new SimpleStringProperty();
            String word = getWord(cellData.getValue().getFid(), 'F');
            prop.set(word);
            return prop;
        });
        wordsTable.getSelectionModel().selectedItemProperty().
            addListener((observable, oldValue, newValue) -> updateWindow());
        
    }
    
    private void updateWindow() {
        if(wordsTable.getSelectionModel().getSelectedItem() != null) {
            removeBtn.setDisable(false);
        } else {
            removeBtn.setDisable(true);
        }
    }
    
    public void initData() {
        try {
            translations = DatabaseHandler.getTranslationsFromTable(tableName);
            pWords = DatabaseHandler.getWordsFromTable('P', tableName);
            fWords = DatabaseHandler.getWordsFromTable('F', tableName);
            newWords = new ArrayList<Word>();
            newTranslations = new ArrayList<Translation>();
            removedTranslations = new ArrayList<Translation>();
            wordsTable.setItems(translations);
            idFromDb = DatabaseHandler.getHighestId();
            tableNameLbl.setText(tableName);
            tableNames = DatabaseHandler.getTableNames();
            tableNames.remove(tableName);
            if(tableNames.size() == 0) {
                anotherTable.setDisable(true);
            }
            else {
                anotherTable.setDisable(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setPreviousScene(Scene scene) {
        previousScene = scene;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    @FXML
    private void handleOk() {
        saveChanges();
        stage.setScene(previousScene);
    }
    
    @FXML
    private void handleCancel() {
        if (!changesSaved) {
            String header = "You have unsaved changes.";
            String message = "Would you like to save you changes now before you quit?";
            String title = "Unsaved changes";
            if(showConfiramtionAlert(header, message, title)) {
                saveChanges();
            }
            changesSaved = true;
        }
        stage.setScene(previousScene);
    }
    
    @FXML
    private void handleApply() {
        saveChanges();
    }
    
    @FXML
    private void handleAdd() {
        //System.out.println("adding");
        if(checkTextFields()) {
            //System.out.println("correct");
            String pStr = pField.getText(), fStr = fField.getText();
            pStr = pStr.trim().toLowerCase();
            fStr = fStr.trim().toLowerCase();
            Word pWord = createWord("P", tableName, pStr);
            if(!pWords.contains(pWord)) {
                pWords.add(pWord);
                if(!newWords.contains(pWord)) {
                    newWords.add(pWord);
                }
            }
            Word fWord = createWord("F", tableName, fStr);
            if(!fWords.contains(fWord)) {
                fWords.add(fWord);
                if(!newWords.contains(fWord)) {
                    newWords.add(fWord);
                }
            }
            Translation translation = new Translation(pWord.getId(), fWord.getId());
            if(!translations.contains(translation)) {
                //System.out.println("adding translatio");
                translations.add(translation);
                if(!newTranslations.contains(translation)) {
                    newTranslations.add(translation);
                }
                removedTranslations.remove(translation);
            }
            changesSaved = false;
            pField.clear();
            fField.clear();
            pField.requestFocus();
        } else {
            //System.out.println("error");
            showCustomAlert("Wrong input", "Correct input you inserted into textfields");
        }
    }
    
    @FXML
    private void handleRemove() {
        Translation translation = wordsTable.getSelectionModel().getSelectedItem();
        translations.remove(translation);
        newTranslations.remove(translation);
        removedTranslations.add(translation);
        changesSaved = false;
    }
    
    @FXML
    private void handleChooseAnotherTable() {
        try {
            if (!changesSaved) {
                String header = "You have unsaved changes.";
                String message = "Would you like to save you changes now before you quit?";
                String title = "Unsaved changes";
                if(showConfiramtionAlert(header, message, title)) {
                    saveChanges();
                }
                changesSaved = true;
            }
            String returnedTableName = displaySelectTable();
            if (returnedTableName != null) {
                tableName = returnedTableName;
                initData();
                tableNames = DatabaseHandler.getTableNames();
                tableNames.remove(tableName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSearch() {
        try {
            String word = searchField.getText();
            if (word.isEmpty()) {
                String header = "No word inserted";
                String message = "Please enter a word you want to find";
                showCustomAlert(header, message);
            } else {
                List<String> tableNames = DatabaseHandler.findWord(word);
                if (tableNames.isEmpty()) {
                    String header = "Word not found";
                    String message = "The word you're looking for is not in the database";
                    showCustomAlert(header, message);
                } else {
                    String title = "Search results";
                    String header = "The word you're looking for is in following tables:";
                    String message = "";
                    for (String s : tableNames) {
                        message += s + '\n';
                    }
                    showInfoAlert(title, header, message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String displaySelectTable() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("view/SelectTable.fxml"));
        AnchorPane page = (AnchorPane) loader.load();
        
        Stage selectStage = new Stage();
        selectStage.resizableProperty().setValue(false);
        selectStage.setTitle("Select Table");
        selectStage.initModality(Modality.WINDOW_MODAL);
        selectStage.initOwner(stage);
        selectStage.getIcons().add(new Image("file:resources/images/icon.png"));
        
        SelectTableController controller = loader.getController();
        controller.setStage(selectStage);
        controller.setTableNames(tableNames);
        Scene scene = new Scene(page);
        selectStage.setScene(scene);
        selectStage.showAndWait();
        
        return controller.getTableName();
    }
    
    private boolean checkTextFields() {
        String pStr = pField.getText(), fStr = fField.getText();
        if (pStr == null || pStr.isEmpty() || fStr == null || fStr.isEmpty()) {
            return false;
        }
        String regex = "[^,][^0-9~!@#$%^&*()_=+\\[{}\\]|\\\\;:<>/]+[^,]";
        if(pStr.matches(regex) && fStr.matches(regex)) {
            return true;
        } else {
            return false;
        }
    }
    
    private String getWord(int id, char lang) {
        String res = null;
        ObservableList<Word> temp = lang == 'P' ? pWords : fWords;
        for (Word w : temp) {
            if(w.getId() == id) {
                res = w.getWord();
            }
        }
        return res;
    }
    
    private void showCustomAlert(String header, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.initOwner(stage);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private boolean showConfiramtionAlert(String header, String message, String title) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }
    
    private void showInfoAlert(String title, String header, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private Word createWord(String lang, String tableName, String word) {
        int id = getWordId(word, lang);
        System.out.println("id retrived: " + id);
        if(id == -1) {
            int newId = getHighestId() + 1;
            System.out.println("New id: " + newId);
            return new Word(newId, lang, word, tableName);
        } else {
            return new Word(id, lang, word, tableName);
        }
    }
    
    private int getHighestId() {
        int res = 0;
        if(!pWords.isEmpty()) {
            res = pWords.get(pWords.size()-1).getId();
        }
        if(!fWords.isEmpty()) {
            int temp = fWords.get(fWords.size()-1).getId();
            if(temp > res) {
                res = temp;
            }
        }
        //System.out.println("Res: " + res);
        return res > idFromDb ? res : idFromDb;
        
    }
    
    private int getWordId(String word, String lang) {
        ObservableList<Word> temp = lang.equals("P") ? pWords : fWords;
        for (Word w : temp) {
            if(w.getWord().equals(word)) {
                return w.getId();
            }
        }
        return -1;
    }
    
    private void saveChanges() {
        try {
            for(Word w : newWords) {
                DatabaseHandler.insertWord(w);
            }
            for(Translation t : newTranslations) {
                DatabaseHandler.insertTranslation(t);
            }
            for(Translation t : removedTranslations) {
                DatabaseHandler.deleteTranslation(t);
            }
            changesSaved = true;
            DatabaseHandler.clearDatabase();
            newWords.clear();
            newTranslations.clear();
            removedTranslations.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
