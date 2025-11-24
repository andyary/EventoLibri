package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

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
                    result.next();
                    CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                    Genitore genitore = creaGenitore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                    FiglioDAO figlioDAO = new FiglioDAO(connection);
                    genitore.setFigli(figlioDAO.getFigli(genitore));
                    recensioni.add(new Recensione(result.getInt("id"), genitore, result.getString("testo"), libro));
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

}
