package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UtenteDAO {

    private Connection connection;

    public UtenteDAO(Connection connection) {
        this.connection = connection;
    }

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
                            CreaUtente creaAmministratore = new CreaAmministratore();
                            Utente amministratore = creaAmministratore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username"));
                            return amministratore;
                        case "Lettore":
                            CreaUtente creaLettore = new CreaLettore();
                            Utente lettore = creaLettore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username"));
                            // manca caricamento eventi creati e di lettura
                            return lettore;
                        case "Genitore":
                            CreaUtente creaGenitore = new CreaGenitore();
                            Utente genitore = creaGenitore.nuovoUtente(result.getInt("id"), result.getString("nome"), result.getString("cognome"), result.getString("username"));
                            FiglioDAO figlioDAODao = new FiglioDAO(connection);
                            ((Genitore) genitore).setFigli(figlioDAODao.getFigli((Genitore) genitore));
                            return genitore;
                        default:
                            return null;
                    }
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                return null;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}
