package vocabulary.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.derby.client.am.SqlException;

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
    
    public static ObservableList<Translation> getTranslationsFromTable(String tableName) throws SQLException {
        ObservableList<Translation> res = FXCollections.observableArrayList();
        Statement s = connection.createStatement();
        String subQuery = "(select id from wordbank where source = '" + tableName + "')";
        String query = "select * from translations where id1 in "
                + subQuery + " or id2 in " + subQuery;
        ResultSet rs = s.executeQuery(query);
        while(rs.next()) {
//            System.out.println("iterating");
            int id1 = rs.getInt(1);
            int id2 = rs.getInt(2);
            res.add(new Translation(id1, id2));
        }
//        System.out.println(tableName);
//        System.out.println(res.size());
        return res;
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
}
