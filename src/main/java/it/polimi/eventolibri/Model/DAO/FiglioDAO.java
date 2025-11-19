package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class FiglioDAO {

    private Connection connection;

    public FiglioDAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<Figlio> getFigli(Genitore genitore) throws SQLException {
        ArrayList<Figlio> figli = new ArrayList<>();
        String query = "SELECT * FROM figli WHERE id_genitore = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, genitore.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return figli;
                else {
                    while (result.next()) {
                        Figlio figlio = new Figlio(result.getInt("id"), result.getString("nome"), result.getDate("dataNascita").toLocalDate(), getIscrizioni(result.getInt("id")), genitore);
                        figli.add(figlio);
                    }
                    return figli;
                }
            } catch (SQLException ex) {
                System.out.println("1" + ex.getMessage());
                return figli;
            }

        } catch (SQLException ex) {
            System.out.println("2" + ex.getMessage());
            return figli;
        }
    }


    public ArrayList<Evento> getIscrizioni(int id_figlio) throws SQLException {
        ArrayList<Evento> iscrizioni = new ArrayList<>();
        String query = "SELECT * FROM iscrizioni i JOIN eventi e JOIN utenti u JOIN luoghi l ON i.id_evento=e.id AND e.id_creatore=u.id AND l.id=e.id_luogo WHERE i.id_figlio = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_figlio);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return iscrizioni;
                else {
                    while (result.next()) {
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore lettore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"),result.getInt("l.id"));
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                        ArrayList<LibroLettore> scaletta = libroLettoreDAO.getScaletta(result.getInt("e.id"));
                        iscrizioni.add(new Evento(result.getInt("e.id"), lettore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), scaletta)) ;
                    }
                    return iscrizioni;
                }
            } catch (SQLException ex) {
                System.out.println("3" + ex.getMessage());
                return iscrizioni;
            }

        } catch (SQLException ex) {
            System.out.println("4" + ex.getMessage());
            return iscrizioni;
        }
    }
}
