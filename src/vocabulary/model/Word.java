package vocabulary.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Word {

    private final IntegerProperty id;
    private final StringProperty langauge;
    private final StringProperty word;
    private final StringProperty source;
    
    public Word() {
        this(-1, null, null, null);
    }
    
    public Word(int id, String language, String word, String source) {
        this.id = new SimpleIntegerProperty(id);
        this.langauge = new SimpleStringProperty(language);
        this.word = new SimpleStringProperty(word);
        this.source = new SimpleStringProperty(source);
    }
    
    @Override
    public boolean equals(Object o) {
        Word w = (Word) o;
        return o != null && id.get() == w.getId() && getLanguage() == w.getLanguage() &&
                word.get().equals(w.getWord()) && source.get().equals(w.getSource());
    }
    
    public void setId(int id) {
        this.id.set(id);
    }
    
    public int getId() {
        return id.get();
    }
    
    public IntegerProperty idProperty() {
        return id;
    }
    
    public void setLanguage(char language) {
        String s = "";
        s += language;
        this.langauge.set(s);
    }
    
    public char getLanguage() {
        return langauge.get().charAt(0);
    }
    
    public StringProperty languageProperty() {
        return langauge;
    }
    
    public void setWord(String word) {
        this.word.set(word);
    }
    
    public String getWord() {
        return word.get();
    }
    
    public StringProperty wordProperty() {
        return word;
    }
    
    public void setSource(String source) {
        this.source.set(source);
    }
    
    public String getSource() {
        return source.get();
    }
    
    public StringProperty sourceProperty() {
        return source;
    }
}
