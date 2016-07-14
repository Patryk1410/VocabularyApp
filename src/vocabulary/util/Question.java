package vocabulary.util;

import java.util.ArrayList;
import java.util.List;

public class Question {

    private String question;
    private List<String> answers;
    
    public Question() {
        question = "";
        answers = new ArrayList<String>();
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String q) {
        question = q;
    }
    
    public List<String> getAnswers() {
        return answers;
    }
    
    
}
