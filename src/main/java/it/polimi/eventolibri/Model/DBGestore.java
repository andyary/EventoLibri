package it.polimi.eventolibri.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/** Classe per la gestione della connessione al database.
 */
public class DBGestore{
    private static final String CONFIG_FILE = "src/main/java/it/polimi/eventolibri/Model/config.properties"; // Percorso del file di configurazione
    private static Connection connection = null; // Connessione al database

    /** Costruttore privato per evitare istanziazioni della classe.
     */
    private DBGestore() { }

    /** Restituisce una connessione al database.
     * @return Connessione al database.
     * @throws SQLException Se si verifica un errore durante la connessione.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) { // Crea una nuova connessione se non esiste o è chiusa
            try {
                Properties props = new Properties(); // Carica le proprietà dal file di configurazione
                props.load(new FileInputStream(CONFIG_FILE)); // Caricamento del file di configurazione
                String url = props.getProperty("db.url"); // Ottiene l'URL del database
                String user = props.getProperty("db.user"); // Ottiene l'username del database
                String pass = props.getProperty("db.password"); // Ottiene la password del database
                connection = DriverManager.getConnection(url, user, pass); // Crea la connessione al database
            } catch (IOException e) {
                throw new SQLException("Errore nel caricamento configurazione DB", e); // Gestione errore di caricamento configurazione
            }
        }
        return connection; // Restituisce la connessione al database
    }

    /** Chiude la connessione al database.
     */
    public static void closeConnection() {
        if (connection != null) { // Chiude la connessione se esiste
            try {
                connection.close(); // Chiusura della connessione
            } catch (SQLException e) {
                e.printStackTrace(); // Gestione errore di chiusura connessione
            }
        }
    }
}
