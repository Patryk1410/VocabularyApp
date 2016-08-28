package vocabulary.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import vocabulary.data.DatabaseHandler;
import vocabulary.model.Word;

public class AskQuestionController {
    
    @FXML
    private Label counter;
    @FXML
    private Label question;
    @FXML
    private Label correct;
    @FXML
    private TextField answer;
    @FXML
    private Button checkBtn;
    
    private Stage stage;

    private boolean allTables;
//    private boolean allWords;
    private boolean toPolish;
    private boolean learn;
    private boolean testFinished;
    private int questionNumber;
    private int numberOfQuestions;
    private int questionId;
    private int score;
    private Map<String,List<String>> questions;
    private List<String> questionList;
    private List<String> copyQuestionList;
    private Random randomGenerator;
    
    private ObservableList<String> tableNames;
    
    public AskQuestionController() { }
    
    public void init(boolean allTables, boolean allWords, boolean toPolish, boolean learn, ObservableList<String> tableNames, int numberOfQuestions) {
        this.allTables = allTables;
//        this.allWords = allWords;
        this.toPolish = toPolish;
        this.learn = learn;
        this.tableNames = tableNames;
        this.correct.setVisible(false);
        this.testFinished = false;
        questions = new TreeMap<>();
        questionList = new ArrayList<>();
        questionNumber = 1;
        score = 0;
        randomGenerator = new Random();
        getQuestions();
        this.numberOfQuestions = allWords ? questions.size() : numberOfQuestions;
        this.questionId = randomGenerator.nextInt(questionList.size());
        copyQuestionList = new ArrayList<>(questionList);
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
                pWords = DatabaseHandler.getWordListFromTables('P', tableNames);
                fWords = DatabaseHandler.getWordListFromTables('F', tableNames);
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
        if (testFinished) {
            showTestFinishedAlert();
            return;
        }
        boolean correctAnswer;
        String ans = answer.getText();
        String q = copyQuestionList.get(questionId);
        if (checkAnswer(ans)) {
            //showCorrectAnswerAlert();
            showCorrectAnswer();
            correctAnswer = true;
            score++;
        } else {
            correctAnswer = false;
            showWrongAnswer(q);
            //showWrongAnswerAlert(q);
        }
        if(questionNumber == numberOfQuestions && !learn) {
            testFinished = true;
            checkBtn.setText("Finish");
            //showTestFinishedAlert();
        } else {
            questionNumber++;
            if (correctAnswer || !learn)  {
                copyQuestionList.remove(questionId);
                if (copyQuestionList.isEmpty() && learn) {
                    testFinished = true;
                    checkBtn.setText("Finish");
                    //showTestFinishedAlert();
                    return;
                }
            }
            if (copyQuestionList.isEmpty() && !learn) {
                copyQuestionList = new ArrayList<>(questionList);
            }
            questionId = randomGenerator.nextInt(copyQuestionList.size());
            askQuestion();
        }
    }
    
    private boolean checkAnswer(String ans) {
        String q = copyQuestionList.get(questionId);
        ans = ans.trim().toLowerCase();
        List<String> processedAnswers = new ArrayList<>(), answers = questions.get(q);
        for (String s : answers) {
            while (s.contains(",")) {
                int index = s.indexOf(',');
                String tmp = s.substring(0, index);
                processedAnswers.add(tmp);
                s = s.substring(index + 1).trim();
            }
            processedAnswers.add(s);
        }
        return processedAnswers.contains(ans);
    }
    
    private void showCorrectAnswer() {
        stage.setHeight(239);
        checkBtn.setTranslateY(0);
        correct.setVisible(true);
        correct.setTextFill(Color.web("green"));
        correct.setText("Correct answer");
    }
    
    private void showWrongAnswer(String q) {
        stage.setHeight(260);
        checkBtn.setTranslateY(30);
        correct.setVisible(true);
        correct.setTextFill(Color.web("red"));
        String correctAnswer = "";
        List<String> answers = questions.get(q);
        for(String s : answers) {
            correctAnswer += (s + ", ");
        }
        correctAnswer = correctAnswer.substring(0, correctAnswer.length()-2);
        
        correct.setText("Wrong answer\nCorrect answer is:\n" + correctAnswer);
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
        ButtonType okButton = new ButtonType("Ok", ButtonData.OK_DONE);
        ButtonType repeatButton = new ButtonType("Repeat", ButtonData.RIGHT);
        alert.getButtonTypes().setAll(okButton, repeatButton);
        alert.initOwner(stage);
        alert.setTitle(learn ? "Learning finished" : "Test finished");
        alert.setHeaderText(learn ? "You finished learning this table" : "The test is finished");
        int percent = score * 100 / numberOfQuestions;
        alert.setContentText(learn ? "" : "Your score is " + score + "/" + numberOfQuestions + " (" + percent + "%)");
        alert.showAndWait().ifPresent(response -> {
           if (response == okButton) {
               stage.close();
           } else {
               repeatTest();
           }
        });
    }
    
    private void repeatTest() {
        copyQuestionList = new ArrayList<>(questionList);
        checkBtn.setText("Check");
        testFinished = false;
        correct.setVisible(false);
        if (learn) {
            askQuestion();
        } else {
            score = 0;
            questionNumber = 1;
            askQuestion();
        }
    }
    
    private void askQuestion() {
        answer.clear();
        answer.requestFocus();
        if (learn) {
            counter.setText("Questions left: " + copyQuestionList.size());
        } else {
            counter.setText(questionNumber + "/" + numberOfQuestions);
        }
        String q = copyQuestionList.get(questionId);
        question.setText(q);
    }
}
