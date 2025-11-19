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
}
