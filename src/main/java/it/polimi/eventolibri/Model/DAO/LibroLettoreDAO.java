package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Classe DAO per la gestione delle operazioni sul database relavice alla classe LibroLettore,
 * utile per la creazione della scaletta di lettura degli eventi.
 */
public class LibroLettoreDAO {
    private Connection connection;

    /** Costruttore della classe LibroLettoreDAO.
     * @param connection Connessione al database.
     */
    public LibroLettoreDAO(Connection connection) {
        this.connection = connection;
    }

    /** Recupera la scaletta di lettura per un evento specifico.
     * @param id_evento ID dell'evento di cui recuperare la scaletta.
     * @return ArrayList di oggetti LibroLettore che rappresentano la scaletta di lettura.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<LibroLettore> getScaletta(int id_evento) throws SQLException {
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        String query = "SELECT * FROM librolettore ll JOIN libri l ON ll.id_libro=l.id  LEFT JOIN utenti u ON  ll.id_lettore=u.id WHERE id_evento = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_evento);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return scaletta;
                else {
                    while (result.next()) {
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        // CreaUtente creaLettore = new CreaLettore();
                        Lettore lettore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Libro libro = new Libro(result.getString("l.titolo"), result.getInt("l.tempoLettura"), result.getString("l.link"), result.getString("l.autore"), result.getInt("l.id"));
                        LibroLettore libroLettore = new LibroLettore(libro, (Lettore) lettore, result.getInt("ll.progressivo"));
                        scaletta.add(libroLettore);
                    }
                    return scaletta;
                }
            } catch (SQLException ex) {
                System.out.println("7" + ex.getMessage());
                return scaletta;
            }
        } catch (SQLException ex) {
            System.out.println("8" + ex.getMessage());
            return scaletta;
        }
    }

    /** Recupera gli eventi di lettura a cui un lettore è iscritto.
     * Evento contiene informazioni sul creatore, luogo e scaletta.
     * Evento contiene listener associati al creatore e ai lettori nella scaletta. Non contiene listener associati ai genitori dei figli iscrittti all'evento.
     * @param lettore Lettore di cui recuperare gli eventi di lettura.
     * @return ArrayList di oggetti Evento che rappresentano gli eventi di lettura.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<Evento> getEventiLettura(Lettore lettore) throws SQLException {
        ArrayList<Evento> eventiLettura = new ArrayList<>();
        String query = "SELECT * FROM librolettore ll JOIN eventi e JOIN luoghi l JOIN utenti u ON e.id=ll.id_evento AND e.id_luogo=l.id AND u.id=e.id_creatore WHERE ll.id_lettore = ? AND e.data >= NOW() ORDER BY e.data ASC";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, lettore.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return eventiLettura;
                else {
                    while (result.next()) {
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"),result.getInt("l.id"));
                        Lettore creatore = new CreaLettore().nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo , result.getTimestamp("e.data").toLocalDateTime(), getScaletta(result.getInt("e.id")) );
                        int flag = 0;
                        for (Evento ev : eventiLettura) {
                            if (ev.getId() == evento.getId()) {
                                flag=1;
                                break;
                            }
                        }
                        if (flag==0) eventiLettura.add(evento);
                    }
                    return eventiLettura;
                }
            } catch (SQLException ex) {
                System.out.println("10" + ex.getMessage());
                return eventiLettura;
            }

        } catch (SQLException ex) {
            System.out.println("11" + ex.getMessage());
            return eventiLettura;
        }
    }

    /** Crea nel database una nuova associazione tra un libro e un lettore per un evento specifico.
     * @param evento Evento a cui associare il libro e il lettore.
     * @param libro Libro da associare.
     * @param lettore Lettore da associare (può essere null).
     * @param progressivo Progressivo dell'associazione libro-lettore in scaletta.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void creaLibroLettore(Evento evento, Libro libro, Lettore lettore, int progressivo) throws SQLException {
        String query = "INSERT into librolettore (id_evento, id_libro, id_lettore, progressivo)   VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId());
            pstatement.setInt(2, libro.getId());
            if (lettore == null) {
                pstatement.setNull(3, java.sql.Types.INTEGER);
            } else {
                pstatement.setInt(3, lettore.getId());
            }
            pstatement.setInt(4, progressivo);
            pstatement.executeUpdate();
            }
    }

    /** Crea nel database la scaletta di lettura per un evento specifico.
     * @param evento Evento per cui creare la scaletta di lettura.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void creaScaletta(Evento evento) throws SQLException {
        for (LibroLettore libroLettore : evento.getScaletta()) {
            creaLibroLettore(evento, libroLettore.getLibro(), libroLettore.getLettore(), libroLettore.getProgressivo());
        }
    }

    /** Cancella la scaletta di lettura per un evento specifico dal database.
     * @param evento Evento di cui cancellare la scaletta di lettura.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaScaletta(Evento evento) throws SQLException {
        String query = "DELETE from librolettore WHERE id_evento = ?";
        try  (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId());
            pstatement.executeUpdate();
        }
    }
}
