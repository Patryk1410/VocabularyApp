package vocabulary.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
    private TextField pField;
    @FXML
    private TextField fField;
    
    private Stage stage;
    private Scene previousScene;
    private String tableName;
    private ObservableList<Word> pWords;
    private ObservableList<Word> fWords;
    private ObservableList<Translation> translations;
    private List<Word> newWords;
    private List<Translation> newTranslations;
    private List<Translation> removedTranslations;
    
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
    }
    
    private boolean checkTextFields() {
        String pStr = pField.getText(), fStr = fField.getText();
        if (pStr == null || pStr.isEmpty() || fStr == null || fStr.isEmpty()) {
            return false;
        }
        String regex = "[a-zéèçàôûöüîïêâëäęóąśźćńł\\s]+";
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
        alert.setTitle("Błąd");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private Word createWord(String lang, String tableName, String word) {
        int id = getWordId(word, lang);
        //System.out.println("id retrived: " + id);
        if(id == -1) {
            int newId = getHighestId() + 1;
            //System.out.println("New id: " + newId);
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
        return res;
    }
    
    private int getWordId(String word, String lang) {
        String res = null;
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
