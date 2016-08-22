package vocabulary.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import vocabulary.model.Translation;
import vocabulary.model.Word;

public class DatabaseHandler {

    private static Connection connection;
    
    public static void setUpDatabase() throws SQLException {
        boolean createTables;
        String connectionString;
        File f = new File("Database");
        if(f.exists()) {
            connectionString = "jdbc:derby:Database";
            createTables = false;
        } else {
            connectionString = "jdbc:derby:Database;create=true";
            createTables = true;
        }
        connection = DriverManager.getConnection(connectionString);
        if(createTables) {
            String query = "create table wordbank (id int primary key, lang char, "
                    + "word varchar(50), source varchar(50))";
            PreparedStatement p = connection.prepareStatement(query);
            p.execute();
            query = "create table translations (id1 int, id2 int)";
            p = connection.prepareStatement(query);
            p.execute();
            createBasic();
        }
    }
    
    public static void createBasic() throws SQLException {
        String[] values = {
                "1, 'P', 'ja', 'Basic'",
                "2, 'F', 'je', 'Basic'",
                "3, 'P', 'ty', 'Basic'",
                "4, 'F', 'tu', 'Basic'",
                "5, 'P', 'on', 'Basic'",
                "6, 'F', 'il', 'Basic'",
                "7, 'P', 'ona', 'Basic'",
                "8, 'F', 'elle', 'Basic'",
                "9, 'P', 'to', 'Basic'",
                "10, 'P', 'tak', 'Basic'",
                "11, 'F', 'oui', 'Basic'",
                "12, 'P', 'nie', 'Basic'",
                "13, 'F', 'non', 'Basic'"
        };
        String[] values2 = {
                "1, 2",
                "3, 4",
                "5, 6",
                "7, 8",
                "9, 6",
                "10, 11",
                "12, 13"
        };
        for(String s : values) {
            String query = "insert into wordbank values (" + s + ")";
            PreparedStatement ps = connection.prepareStatement(query);  
            ps.executeUpdate();
        }
        for(String s : values2) {
            String query = "insert into translations values (" + s + ")";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.executeUpdate();
        }
    }
    
    public static ObservableList<String> getTableNames() throws SQLException {
        ObservableList<String> res = FXCollections.observableArrayList();
        Statement s = connection.createStatement();
        String query = "select distinct source from wordbank";
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
            res.add(rs.getString(1));
        }
        return res;
    }
    
    public static ObservableList<Word> getWordsFromTable(char language, String tableName) throws SQLException {
        ObservableList<Word> res = FXCollections.observableArrayList();
        Statement s = connection.createStatement();
        String query = "select * from wordbank where lang = '" + language + "' and source = '" + tableName + "'";
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
            int id = rs.getInt(1);
            String lang = rs.getString(2);
            String word = rs.getString(3);
            String source = rs.getString(4);
            res.add(new Word(id, lang, word, source));
        }
        return res;
    }
    
    public static List<Word> getWordListFromTables(char language, ObservableList<String> tableNames) throws SQLException {
        List<Word> res = FXCollections.observableArrayList();
        Statement s = connection.createStatement();
        String query = "select * from wordbank where lang = '" + language + "' and source in ('";
        query += tableNames.get(0);
        for (int i = 1; i < tableNames.size(); ++i) {
            query += "', '" + tableNames.get(i);
        }
        query += "')";
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
            int id = rs.getInt(1);
            String lang = rs.getString(2);
            String word = rs.getString(3);
            String source = rs.getString(4);
            res.add(new Word(id, lang, word, source));
        }
        return res;
    }
    
    public static List<Word> getWordListFromTable(char language, String tableName) throws SQLException {
        List<Word> res = new ArrayList<Word>();
        Statement s = connection.createStatement();
        String query = "select * from wordbank where lang = '" + language + "' and source = '" + tableName + "'";
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
            int id = rs.getInt(1);
            String lang = rs.getString(2);
            String word = rs.getString(3);
            String source = rs.getString(4);
            res.add(new Word(id, lang, word, source));
        }
        return res;
    }
    
    public static ObservableList<Translation> getTranslationsFromTable(String tableName) throws SQLException {
        ObservableList<Translation> res = FXCollections.observableArrayList();
        Statement s = connection.createStatement();
        String subQuery = "(select id from wordbank where source = '" + tableName + "')";
        String query = "select * from translations where id1 in "
                + subQuery + " or id2 in " + subQuery;
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
            int id1 = rs.getInt(1);
            int id2 = rs.getInt(2);
            res.add(new Translation(id1, id2));
        }
        return res;
    }
    
    public static List<Translation> getTranslationListFromTable(String tableName) throws SQLException {
        List<Translation> res = new ArrayList<Translation>();
        Statement s = connection.createStatement();
        String subQuery = "(select id from wordbank where source = '" + tableName + "')";
        String query = "select * from translations where id1 in "
                + subQuery + " or id2 in " + subQuery;
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
            int id1 = rs.getInt(1);
            int id2 = rs.getInt(2);
            res.add(new Translation(id1, id2));
        }
        return res;
    }
    
    public static List<Translation> getTranslationList() throws SQLException {
        List<Translation> res = new ArrayList<Translation>();
        Statement s = connection.createStatement();
        String query = "select * from translations";
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
            int id1 = rs.getInt(1);
            int id2 = rs.getInt(2);
            res.add(new Translation(id1, id2));
        }
        return res;
    }
    
    public static void deleteTranslationsFromTable(String tableName) throws SQLException {
        Statement s = connection.createStatement();
        String subQuery = "(select id from wordbank where source = '" + tableName + "')";
        String query = "delete from translations where id1 in "
                + subQuery + " or id2 in " + subQuery;
        s.execute(query);
    }
    
    public static String getWord(int id) throws SQLException {
        Statement s = connection.createStatement();
        String query = "select word from wordbank where id = " + id;
        ResultSet rs = s.executeQuery(query);
        if (!rs.next()) {
            throw new SQLException("Word not found");
        }
        return rs.getString(1);
    }
    
    public static int getHighestId() throws SQLException {
        Statement s = connection.createStatement();
        String query = "select max(id) from wordbank";
        ResultSet rs = s.executeQuery(query);
        if(!rs.next()) {
            return 0;
        }
        return rs.getInt(1);
    }
    
    public static int getWordId(String word) throws SQLException {
        Statement s = connection.createStatement();
        String query = "select id from wordbank where word = '" + word + "'";
        ResultSet rs = s.executeQuery(query);
        if(!rs.next()) {
            return -1;
        }
        return rs.getInt(1);
    }
    
    public static void insertWord(Word w) throws SQLException {
        String query = "insert into wordbank values(?, ?, ?, ?)";
        String lang = "" + w.getLanguage();
        PreparedStatement s = connection.prepareStatement(query);
        s.setInt(1, w.getId());
        s.setString(2, lang);
        s.setString(3, w.getWord());
        s.setString(4, w.getSource());
        s.executeUpdate();
    }
    
    public static void insertTranslation(Translation t) throws SQLException {
        String query = "insert into translations values(?,?)";
        PreparedStatement s = connection.prepareStatement(query);
        s.setInt(1, t.getPid());
        s.setInt(2, t.getFid());
        s.executeUpdate();
    }
    
    public static void deleteTranslation(Translation t) throws SQLException {
        String query = "delete from translations where id1 = ? and id2 = ?";
        PreparedStatement s = connection.prepareStatement(query);
        s.setInt(1, t.getPid());
        s.setInt(2, t.getFid());
        s.executeUpdate();
    }
    
    public static List<String> getWordTranslations(Word w, char lang) throws SQLException {
        List<String> res = new ArrayList<>();
        Statement s = connection.createStatement();
        String chosenLanguage = lang == 'P' ? "id1" : "id2";
        String serchedLanguage = lang == 'P' ? "id2" : "id1";
        String query = "select word from wordbank where id in "
                + "(select " + serchedLanguage + " from translations where " + chosenLanguage + " = "
                + w.getId() + ")";
        ResultSet rs = s.executeQuery(query);
        while (rs.next()) {
            res.add(rs.getString(1));
        }
        return res;
    }
    
    public static List<Word> getWordList(char language) throws SQLException {
        List<Word> res = new ArrayList<Word>();
        Statement s = connection.createStatement();
        String query = "select * from wordbank where lang = '" + language + "'";
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
            int id = rs.getInt(1);
            String lang = rs.getString(2);
            String word = rs.getString(3);
            String source = rs.getString(4);
            res.add(new Word(id, lang, word, source));
        }
        return res;
    }
    
    public static void importDatabase(List<Word> pWords, List<Word> fWords, List<Translation> translations) throws SQLException {
        String query1 = "delete from wordbank";
        String query2 = "delete from translations";
        String query3 = "insert into wordbank values (?, ?, ?, ?)";
        String query4 = "insert into translations values (?, ?)";
        
        PreparedStatement s = connection.prepareStatement(query1);
        s.executeUpdate();
        s = connection.prepareStatement(query2);
        s.executeUpdate();
        s = connection.prepareStatement(query3);
        for (Word w : pWords) {
            s.setInt(1, w.getId());
            s.setString(2, "P");
            s.setString(3, w.getWord());
            s.setString(4, w.getSource());
            s.executeUpdate();
        }
        for (Word w : fWords) {
            s.setInt(1, w.getId());
            s.setString(2, "F");
            s.setString(3, w.getWord());
            s.setString(4, w.getSource());
            s.executeUpdate();
        }
        s = connection.prepareStatement(query4);
        for (Translation t : translations) {
            s.setInt(1, t.getPid());
            s.setInt(2, t.getFid());
            s.executeUpdate();
        }
    }
    
    public static List<String> findWord(String word) throws SQLException {
        List<String> res = new ArrayList<>();
        String query = "select source from wordbank where word = '" + word + "'";
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery(query);
        while (rs.next()) {
            res.add(rs.getString(1));
        }
        return res;
    }
    
    public static void clearDatabase() {
        try {
            Statement s = connection.createStatement();
            String query = 
                    "delete from wordbank where id not in "
                    + "(select id1 from translations) and id not in "
                    + "(select id2 from translations)";
            s.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
