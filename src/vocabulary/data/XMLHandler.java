package vocabulary.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import vocabulary.model.Translation;
import vocabulary.model.Word;

public class XMLHandler {

    public static void importDatabase(File file) throws ParserConfigurationException, SAXException, IOException, SQLException {
        List<Word> pWords = new Vector<>();
        List<Word> fWords = new Vector<>();
        List<Translation> translations = new Vector<>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        
        getWords(pWords, document, 'P');
        getWords(fWords, document, 'F');
        getTranslations(translations, document);
        
        System.out.println("pWords - " + pWords.size() + '\n' + "fWords - " + fWords.size());
        
        DatabaseHandler.importDatabase(pWords, fWords, translations);
    }
    
    public static Document exportDatabase(File file) throws SQLException, ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        
        Element rootElement = document.createElement("Data");
        document.appendChild(rootElement);
        
        exportWords(document, rootElement, 'P');
        exportWords(document, rootElement, 'F');
        exportTranslations(document, rootElement);
        
        return document;
    }
    
    private static void getWords(List<Word> words, Document document, char language) {
        System.out.println(language);
        NodeList wordNodes = document.getElementsByTagName(language == 'P' ? "pWord" : "fWord");
        for (int i = 0; i < wordNodes.getLength(); ++i) {
            Node wordNode = wordNodes.item(i);
            Element wordElem = (Element) wordNode;
            int wordId = Integer.parseInt(
                    wordElem.getElementsByTagName("id").item(0).getTextContent());
            String wordLang = wordElem.getElementsByTagName("language").item(0).getTextContent();
            String wordWord = wordElem.getElementsByTagName("word").item(0).getTextContent();
            String wordSource = wordElem.getElementsByTagName("source").item(0).getTextContent();
            Word word = new Word(wordId, wordLang, wordWord, wordSource);
            words.add(word);
        }
    }
    
    private static void getTranslations(List<Translation> translations, Document document) {
        NodeList translationNodes = document.getElementsByTagName("translation");
        for (int i = 0; i < translationNodes.getLength(); ++i) {
            Node translationNode = translationNodes.item(i);
            Element translationElem = (Element) translationNode;
            int id1 = Integer.parseInt(
                    translationElem.getElementsByTagName("id1").item(0).getTextContent());
            int id2 = Integer.parseInt(
                    translationElem.getElementsByTagName("id2").item(0).getTextContent());
            Translation translation = new Translation(id1, id2);
            translations.add(translation);
        }
    }
    
    private static void exportWords(Document document, Element rootElement, char language) throws SQLException {
        List<Word> wordList = DatabaseHandler.getWordList(language);
        System.out.println(language);
        Element words = document.createElement(language == 'P' ? "pWords" : "fWords");
        rootElement.appendChild(words);
        for (Word w : wordList) {
            Element word = document.createElement(language == 'P' ? "pWord" : "fWord");
            words.appendChild(word);
            Element idElem = document.createElement("id");
            word.appendChild(idElem);
            idElem.appendChild(document.createTextNode(Integer.toString(w.getId())));
            Element languageElem = document.createElement("language");
            word.appendChild(languageElem);
            languageElem.appendChild(document.createTextNode(Character.toString(w.getLanguage())));
            Element wordElem = document.createElement("word");
            word.appendChild(wordElem);
            wordElem.appendChild(document.createTextNode(w.getWord()));
            Element sourceElem = document.createElement("source");
            word.appendChild(sourceElem);
            sourceElem.appendChild(document.createTextNode(w.getSource()));
        }
    }
    
    private static void exportTranslations(Document document, Element rootElement) throws SQLException {
        List<Translation> translationList = DatabaseHandler.getTranslationList();
        Element translations = document.createElement("translations");
        rootElement.appendChild(translations);
        for (Translation t : translationList) {
            Element translation = document.createElement("translation");
            translations.appendChild(translation);
            Element firstId = document.createElement("id1");
            translation.appendChild(firstId);
            firstId.appendChild(document.createTextNode(Integer.toString(t.getPid())));
            Element secondId = document.createElement("id2");
            translation.appendChild(secondId);
            secondId.appendChild(document.createTextNode(Integer.toString(t.getFid())));
        }
    }
}
