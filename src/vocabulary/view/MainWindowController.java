package vocabulary.view;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import vocabulary.MainApp;
import vocabulary.data.DatabaseHandler;
import vocabulary.data.StringsHolder;
import vocabulary.data.XMLHandler;
import vocabulary.util.CustomException;
import vocabulary.util.CustomString;
import vocabulary.util.StringComparator;

public class MainWindowController {
    
    @FXML
    private ListView<String> chooseTable;
    @FXML
    private Button editTable;
    @FXML
    private Button removeTable;
    @FXML
    private Button renameTable;
    @FXML
    private TextField numberOfWordsField;
    @FXML
    private RadioButton allTablesRdbtn;
    @FXML
    private RadioButton allWordsRdbtn;
    @FXML
    private RadioButton toPolishRdbtn;
    @FXML
    private RadioButton toFrenchRdbtn;
    @FXML
    private RadioButton learnRdbtn;
    
    private MainApp mainApp;
    private ObservableList<String> tables;
    private Stage stage;
    
    public MainWindowController() { }
    
    @FXML
    private void initialize() {
        try {
            tables = DatabaseHandler.getTableNames();
            tables.sort(new StringComparator());
            chooseTable.setItems(tables);
            chooseTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            chooseTable.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> updateWindowState());
            toPolishRdbtn.setText("Translate to " + StringsHolder.fLanguage.toLowerCase());
            toFrenchRdbtn.setText("Translate to " + StringsHolder.sLanguage.toLowerCase());
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
        if(chooseTable.getSelectionModel().getSelectedItems().size() == 1) {
            editTable.setDisable(false);
            removeTable.setDisable(false);
            renameTable.setDisable(false);
        } else {
            editTable.setDisable(true);
            removeTable.setDisable(true);
            renameTable.setDisable(true);
        }
    }
    
    private boolean showConfiramtionAlert(String tableName) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        ButtonType yesButtonType = new ButtonType("Yes", ButtonData.OK_DONE);
        ButtonType noButtonType = new ButtonType("No", ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButtonType, noButtonType);
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
    
    private void showErrorAlert(String msg, String title, String header) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    
    private String showRenameAlert(String tableName) {
        TextInputDialog alert = new TextInputDialog(tableName);
        alert.initOwner(stage);
        alert.setTitle("Rename table");
        alert.setHeaderText("Enter new name for the table " + tableName);
        alert.setContentText(null);
        Optional<String> res = alert.showAndWait();
        return res.get();
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
            mainApp.showViewTable(tableName.get());
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
    private void handleRenameTable() {
        try {
            String tableName = chooseTable.getSelectionModel().getSelectedItem();
            String newTableName = showRenameAlert(tableName);
            tables.remove(tableName);
            tables.add(newTableName);
            tables.sort(null);
            DatabaseHandler.renameTable(tableName, newTableName);
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
        boolean learn = learnRdbtn.isSelected();
        ObservableList<String> tableNames = allTables ? null : chooseTable.getSelectionModel().getSelectedItems();
        if ((allTables || !tableNames.isEmpty()) && (allWords || (!numberOfWordsField.getText().isEmpty() && checkIfNumberValid()))) { 
            int numberOfWords = allWords ? 0 : Integer.parseInt(numberOfWordsField.getText());
            mainApp.startTest(allTables, allWords, toPolish, learn, tableNames, numberOfWords);
        } else {
            boolean b1 = !allTables && tableNames.isEmpty();
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
        try {
            File htmlFile = new File("resources/patch_notes/patch_notes.html");
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExportDatabase() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Database");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("XML", "*.xml"));
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                throw new CustomException("File not created");
            }
            
            Document document = XMLHandler.exportDatabase(file);
            
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
            
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (CustomException e) {
            e.printStackTrace();
        } 
    }
    
    @FXML
    private void handleImportDatabase() {
        try { 
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Database");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("XLM", "*.xml"));
            File file = fileChooser.showOpenDialog(stage);
            
            XMLHandler.importDatabase(file);
            
            tables = DatabaseHandler.getTableNames();
            tables.sort(new StringComparator());
            chooseTable.setItems(tables);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
            String msg = "Please select valid file.";
            String title = "Parsing error";
            String header = "Invalid file has been selected";
            showErrorAlert(msg, title, header);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }  
    }
    
    @FXML
    private void handleHelp() {
        try {
            File htmlFile = new File("resources/manual.html");
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
