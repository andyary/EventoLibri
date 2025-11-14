package it.polimi.eventolibri.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DBGestore{
    private static final String CONFIG_FILE = "src/main/java/it/polimi/eventolibri/Model/config.properties";
    private static Connection connection = null;
    private DBGestore() { }
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream(CONFIG_FILE));
                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String pass = props.getProperty("db.password");
                connection = DriverManager.getConnection(url, user, pass);
            } catch (IOException e) {
                throw new SQLException("Errore nel caricamento configurazione DB", e);
            }
        }
        return connection;
    }
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
