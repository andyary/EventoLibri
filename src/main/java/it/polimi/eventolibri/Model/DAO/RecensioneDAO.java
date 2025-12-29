package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.*;
import java.util.ArrayList;

public class RecensioneDAO {

    private Connection connection;
    public RecensioneDAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<Recensione> getRecensione(Libro libro) throws SQLException {
        ArrayList<Recensione> recensioni = new ArrayList<>();
        String query = "SELECT * FROM recensioni r JOIN utenti u ON r.id_genitore=u.id WHERE id_libro = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, libro.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return recensioni;
                else {
                    while (result.next()) {
                        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                        Genitore genitore = creaGenitore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        FiglioDAO figlioDAO = new FiglioDAO(connection);
                        genitore.setFigli(figlioDAO.getFigli(genitore));
                        recensioni.add(new Recensione(result.getInt("id"), genitore, result.getString("testo"), libro));
                    }
                    return recensioni;
                }
            }catch (SQLException ex) {
                System.out.println("305" + ex.getMessage());
                return recensioni;
            }
        }catch (SQLException ex) {
            System.out.println("306" + ex.getMessage());
            return recensioni;
        }
    }

    public int creaRecensione(String testo, Libro libro, Genitore genitore) throws SQLException {
        String query = "INSERT into recensioni (testo, id_libro, id_genitore)   VALUES(?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, testo);
            pstatement.setInt(2, libro.getId());
            pstatement.setInt(3, genitore.getId());
            pstatement.executeUpdate();
            ResultSet rs = pstatement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    public void cancellaRecensione(Libro libro, Genitore genitore) throws SQLException {
        String query = "DELETE FROM recensioni WHERE id_libro = ? AND id_genitore = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, libro.getId());
            pstatement.setInt(2, genitore.getId());
            pstatement.executeUpdate();
        }
    }

    public boolean getRecensibilita(Libro libro, Utente utente) {
        if (!(utente instanceof Genitore)) return false;
        Genitore genitore = (Genitore) utente;
        String query = "" +
                "SELECT * FROM iscrizioni i " +
                "JOIN eventi e ON i.id_evento = e.id " +
                "JOIN figli f ON i.id_figlio = f.id " +
                "JOIN utenti u ON f.id_genitore = u.id " +
                "JOIN librolettore ll ON e.id = ll.id_evento " +
                "WHERE u.id = ? AND ll.id_libro = ?  AND e.data < current_date()";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, genitore.getId());
            pstatement.setInt(2, libro.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (result.next())
                    return true;
                else {
                    return false;
                }
            }catch (SQLException ex) {
                System.out.println("Errore recensioneDao.getRecensibilità: " + ex.getMessage());
                return false;
            }
        }catch (SQLException ex) {
            System.out.println("Errore recensioneDao.getRecensibilità: " + ex.getMessage());
            return false;
        }
    }
}
