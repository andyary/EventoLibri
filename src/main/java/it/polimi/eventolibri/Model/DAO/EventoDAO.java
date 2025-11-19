package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class EventoDAO {

    private Connection connection;

    public EventoDAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<Evento> getEventiCreati(Lettore lettore) throws SQLException {
        ArrayList<Evento> eventiCreati = new ArrayList<>();
        String query = "SELECT * FROM eventi e JOIN luoghi l ON e.id_luogo=l.id WHERE e.id_creatore = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, lettore.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return eventiCreati;
                else {
                    while (result.next()) {
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"),result.getInt("l.id"));
                        Evento evento = new Evento(result.getInt("e.id"), lettore, result.getString("e.nome"), luogo , result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")) );
                        eventiCreati.add(evento);
                    }
                    return eventiCreati;
                }
            } catch (SQLException ex) {
                System.out.println("8" + ex.getMessage());
                return eventiCreati;
            }

        } catch (SQLException ex) {
            System.out.println("9" + ex.getMessage());
            return eventiCreati;
        }
    }
}
