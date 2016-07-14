package vocabulary.view;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import vocabulary.data.DatabaseHandler;
import vocabulary.model.Translation;
import vocabulary.model.Word;

public class AskQuestionController {

    private boolean allTables;
    private boolean allWords;
    private boolean toPolish;
    private int questionNumber;
    private int numberOfQuestions;
    private int score;
    private Map<String,List<String>> questions;
    
    private String tableName;
    
    public AskQuestionController() { }
    
    public void init(boolean allTables, boolean allWords, boolean toPolish, String tableName, int numberOfQuestions) {
        this.allTables = allTables;
        this.allWords = allWords;
        this.toPolish = toPolish;
        this.tableName = tableName;
        questions = new TreeMap<String,List<String>>();
        questionNumber = 1;
        score = 0;
        getQuestions();
        if(allWords) {
            this.numberOfQuestions = questions.size();
        } else {
            this.numberOfQuestions = numberOfQuestions;
        }
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
