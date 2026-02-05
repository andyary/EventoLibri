package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.*;
import java.util.ArrayList;

/**
 * Classe DAO per la gestione degli utenti nel database.
 */
public class UtenteDAO {

    /**
     * Attributo: Connessione al database.
     */
    private Connection connection;

    /**
     * Costruttore della classe UtenteDAO.
     *
     * @param connection Connessione al database.
     */
    public UtenteDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Metodo per verificare le credenziali di un utente, usato in fase di login.
     * In base al tipo di utente, viene creato l'oggetto corrispondente.
     *
     * @param username Username dell'utente.
     * @param psw      Password dell'utente.
     * @return Utente se le credenziali sono corrette, null altrimenti.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public Utente checkCredentials(String username, String psw) throws SQLException {
        String query = "SELECT * FROM utenti WHERE username = ? AND psw = ?"; //query per verificare le credenziali
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, username); //imposto username
            pstatement.setString(2, psw); //imposto password
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return null; //ritorno null
                else {
                    result.next(); //sposto il cursore al primo risultato
                    switch (
                            result.getString("tipo")) { //controllo il tipo di utente
                        case "Amministratore": //se e' un amministratore
                            CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore(); //creo il factory per gli amministratori
                            Amministratore amministratore = creaAmministratore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username")); //creo l'amministratore
                            return amministratore; //ritorno l'amministratore
                        case "Lettore": //se e' un lettore
                            CreaUtente<Lettore> creaLettore = new CreaLettore(); //creo il factory per i lettori
                            Lettore lettore = creaLettore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username")); //creo il lettore
                            lettore.setEventiCreati(new EventoDAO(connection).getEventiCreati(lettore)); //aggiungo gli eventi creati dal lettore
                            lettore.setIscrizioniLettura(new LibroLettoreDAO(connection).getEventiLettura(lettore)); //aggiungo le iscrizioni di lettura del lettore
                            return lettore; //ritorno il lettore
                        case "Genitore": //se e' un genitore
                            CreaUtente<Genitore> creaGenitore = new CreaGenitore(); //creo il factory per i genitori
                            Genitore genitore = creaGenitore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username")); //creo il genitore
                            FiglioDAO figlioDAO = new FiglioDAO(connection); //creo il DAO dei figli
                            genitore.setFigli(figlioDAO.getFigli(genitore)); //aggiungo i figli al genitore
                            return genitore; //ritorno il genitore
                        default: //se il tipo non e' riconosciuto
                            return null; //ritorno null
                    }
                }
            } catch (SQLException ex) {
                System.out.println("5" + ex.getMessage()); //stampa messaggio di errore
                return null; //ritorna null in caso di errore
            }
        } catch (SQLException ex) {
            System.out.println("6" + ex.getMessage()); //stampa messaggio di errore
            return null; //ritorna null in caso di errore
        }
    }

    /**
     * Metodo per creare un nuovo lettore nel database.
     *
     * @param nome     Nome del lettore.
     * @param cognome  Cognome del lettore.
     * @param username Username del lettore.
     * @param psw      Password del lettore.
     * @return ID del nuovo lettore creato, -1 in caso di errore.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int creaLettore(String nome, String cognome, String username, String psw) throws SQLException {
        String query = "INSERT into utenti (nome, cognome, username, psw, tipo)   VALUES(?, ?, ?, ?, ?)"; //query per creare un nuovo lettore
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome); //imposto nome
            pstatement.setString(2, cognome); //imposto cognome
            pstatement.setString(3, username); //imposto username
            pstatement.setString(4, psw); //imposto password
            pstatement.setString(5, "Lettore"); //imposto tipo
            pstatement.executeUpdate(); //eseguo la query
            ResultSet rs = pstatement.getGeneratedKeys(); //prendo le chiavi generate
            if (rs.next()) {
                return rs.getInt(1); //ritorno l'id del nuovo lettore creato
            }
            return -1; //ritorno -1 in caso di errore
        }
    }

    /**
     * Metodo per creare un nuovo genitore nel database.
     *
     * @param nome     Nome del genitore.
     * @param cognome  Cognome del genitore.
     * @param username Username del genitore.
     * @param psw      Password del genitore.
     * @return ID del nuovo genitore creato, -1 in caso di errore.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int creaGenitore(String nome, String cognome, String username, String psw) throws SQLException {
        String query = "INSERT into utenti (nome, cognome, username, psw, tipo)   VALUES(?, ?, ?, ?, ?)"; //query per creare un nuovo genitore
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome); //imposto nome
            pstatement.setString(2, cognome); //imposto cognome
            pstatement.setString(3, username); //imposto username
            pstatement.setString(4, psw); //imposto password
            pstatement.setString(5, "Genitore"); //imposto tipo
            pstatement.executeUpdate(); //eseguo la query
            ResultSet rs = pstatement.getGeneratedKeys(); //prendo le chiavi generate
            if (rs.next()) {
                return rs.getInt(1); //ritorno l'id del nuovo genitore creato
            }
            return -1; //ritorno -1 in caso di errore
        }
    }

    /**
     * Metodo per creare un nuovo amministratore nel database.
     *
     * @param nome     Nome dell'amministratore.
     * @param cognome  Cognome dell'amministratore.
     * @param username Username dell'amministratore.
     * @param psw      Password dell'amministratore.
     * @return ID del nuovo amministratore creato, -1 in caso di errore.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int creaAmministratore(String nome, String cognome, String username, String psw) throws SQLException {
        String query = "INSERT into utenti (nome, cognome, username, psw, tipo)   VALUES(?, ?, ?, ?, ?)"; //query per creare un nuovo amministratore
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome); //imposto nome
            pstatement.setString(2, cognome); //imposto cognome
            pstatement.setString(3, username); //imposto username
            pstatement.setString(4, psw); //imposto password
            pstatement.setString(5, "Amministratore"); //imposto tipo
            pstatement.executeUpdate(); //eseguo la query
            ResultSet rs = pstatement.getGeneratedKeys(); //prendo le chiavi generate
            if (rs.next()) {
                return rs.getInt(1); //ritorno l'id del nuovo amministratore creato
            }
            return -1; //ritorno -1 in caso di errore
        }
    }

    /**
     * Metodo per verificare se esiste un utente con uno specifico username.
     *
     * @param username Username da verificare.
     * @return ID dell'utente se esiste, -1 altrimenti.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int checkUserName(String username) throws SQLException {
        String query = "SELECT * FROM utenti WHERE username = ?"; //query per verificare se esiste un utente con questo username
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, username); //imposto username
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con questo username
                    return -1; //ritorno -1
                else {
                    result.next(); //sposto il cursore al primo risultato
                    return result.getInt("id"); //ritorno l'id dell'utente
                }
            } catch (SQLException ex) {
                System.out.println("Errore Query Check Username" + ex.getMessage()); //stampa messaggio di errore
                return -1; //ritorna -1 in caso di errore
            }
        } catch (SQLException ex) {
            System.out.println("Errore Query Check Username" + ex.getMessage()); //stampa messaggio di errore
            return -1; //ritorna -1 in caso di errore
        }
    }

    /**
     * Metodo per cancellare un genitore dal database.
     * Prima di cancellare il genitore, vengono cancellati tutti i suoi figli.
     *
     * @param genitore Genitore da cancellare.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public void cancellaGenitore(Genitore genitore) throws SQLException {
        //cancella figli di genitore
        FiglioDAO figlioDAO = new FiglioDAO(connection); //creo il DAO dei figli
        figlioDAO.cancellaFigli(genitore); //cancello i figli del genitore
        //cancella iscrizioni lettura
        String query = "DELETE FROM utenti WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, genitore.getId()); //imposto id del genitore
            pstatement.executeUpdate(); //eseguo l'update
        }
    }

    /**
     * Metodo per cancellare un lettore dal database.
     *
     * @param lettore Lettore da cancellare.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public void cancellaLettore(Lettore lettore) throws SQLException {
        //cancella iscrizioni lettura
        String query = "DELETE FROM utenti WHERE id = ?"; //query per cancellare il lettore
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, lettore.getId()); //imposto id del lettore
            pstatement.executeUpdate(); //eseguo l'update
        }
    }


    public void cancellaAmministratore(Amministratore amministratore) throws SQLException {
        String query = "DELETE FROM utenti WHERE id = ?"; //query per cancellare l'amministratore
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, amministratore.getId()); //imposto id dell'amministratore
            pstatement.executeUpdate(); //eseguo l'update
        }
    }

    /**
     * Metodo per modificare i dati (nome e cognome) di un genitore nel database.
     *
     * @param genitore Genitore da modificare.
     * @return Numero di righe modificate.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int aggiornaGenitore(Genitore genitore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?"; //query per aggiornare i dati del genitore
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, genitore.getNome()); //imposto nuovo nome
            pstatement.setString(2, genitore.getCognome()); //imposto nuovo cognome
            pstatement.setInt(3, genitore.getId()); //imposto id del genitore
            return pstatement.executeUpdate(); //eseguo l'update e ritorno il numero di righe modificate
        }
    }

    /**
     * Metodo per modificare i dati (nome e cognome) di un lettore nel database.
     *
     * @param lettore Lettore da modificare.
     * @return Numero di righe modificate.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int aggiornaLettore(Lettore lettore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?"; //query per aggiornare i dati del lettore
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, lettore.getNome()); //imposto nuovo nome
            pstatement.setString(2, lettore.getCognome()); //imposto nuovo cognome
            pstatement.setInt(3, lettore.getId()); //imposto id del lettore
            return pstatement.executeUpdate(); //eseguo l'update e ritorno il numero di righe modificate
        }
    }

    /**
     * Metodo per modificare i dati (nome e cognome) di un amministratore nel database.
     *
     * @param amministratore Amministratore da modificare.
     * @return Numero di righe modificate.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int aggiornaAmministratore(Amministratore amministratore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?"; //query per aggiornare i dati dell'amministratore
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, amministratore.getNome()); //imposto nuovo nome
            pstatement.setString(2, amministratore.getCognome()); //imposto nuovo cognome
            pstatement.setInt(3, amministratore.getId()); //imposto id dell'amministratore
            return pstatement.executeUpdate(); //eseguo l'update e ritorno il numero di righe modificate
        }
    }

    /**
     * Metodo per ottenere tutti i lettori presenti nel database.
     *
     * @return ArrayList di Lettore presenti nel database, null in caso di errore.
     */
    public ArrayList<Lettore> getLettori() {
        String query = "SELECT * FROM utenti WHERE tipo = 'Lettore'"; //query per recuperare la lista di tutti i lettori
        ArrayList<Lettore> lettori = new ArrayList<>(); //lista vuota di lettori
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esistono lettori
                    return lettori; //ritorno lista vuota
                else {
                    //scorro tutti i risultati
                    while (result.next()) {
                        CreaUtente<Lettore> creaLettore = new CreaLettore(); //creo il factory per i lettori
                        Lettore lettore = creaLettore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username")); //creo il lettore
                        lettori.add(lettore); //aggiungo il lettore alla lista
                    }
                    return lettori; //ritorno la lista di lettori
                }
            } catch (SQLException ex) {
                System.out.println("Errore Query Get Lettori" + ex.getMessage()); //stampa messaggio di errore
                return null; //ritorna null in caso di errore
            }
        } catch (SQLException ex) {
            System.out.println("Errore Query Get Lettori" + ex.getMessage());  //stampa messaggio di errore
            return null; //ritorna null in caso di errore
        }
    }
}
