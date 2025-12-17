package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.*;
import java.util.ArrayList;

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

    public void cancellaLettore(Lettore lettore) throws SQLException {
        //cancella iscrizioni lettura
        String query = "DELETE FROM utenti WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, lettore.getId());
            pstatement.executeUpdate();
        }
    }

    public int aggiornaGenitore(Genitore genitore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, genitore.getNome());
            pstatement.setString(2, genitore.getCognome());
            pstatement.setInt(3, genitore.getId());
            return pstatement.executeUpdate();
        }
    }

    public int aggiornaLettore(Lettore lettore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, lettore.getNome());
            pstatement.setString(2, lettore.getCognome());
            pstatement.setInt(3, lettore.getId());
            return pstatement.executeUpdate();
        }
    }

    public int aggiornaAmministratore(Amministratore amministratore) throws SQLException {
        String query = "UPDATE utenti SET nome = ? , cognome = ? WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, amministratore.getNome());
            pstatement.setString(2, amministratore.getCognome());
            pstatement.setInt(3, amministratore.getId());
            return pstatement.executeUpdate();
        }
    }

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
