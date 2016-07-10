package vocabulary.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
