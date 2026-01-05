package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;
import java.sql.*;
import java.util.ArrayList;

/** Classe DAO per la gestione degli utenti nel database.
 */
public class UtenteDAO {

    /** Attributo: Connessione al database.*/
    private Connection connection;

    /** Costruttore della classe UtenteDAO.
     * @param connection Connessione al database.
     */
    public UtenteDAO(Connection connection) {
        this.connection = connection;
    }

    /** Metodo per verificare le credenziali di un utente, usato in fase di login.
     * In base al tipo di utente, viene creato l'oggetto corrispondente.
     * @param username Username dell'utente.
     * @param psw Password dell'utente.
     * @return Utente se le credenziali sono corrette, null altrimenti.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public Utente checkCredentials(String username, String psw) throws SQLException {
        String query = "SELECT * FROM utenti WHERE username = ? AND psw = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, username);
            pstatement.setString(2, psw);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return null;
                else {
                    result.next();
                    switch (
                            result.getString("tipo")){
                        case "Amministratore":
                            CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore();
                            Amministratore amministratore = creaAmministratore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username"));
                            return amministratore;
                        case "Lettore":
                            CreaUtente<Lettore> creaLettore = new CreaLettore();
                            Lettore lettore = creaLettore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username"));
                            lettore.setEventiCreati(new EventoDAO(connection).getEventiCreati(lettore));
                            lettore.setIscrizioniLettura(new LibroLettoreDAO(connection).getEventiLettura(lettore));
                            return lettore;
                        case "Genitore":
                            CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                            Genitore genitore = creaGenitore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username"));
                            FiglioDAO figlioDAO = new FiglioDAO(connection);
                            genitore.setFigli(figlioDAO.getFigli(genitore));
                            return genitore;
                        default:
                            return null;
                    }
                }

            } catch (SQLException ex) {
                System.out.println("5" + ex.getMessage());
                return null;
            }
        } catch (SQLException ex) {
            System.out.println("6" + ex.getMessage());
            return null;
        }
    }

    /** Metodo per creare un nuovo lettore nel database.
     * @param nome Nome del lettore.
     * @param cognome Cognome del lettore.
     * @param username Username del lettore.
     * @param psw Password del lettore.
     * @return ID del nuovo lettore creato, -1 in caso di errore.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int creaLettore(String nome, String cognome, String username, String psw) throws SQLException {
        String query = "INSERT into utenti (nome, cognome, username, psw, tipo)   VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome);
            pstatement.setString(2, cognome);
            pstatement.setString(3, username);
            pstatement.setString(4, psw);
            pstatement.setString(5, "Lettore");
            pstatement.executeUpdate();
            ResultSet rs = pstatement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    /** Metodo per creare un nuovo genitore nel database.
     * @param nome Nome del genitore.
     * @param cognome Cognome del genitore.
     * @param username Username del genitore.
     * @param psw Password del genitore.
     * @return ID del nuovo genitore creato, -1 in caso di errore.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int creaGenitore(String nome, String cognome, String username, String psw) throws SQLException {
        String query = "INSERT into utenti (nome, cognome, username, psw, tipo)   VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome);
            pstatement.setString(2, cognome);
            pstatement.setString(3, username);
            pstatement.setString(4, psw);
            pstatement.setString(5, "Genitore");
            pstatement.executeUpdate();
            ResultSet rs = pstatement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    /** Metodo per creare un nuovo amministratore nel database.
     * @param nome Nome dell'amministratore.
     * @param cognome Cognome dell'amministratore.
     * @param username Username dell'amministratore.
     * @param psw Password dell'amministratore.
     * @return ID del nuovo amministratore creato, -1 in caso di errore.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int creaAmministratore(String nome, String cognome, String username, String psw) throws SQLException {
        String query = "INSERT into utenti (nome, cognome, username, psw, tipo)   VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome);
            pstatement.setString(2, cognome);
            pstatement.setString(3, username);
            pstatement.setString(4, psw);
            pstatement.setString(5, "Amministratore");
            pstatement.executeUpdate();
            ResultSet rs = pstatement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    /** Metodo per verificare se esiste un utente con uno specifico username.
     * @param username Username da verificare.
     * @return ID dell'utente se esiste, -1 altrimenti.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int checkUserName (String username) throws SQLException {
        String query = "SELECT * FROM utenti WHERE username = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, username);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con questo username
                    return -1;
                else {
                    result.next();
                    return result.getInt("id");
                }

            } catch (SQLException ex) {
                System.out.println("Errore Query Check Username" + ex.getMessage());
                return -1;
            }
        } catch (SQLException ex) {
            System.out.println("Errore Query Check Username" + ex.getMessage());
            return -1;
        }
    }

    /** Metodo per cancellare un genitore dal database.
     * Prima di cancellare il genitore, vengono cancellati tutti i suoi figli.
     * @param genitore Genitore da cancellare.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public void cancellaGenitore(Genitore genitore) throws SQLException {
        //cancella figli di genitore
        FiglioDAO figlioDAO = new FiglioDAO(connection);
        figlioDAO.cancellaFigli(genitore);
        String query = "DELETE FROM utenti WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, genitore.getId());
            pstatement.executeUpdate();
        }
    }

    /** Metodo per cancellare un lettore dal database.
     * @param lettore Lettore da cancellare.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public void cancellaLettore(Lettore lettore) throws SQLException {
        //cancella iscrizioni lettura
        String query = "DELETE FROM utenti WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, lettore.getId());
            pstatement.executeUpdate();
        }
    }

    /** Metodo per modificare i dati (nome e cognome) di un genitore nel database.
     * @param genitore Genitore da modificare.
     * @return Numero di righe modificate.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int aggiornaGenitore(Genitore genitore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, genitore.getNome());
            pstatement.setString(2, genitore.getCognome());
            pstatement.setInt(3, genitore.getId());
            return pstatement.executeUpdate();
        }
    }

    /** Metodo per modificare i dati (nome e cognome) di un lettore nel database.
     * @param lettore Lettore da modificare.
     * @return Numero di righe modificate.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int aggiornaLettore(Lettore lettore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, lettore.getNome());
            pstatement.setString(2, lettore.getCognome());
            pstatement.setInt(3, lettore.getId());
            return pstatement.executeUpdate();
        }
    }

    /** Metodo per modificare i dati (nome e cognome) di un amministratore nel database.
     * @param amministratore Amministratore da modificare.
     * @return Numero di righe modificate.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int aggiornaAmministratore(Amministratore amministratore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, amministratore.getNome());
            pstatement.setString(2, amministratore.getCognome());
            pstatement.setInt(3, amministratore.getId());
            return pstatement.executeUpdate();
        }
    }

    /** Metodo per ottenere tutti i lettori presenti nel database.
     * @return ArrayList di Lettore presenti nel database, null in caso di errore.
     */
    public ArrayList<Lettore> getLettori() {
        String query = "SELECT * FROM utenti WHERE tipo = 'Lettore'";
        ArrayList<Lettore> lettori = new ArrayList<>();
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esistono lettori
                    return lettori;
                else {
                    while (result.next()) {
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore lettore = creaLettore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username"));
                        lettori.add(lettore);
                    }
                    return lettori;
                }
            } catch (SQLException ex) {
                System.out.println("Errore Query Get Lettori" + ex.getMessage());
                return null;
            }
        } catch (SQLException ex) {
            System.out.println("Errore Query Get Lettori" + ex.getMessage());
            return null;
        }
    }
}
