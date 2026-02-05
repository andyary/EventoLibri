package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Classe DAO per la gestione delle operazioni sul database relavice alla classe LibroLettore,
 * utile per la creazione della scaletta di lettura degli eventi.
 */
public class LibroLettoreDAO {
    private Connection connection;

    /**
     * Costruttore della classe LibroLettoreDAO.
     *
     * @param connection Connessione al database.
     */
    public LibroLettoreDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Recupera la scaletta di lettura per un evento specifico.
     *
     * @param id_evento ID dell'evento di cui recuperare la scaletta.
     * @return ArrayList di oggetti LibroLettore che rappresentano la scaletta di lettura.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<LibroLettore> getScaletta(int id_evento) throws SQLException {
        ArrayList<LibroLettore> scaletta = new ArrayList<>(); //lista vuota di libri e lettori
        String query = "SELECT * FROM librolettore ll JOIN libri l ON ll.id_libro=l.id  LEFT JOIN utenti u ON  ll.id_lettore=u.id " +
                "WHERE id_evento = ?"; //query per prendere la scaletta di un evento
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_evento); //imposto id dell'evento
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste scaletta per questo evento
                    return scaletta; //ritorno lista vuota
                else {
                    while (result.next()) {
                        CreaUtente<Lettore> creaLettore = new CreaLettore(); //creo il factory per i lettori
                        Lettore lettore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"),
                                result.getString("u.cognome"), result.getString("u.username")); //creo il lettore
                        Libro libro = new Libro(result.getString("l.titolo"), result.getInt("l.tempoLettura"),
                                result.getString("l.link"), result.getString("l.autore"),
                                result.getInt("l.id")); //creo il libro
                        LibroLettore libroLettore = new LibroLettore(libro, (Lettore) lettore, result.getInt("ll.progressivo")); //creo l'associazione libro-lettore
                        scaletta.add(libroLettore); //aggiungo alla scaletta
                    }
                    return scaletta; //ritorno la scaletta
                }
            } catch (SQLException ex) {
                System.out.println("7" + ex.getMessage()); //stampa messaggio di errore
                return scaletta; //ritorna la scaletta in caso di errore
            }
        } catch (SQLException ex) {
            System.out.println("8" + ex.getMessage()); //stampa messaggio di errore
            return scaletta; //ritorna la scaletta in caso di errore
        }
    }

    /**
     * Recupera gli eventi di lettura a cui un lettore è iscritto.
     * Evento contiene informazioni sul creatore, luogo e scaletta.
     * Evento contiene listener associati al creatore e ai lettori nella scaletta. Non contiene listener associati ai genitori dei figli iscrittti all'evento.
     *
     * @param lettore Lettore di cui recuperare gli eventi di lettura.
     * @return ArrayList di oggetti Evento che rappresentano gli eventi di lettura.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<Evento> getEventiLettura(Lettore lettore) throws SQLException {
        ArrayList<Evento> eventiLettura = new ArrayList<>(); //lista vuota di eventi di lettura
        String query = "SELECT * FROM librolettore ll JOIN eventi e JOIN luoghi l JOIN utenti u " +
                "ON e.id=ll.id_evento AND e.id_luogo=l.id AND u.id=e.id_creatore " +
                "WHERE ll.id_lettore = ? AND e.data >= NOW() ORDER BY e.data ASC"; //query per prendere gli eventi di lettura a cui è iscritto il lettore
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, lettore.getId()); //imposto id del lettore
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esistono eventi di lettura per questo lettore
                    return eventiLettura; //ritorno lista vuota
                else {
                    while (result.next()) {
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                        Lettore creatore = new CreaLettore().nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), getScaletta(result.getInt("e.id")));
                        int flag = 0; //flag di controllo per evitare duplicati
                        for (Evento ev : eventiLettura) {
                            if (ev.getId() == evento.getId()) {
                                flag = 1; // flag a 1 se l'evento è già presente nella lista
                                break; // esco dal ciclo
                            }
                        }
                        if (flag == 0) eventiLettura.add(evento); //aggiungo l'evento alla lista se non è già presente
                    }
                    return eventiLettura; //ritorno la lista di eventi di lettura
                }
            } catch (SQLException ex) {
                System.out.println("10" + ex.getMessage()); //stampa messaggio di errore
                return eventiLettura; //ritorna la lista di eventi di lettura in caso di errore
            }
        } catch (SQLException ex) {
            System.out.println("11" + ex.getMessage()); //stampa messaggio di errore
            return eventiLettura; //ritorna la lista di eventi di lettura in caso di errore
        }
    }

    /**
     * Crea nel database una nuova associazione tra un libro e un lettore per un evento specifico.
     *
     * @param evento      Evento a cui associare il libro e il lettore.
     * @param libro       Libro da associare.
     * @param lettore     Lettore da associare (può essere null).
     * @param progressivo Progressivo dell'associazione libro-lettore in scaletta.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void creaLibroLettore(Evento evento, Libro libro, Lettore lettore, int progressivo) throws SQLException {
        String query = "INSERT into librolettore (id_evento, id_libro, id_lettore, progressivo)   VALUES(?, ?, ?, ?)"; //query per creare l'associazione libro-lettore
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId()); //imposto id dell'evento
            pstatement.setInt(2, libro.getId()); //imposto id del libro
            // imposto id del lettore, può essere null
            if (lettore == null) {
                pstatement.setNull(3, java.sql.Types.INTEGER); //imposto id del lettore a null
            } else {
                pstatement.setInt(3, lettore.getId()); //imposto id del lettore
            }
            pstatement.setInt(4, progressivo); //imposto il progressivo
            pstatement.executeUpdate(); //eseguo l'update
        }
    }

    /**
     * Crea nel database la scaletta di lettura per un evento specifico.
     *
     * @param evento Evento per cui creare la scaletta di lettura.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void creaScaletta(Evento evento) throws SQLException {
        //scorro tutta la scaletta dell'evento
        for (LibroLettore libroLettore : evento.getScaletta()) {
            creaLibroLettore(evento, libroLettore.getLibro(), libroLettore.getLettore(), libroLettore.getProgressivo()); // crea l'associazione libro-lettore per ogni elemento della scaletta
        }
    }

    /**
     * Cancella la scaletta di lettura per un evento specifico dal database.
     *
     * @param evento Evento di cui cancellare la scaletta di lettura.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaScaletta(Evento evento) throws SQLException {
        String query = "DELETE from librolettore WHERE id_evento = ?"; //query per cancellare la scaletta di un evento
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId()); //imposto id dell'evento
            pstatement.executeUpdate(); //eseguo l'update
        }
    }
}
