package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LibroLettoreDAO {
    private Connection connection;

    public LibroLettoreDAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<LibroLettore> getScaletta(int id_evento) throws SQLException {
        ArrayList<LibroLettore> scaletta = new ArrayList<>();
        String query = "SELECT * FROM librolettore ll JOIN libri l JOIN utenti u ON ll.id_libro=l.id AND ll.id_lettore=u.id WHERE id_evento = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_evento);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return scaletta;
                else {
                    while (!result.isAfterLast()) {
                        result.next();
                        CreaUtente creaLettore = new CreaLettore();
                        Utente lettore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Libro libro = new Libro(result.getString("l.titolo"), result.getInt("l.tempoLettura"), result.getString("l.link"), result.getString("l.autore"), result.getInt("l.id"));
                        LibroLettore libroLettore = new LibroLettore(libro, (Lettore) lettore, result.getInt("ll.progressivo"));
                        scaletta.add(libroLettore);
                    }
                    return scaletta;
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
                return scaletta;
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            return scaletta;
        }
    }

}
