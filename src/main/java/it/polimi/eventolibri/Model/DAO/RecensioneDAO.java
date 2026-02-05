package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;
import java.sql.*;
import java.util.ArrayList;

/** Classe DAO per la gestione delle recensioni nel database.
 * Fornisce metodi per creare, recuperare e cancellare recensioni.
 */
public class RecensioneDAO {

    private Connection connection;

    /** Costruttore della classe RecensioneDAO.
     * @param connection Connessione al database.
     */
    public RecensioneDAO(Connection connection) {
        this.connection = connection;
    }

    /** Recupera le recensioni associate a un libro specifico.
     * @param libro Libro di cui recuperare le recensioni.
     * @return ArrayList di recensioni associate al libro.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public ArrayList<Recensione> getRecensione(Libro libro) throws SQLException {
        ArrayList<Recensione> recensioni = new ArrayList<>(); //lista vuota di recensioni
        String query = "SELECT * FROM recensioni r JOIN utenti u ON r.id_genitore=u.id WHERE id_libro = ?"; //query per prendere le recensioni di un libro
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, libro.getId()); //imposto id del libro
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esistono recensioni per questo libro
                    return recensioni; //ritorno lista vuota
                else {
                    // ciclo sui risultati
                    while (result.next()) {
                        CreaUtente<Genitore> creaGenitore = new CreaGenitore(); //creo il factory per i genitori
                        Genitore genitore = creaGenitore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username")); //creo il genitore
                        FiglioDAO figlioDAO = new FiglioDAO(connection); //creo il DAO dei figli
                        genitore.setFigli(figlioDAO.getFigli(genitore)); //aggiungo i figli al genitore
                        recensioni.add(new Recensione(result.getInt("id"), genitore, result.getString("testo"), libro)); //creo la recensione e la aggiungo alla lista
                    }
                    return recensioni; //ritorno la lista di recensioni
                }
            }catch (SQLException ex) {
                System.out.println("305" + ex.getMessage()); //stampa messaggio di errore
                return recensioni; //ritorna la lista di recensioni in caso di errore
            }
        }catch (SQLException ex) {
            System.out.println("306" + ex.getMessage()); //stampa messaggio di errore
            return recensioni; //ritorna la lista di recensioni in caso di errore
        }
    }

    /** Crea una nuova recensione nel database.
     * @param testo Testo della recensione.
     * @param libro Libro associato alla recensione.
     * @param genitore Genitore che ha scritto la recensione.
     * @return ID della recensione appena creata.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public int creaRecensione(String testo, Libro libro, Genitore genitore) throws SQLException {
        String query = "INSERT into recensioni (testo, id_libro, id_genitore)   VALUES(?, ?, ?)"; //query per creare una nuova recensione
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, testo); //imposto il testo della recensione
            pstatement.setInt(2, libro.getId()); //imposto l'id del libro
            pstatement.setInt(3, genitore.getId()); //imposto l'id del genitore
            pstatement.executeUpdate(); //eseguo la query
            ResultSet rs = pstatement.getGeneratedKeys(); //prendo le chiavi generate
            if (rs.next()) {
                return rs.getInt(1); //ritorno l'id della recensione appena creata
            }
            return -1; //ritorno -1 in caso di errore
        }
    }

    /** Cancella una recensione associata a un libro e a un genitore specifici.
     * @param libro Libro associato alla recensione.
     * @param genitore Genitore che ha scritto la recensione.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaRecensione(Libro libro, Genitore genitore) throws SQLException {
        String query = "DELETE FROM recensioni WHERE id_libro = ? AND id_genitore = ?"; //query per cancellare la recensione
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, libro.getId()); //imposto id del libro
            pstatement.setInt(2, genitore.getId()); //imposto id del genitore
            pstatement.executeUpdate(); //eseguo l'update
        }
    }

    /** Cancella una recensione specifica dal database (si riferisce alla recensione tramite suo id).
     * @param recensione Recensione da cancellare.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaRecensione(Recensione recensione) throws SQLException {
        String query = "DELETE FROM recensioni WHERE id = ?"; //query per cancellare la recensione
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, recensione.getId()); //imposto id della recensione
            pstatement.executeUpdate(); //eseguo l'update
        }
    }

    /** Verifica se un utente ha la possibilità di recensire un libro specifico.
     * Un utente può recensire un libro solo se è un genitore che ha almeno un figlio che
     * ha partecipato a un evento in cui il libro è stato letto.
     * Lettori e Amministratori non possono recensire libri.
     * @param libro Libro da recensire.
     * @param utente Utente che vuole recensire il libro.
     * @return true se l'utente può recensire il libro, false altrimenti.
     */
    public boolean getRecensibilita(Libro libro, Utente utente) {
        if (!(utente instanceof Genitore)) return false; //se l'utente non è un genitore ritorno false
        Genitore genitore = (Genitore) utente; //casto l'utente a genitore
        String query = "" +
                "SELECT * FROM iscrizioni i " +
                "JOIN eventi e ON i.id_evento = e.id " +
                "JOIN figli f ON i.id_figlio = f.id " +
                "JOIN utenti u ON f.id_genitore = u.id " +
                "JOIN librolettore ll ON e.id = ll.id_evento " +
                "WHERE u.id = ? AND ll.id_libro = ?  AND e.data < current_date()"; //query per verificare se il genitore ha un figlio che ha partecipato a un evento in cui il libro è stato letto
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, genitore.getId()); //imposto id del genitore
            pstatement.setInt(2, libro.getId()); //imposto id del libro
            try (ResultSet result = pstatement.executeQuery();) {
                if (result.next()) //se c'è almeno un risultato
                    return true; //ritorno true
                else {
                    return false; //ritorno false
                }
            }catch (SQLException ex) {
                System.out.println("Errore recensioneDao.getRecensibilità: " + ex.getMessage()); //stampa messaggio di errore
                return false; //ritorna false in caso di errore
            }
        }catch (SQLException ex) {
            System.out.println("Errore recensioneDao.getRecensibilità: " + ex.getMessage()); //stampa messaggio di errore
            return false; //ritorna false in caso di errore
        }
    }
}
