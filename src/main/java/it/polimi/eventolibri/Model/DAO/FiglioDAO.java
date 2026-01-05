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
        String query = "SELECT * FROM figli WHERE id_genitore = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, genitore.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return figli;
                else {
                    while (result.next()) {
                        Figlio figlio = new Figlio(result.getInt("id"), result.getString("nome"), result.getDate("dataNascita").toLocalDate(), getIscrizioni(result.getInt("id")), genitore);
                        figli.add(figlio);
                    }
                    return figli;
                }
            } catch (SQLException ex) {
                System.out.println("1" + ex.getMessage());
                return figli;
            }

        } catch (SQLException ex) {
            System.out.println("2" + ex.getMessage());
            return figli;
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
        String query = "SELECT * FROM iscrizioni i JOIN eventi e JOIN utenti u JOIN luoghi l ON i.id_evento=e.id AND e.id_creatore=u.id AND l.id=e.id_luogo WHERE i.id_figlio = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_figlio);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return iscrizioni;
                else {
                    while (result.next()) {
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore creatore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"),result.getInt("l.id"));
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                        ArrayList<LibroLettore> scaletta = libroLettoreDAO.getScaletta(result.getInt("e.id"));
                        iscrizioni.add(new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), scaletta)) ;
                    }
                    return iscrizioni;
                }
            } catch (SQLException ex) {
                System.out.println("3" + ex.getMessage());
                return iscrizioni;
            }

        } catch (SQLException ex) {
            System.out.println("4" + ex.getMessage());
            return iscrizioni;
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
        String query = "INSERT into figli (nome, dataNascita, id_genitore)   VALUES(?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome);
            pstatement.setDate(2, Date.valueOf(dataNascita));
            pstatement.setInt(3, genitore.getId());
            pstatement.executeUpdate();
            ResultSet rs = pstatement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    /** Cancella un figlio dal database, incluse le sue iscrizioni agli eventi.
     * @param figlio Figlio da cancellare.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaFiglio(Figlio figlio) throws SQLException {
        String query = "DELETE FROM iscrizioni WHERE id_figlio = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, figlio.getId());
            pstatement.executeUpdate();
        }
        String query2 = "DELETE FROM figli WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query2);) {
            pstatement.setInt(1, figlio.getId());
            pstatement.executeUpdate();
        }
    }

    /** Cancella tutti i figli associati a un genitore specifico, incluse le loro iscrizioni agli eventi.
     * @param genitore Genitore di cui cancellare i figli.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaFigli(Genitore genitore) throws SQLException {
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
        String query = "SELECT COUNT(*) AS count, l.capienza AS capienza, i.id_evento AS id_evento FROM iscrizioni i JOIN eventi e ON i.id_evento=e.id JOIN luoghi l ON e.id_luogo=l.id WHERE i.id_evento = ? GROUP BY i.id_evento";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (result.next()) {
                    int count = result.getInt("count");
                    int capienza = result.getInt("capienza");
                    if (count >= capienza) {
                        throw new SQLException("Capienza massima del luogo raggiunta.");
                    }
                }
            }
        }
        query = "INSERT into iscrizioni (id_figlio, id_evento)   VALUES(?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, figlio.getId());
            pstatement.setInt(2, evento.getId());
            pstatement.executeUpdate();
        }
    }

    /** Cancella l'iscrizione di un figlio a un evento specifico.
     * @param figlio Figlio da disiscrivere dall'evento.
     * @param evento Evento da cui disiscrivere il figlio.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaFiglioEvento(Figlio figlio, Evento evento) throws SQLException {
        String query = "DELETE FROM iscrizioni WHERE id_figlio = ? AND id_evento = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, figlio.getId());
            pstatement.setInt(2, evento.getId());
            pstatement.executeUpdate();
        }
    }
}
