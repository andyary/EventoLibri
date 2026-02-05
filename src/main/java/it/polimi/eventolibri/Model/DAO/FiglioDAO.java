package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/** Classe DAO per la gestione dei figli nel database.
 * Contiene metodi per ottenere, creare, cancellare figli e gestire le iscrizioni agli eventi.
 */
public class FiglioDAO {

    private Connection connection;

    /** Costruttore della classe FiglioDAO.
     * @param connection Connessione al database.
     */
    public FiglioDAO(Connection connection) {
        this.connection = connection;
    }

    /** Recupera i figli associati a un genitore specifico.
     * Figlio viene popolato con le iscrizioni agli eventi.
     * @param genitore Genitore di cui recuperare i figli.
     * @return ArrayList di figli associati al genitore.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<Figlio> getFigli(Genitore genitore) throws SQLException {
        ArrayList<Figlio> figli = new ArrayList<>();
        String query = "SELECT * FROM figli WHERE id_genitore = ?"; // Recupera i figli del genitore
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, genitore.getId()); // Imposta l'ID del genitore
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esistono figli per questo genitore
                    return figli; // Ritorna lista vuota
                else {
                    while (result.next()) {
                        Figlio figlio = new Figlio(result.getInt("id"), result.getString("nome"),
                                result.getDate("dataNascita").toLocalDate(),
                                getIscrizioni(result.getInt("id")), genitore); // Popola le iscrizioni del figlio
                        figli.add(figlio); // Aggiungi il figlio alla lista
                    }
                    return figli; // Ritorna la lista dei figli
                }
            } catch (SQLException ex) {
                System.out.println("1" + ex.getMessage()); // Stampa l'errore
                return figli; // Ritorna la lista dei figli (potrebbe essere vuota)
            }

        } catch (SQLException ex) {
            System.out.println("2" + ex.getMessage()); // Stampa l'errore
            return figli; // Ritorna la lista dei figli (potrebbe essere vuota)
        }
    }


    /** Recupera le iscrizioni agli eventi per un figlio specifico.
     * Evento viene popolato con il creatore, luogo e scaletta dei libri.
     * Evento contiene solo i listener associati al creatore e ai lettori in scaletta. Non contiene listener dei genitori iscritti.
     * @param id_figlio ID del figlio di cui recuperare le iscrizioni.
     * @return ArrayList di eventi a cui il figlio è iscritto.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<Evento> getIscrizioni(int id_figlio) throws SQLException {
        ArrayList<Evento> iscrizioni = new ArrayList<>();
        String query = "SELECT * FROM iscrizioni i JOIN eventi e JOIN utenti u JOIN luoghi l ON i.id_evento=e.id AND e.id_creatore=u.id AND l.id=e.id_luogo WHERE i.id_figlio = ?"; // Recupera le iscrizioni del figlio
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_figlio); // Imposta l'ID del figlio
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esistono iscrizioni per questo figlio
                    return iscrizioni; // Ritorna lista vuota
                else {
                    while (result.next()) {
                        CreaUtente<Lettore> creaLettore = new CreaLettore(); // Crea un lettore per il creatore dell'evento
                        Lettore creatore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username")); // Crea il creatore dell'evento
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"),result.getInt("l.id")); // Crea il luogo dell'evento
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection); // DAO per recuperare la scaletta dei libri
                        ArrayList<LibroLettore> scaletta = libroLettoreDAO.getScaletta(result.getInt("e.id")); // Recupera la scaletta dei libri per l'evento
                        iscrizioni.add(new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), scaletta)) ; // Aggiungi l'evento alla lista delle iscrizioni
                    }
                    return iscrizioni; // Ritorna la lista delle iscrizioni
                }
            } catch (SQLException ex) {
                System.out.println("3" + ex.getMessage()); // Stampa l'errore
                return iscrizioni; // Ritorna la lista delle iscrizioni (potrebbe essere vuota)
            }
        } catch (SQLException ex) {
            System.out.println("4" + ex.getMessage()); // Stampa l'errore
            return iscrizioni; // Ritorna la lista delle iscrizioni (potrebbe essere vuota)
        }
    }

    /** Crea un nuovo figlio nel database. Ritorna l'ID del figlio appena creato. -1 in caso di errore.
     * @param nome Nome del figlio.
     * @param dataNascita Data di nascita del figlio.
     * @param genitore Genitore del figlio.
     * @return ID del figlio appena creato.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public int creaFiglio(String nome, LocalDate dataNascita, Genitore genitore) throws SQLException {
        String query = "INSERT into figli (nome, dataNascita, id_genitore)   VALUES(?, ?, ?)"; // Crea un nuovo figlio
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome); // Imposta il nome del figlio
            pstatement.setDate(2, Date.valueOf(dataNascita)); // Imposta la data di nascita del figlio
            pstatement.setInt(3, genitore.getId()); // Imposta l'ID del genitore
            pstatement.executeUpdate(); // Esegui l'update
            ResultSet rs = pstatement.getGeneratedKeys(); // Recupera le chiavi generate
            if (rs.next()) {
                return rs.getInt(1); // Ritorna l'ID del figlio appena creato
            }
            return -1; // Ritorna -1 in caso di errore
        }
    }

    /** Cancella un figlio dal database, incluse le sue iscrizioni agli eventi.
     * @param figlio Figlio da cancellare.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaFiglio(Figlio figlio) throws SQLException {
        String query = "DELETE FROM iscrizioni WHERE id_figlio = ?"; // Cancella le iscrizioni del figlio
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, figlio.getId()); // Imposta l'ID del figlio
            pstatement.executeUpdate(); // Esegui l'update
        }
        String query2 = "DELETE FROM figli WHERE id = ?"; // Cancella il figlio
        try (PreparedStatement pstatement = connection.prepareStatement(query2);) {
            pstatement.setInt(1, figlio.getId()); // Imposta l'ID del figlio
            pstatement.executeUpdate(); // Esegui l'update
        }
    }

    /** Cancella tutti i figli associati a un genitore specifico, incluse le loro iscrizioni agli eventi.
     * @param genitore Genitore di cui cancellare i figli.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaFigli(Genitore genitore) throws SQLException {
        // Cancella tutti i figli del genitore
        for (Figlio figlio : genitore.getFigli()) {
            cancellaFiglio(figlio);
        }
    }

    /** Iscrive un figlio a un evento specifico, controllando la capienza del luogo.
     * @param figlio Figlio da iscrivere all'evento.
     * @param evento Evento a cui iscrivere il figlio.
     * @throws SQLException Se si verifica un errore durante l'accesso al database o se la capienza massima del luogo è raggiunta.
     */
    public void iscriviFiglioEvento(Figlio figlio, Evento evento) throws SQLException {
        String query = "SELECT COUNT(*) AS count, l.capienza AS capienza, i.id_evento AS id_evento FROM iscrizioni i " +
                "JOIN eventi e ON i.id_evento=e.id JOIN luoghi l ON e.id_luogo=l.id WHERE i.id_evento = ? GROUP BY i.id_evento"; // Controlla la capienza del luogo
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId()); // Imposta l'ID dell'evento
            try (ResultSet result = pstatement.executeQuery();) {
                if (result.next()) {
                    int count = result.getInt("count"); // Numero di iscritti all'evento
                    int capienza = result.getInt("capienza"); // Capienza del luogo
                    if (count >= capienza) {
                        throw new SQLException("Capienza massima del luogo raggiunta."); // Lancia un'eccezione se la capienza è raggiunta
                    }
                }
            }
        }
        query = "INSERT into iscrizioni (id_figlio, id_evento)   VALUES(?, ?)"; // Iscrivi il figlio all'evento
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, figlio.getId()); // Imposta l'ID del figlio
            pstatement.setInt(2, evento.getId()); // Imposta l'ID dell'evento
            pstatement.executeUpdate(); // Esegui l'update
        }
    }

    /** Cancella l'iscrizione di un figlio a un evento specifico.
     * @param figlio Figlio da disiscrivere dall'evento.
     * @param evento Evento da cui disiscrivere il figlio.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaFiglioEvento(Figlio figlio, Evento evento) throws SQLException {
        String query = "DELETE FROM iscrizioni WHERE id_figlio = ? AND id_evento = ?"; // Cancella l'iscrizione del figlio all'evento
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, figlio.getId()); // Imposta l'ID del figlio
            pstatement.setInt(2, evento.getId()); // Imposta l'ID dell'evento
            pstatement.executeUpdate(); // Esegui l'update
        }
    }
}
