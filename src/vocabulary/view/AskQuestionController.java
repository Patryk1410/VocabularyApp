package vocabulary.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import vocabulary.data.DatabaseHandler;
import vocabulary.model.Word;

public class AskQuestionController {
    
    @FXML
    private Label counter;
    @FXML
    private Label question;
    @FXML
    private TextField answer;
    
    private Stage stage;

    private boolean allTables;
    private boolean allWords;
    private boolean toPolish;
    private int questionNumber;
    private int numberOfQuestions;
    private int questionId;
    private int score;
    private Map<String,List<String>> questions;
    private List<String> questionList;
    private Random randomGenerator;
    
    private String tableName;
    
    public AskQuestionController() { }
    
    public void init(boolean allTables, boolean allWords, boolean toPolish, String tableName, int numberOfQuestions) {
        this.allTables = allTables;
        this.allWords = allWords;
        this.toPolish = toPolish;
        this.tableName = tableName;
        questions = new TreeMap<String,List<String>>();
        questionList = new ArrayList<String>();
        questionNumber = 1;
        score = 0;
        randomGenerator = new Random();
        getQuestions();
        this.numberOfQuestions = allWords ? questions.size() : numberOfQuestions;
        this.questionId = allWords ? questionNumber - 1 : randomGenerator.nextInt(questionList.size());
        askQuestion();
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(event -> {
            if(!showConfirmCloseAlert()) {
                event.consume();
            }
        });
    }
    
    public void getQuestions() {
        try {
            List<Word> pWords = null;
            List<Word> fWords = null;
            if(!allTables) {
                pWords = DatabaseHandler.getWordListFromTable('P', tableName);
                fWords = DatabaseHandler.getWordListFromTable('F', tableName);
            } else {
                pWords = DatabaseHandler.getWordList('P');
                fWords = DatabaseHandler.getWordList('F');
            }
            List<Word> current = toPolish ? fWords : pWords;
            char lang = toPolish ? 'F' : 'P';
            for(Word w : current) {
                List<String> wordTranslations = DatabaseHandler.getWordTranslations(w, lang);
                questions.put(w.getWord(), wordTranslations);
                questionList.add(w.getWord());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCheck() {
        String ans = answer.getText();
        if (checkAnswer(ans)) {
            showCorrectAnswerAlert();
            score++;
        } else {
            String q = questionList.get(questionId);
            showWrongAnswerAlert(q);
        }
        if(questionNumber == numberOfQuestions) {
            showTestFinishedAlert();
            stage.close();
        } else {
            questionNumber++;
            questionId = allWords ? questionNumber - 1 : randomGenerator.nextInt(questionList.size());
            askQuestion();
        }
    }
    
    private boolean checkAnswer(String ans) {
        String q = questionList.get(questionId);
        ans = ans.trim().toLowerCase();
        return questions.get(q).contains(ans);
    }
    
    private void showCorrectAnswerAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Correct Answer");
        alert.setHeaderText("Correct Answer!");
        alert.setContentText("Your answer is correct");
        alert.showAndWait();
    }
    
    private void showWrongAnswerAlert(String q) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("Wrong Answer");
        alert.setHeaderText("Wrong Answer!");
        String correctAnswer = "";
        List<String> answers = questions.get(q);
        for(String s : answers) {
            correctAnswer += (s + ", ");
        }
        correctAnswer = correctAnswer.substring(0, correctAnswer.length()-2);
        alert.setContentText("Correct answer is: \n" + correctAnswer);
        alert.showAndWait();
    }
    
    private boolean showConfirmCloseAlert() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle("Exit test");
        alert.setHeaderText("Are you sure you want to exit the test?");
        alert.setContentText("All your progress will be lost.");
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }
    
    private void showTestFinishedAlert() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Test finished");
        alert.setHeaderText("The test is finished");
        int percent = score * 100 / numberOfQuestions;
        alert.setContentText("Your score is " + score + "/" + numberOfQuestions + " (" + percent + "%)");
        alert.showAndWait();
    }
    
    private void askQuestion() {
        answer.clear();
        answer.requestFocus();
        counter.setText(questionNumber + "/" + numberOfQuestions);
        String q = questionList.get(questionId);
        question.setText(q);
    }
}
