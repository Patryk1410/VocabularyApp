package vocabulary.view;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
import vocabulary.model.Translation;
import vocabulary.model.Word;
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
    
    private void showInformationAlert(String msg, String title, String header) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    private boolean checkIfNumberValid() {
        try {
            int n = Integer.parseInt(numberOfWordsField.getText());
            return n > 0;
        } catch (NumberFormatException e) {
            return false;
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
                tables.remove(tableName);
            }
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
        if ((allTables || tableName != null) && (allWords || (!numberOfWordsField.getText().isEmpty() && checkIfNumberValid()))) { 
            int numberOfWords = allWords ? 0 : Integer.parseInt(numberOfWordsField.getText());
            mainApp.startTest(allTables, allWords, toPolish, tableName, numberOfWords);
        } else {
            boolean b1 = !allTables && tableName == null;
            boolean b2 = !allWords && numberOfWordsField.getText().isEmpty();
            boolean b3 = !allWords && !numberOfWordsField.getText().isEmpty() && !checkIfNumberValid();
            showWarningAlert(b1, b2, b3);
        }
    }
    
    @FXML
    private void handleAbout() {
        try(BufferedReader br = new BufferedReader(new FileReader("resources/about.txt"))) {
            String line = "";
            String msg = "";
            while((line = br.readLine()) != null) {
                msg += line + '\n';
            }
            String title = "About";
            String header = "Vocabulary app";
            showInformationAlert(msg, title, header);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handlePatchNotes() {
        String[] fileNames = {"1_1.txt", "1_2.txt"};
        String msg = "";
        String title = "Patch Notes";
        String header = "Patch Notes";
        for (String s : fileNames) {
            s = "resources/patch_notes/" + s;
            try (BufferedReader br = new BufferedReader(new FileReader(s))) {
                String line = "";
                while ((line = br.readLine()) != null) {
                    msg += line + '\n';
                }
                showInformationAlert(msg, title, header);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            s += '\n';
        }
    }
    
    @FXML
    private void handleImportDatabase() {
        try {
            List<Word> pWordList = DatabaseHandler.getWordList('P');
            List<Word> fWordList = DatabaseHandler.getWordList('F');
            List<Translation> translationList = DatabaseHandler.getTranslationList();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Element rootElement = document.createElement("Data");
            document.appendChild(rootElement);
            Element pWords = document.createElement("pWords");
            rootElement.appendChild(pWords);
            for (Word w : pWordList) {
                Element pWord = document.createElement("pWord");
                Attr idAttr = document.createAttribute("id");
                idAttr.setValue(Integer.toString(w.getId()));
                pWord.setAttributeNode(idAttr); //<- Tu skonczyles
                pWords.appendChild(pWord);
                
            }
            
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }
}
