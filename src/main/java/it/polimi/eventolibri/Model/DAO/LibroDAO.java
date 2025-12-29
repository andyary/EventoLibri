package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.Libro;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LibroDAO {

    private Connection connection;

    public LibroDAO(Connection connection) {
        this.connection = connection;
    }

    public Libro getLibro(int idLibro) throws SQLException {
        String query = "SELECT * FROM libri WHERE id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, idLibro);
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return null;
                else {
                    result.next();
                    Libro libro = new Libro(result.getString("titolo"), result.getInt("tempoLettura"), result.getString("link"), result.getString("autore"), result.getInt("id"), result.getString("isbn"));
                    RecensioneDAO recensioneDAO = new RecensioneDAO(connection);
                    libro.aggiungiRecensioni(recensioneDAO.getRecensione(libro));
                    return libro;
                }
            } catch (SQLException ex) {
                System.out.println("205" + ex.getMessage());
                return null;
            }
        } catch (SQLException ex) {
            System.out.println("206" + ex.getMessage());
            return null;
        }
    }

    public ArrayList<Libro> getLibri() {
        String query = "SELECT * FROM libri";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return null;
                else {
                    ArrayList<Libro> libri = new ArrayList<>();
                    while (result.next()) {
                        Libro libro = new Libro(
                                result.getString("titolo"),
                                result.getInt("tempoLettura"),
                                result.getString("link"),
                                result.getString("autore"),
                                result.getInt("id"),
                                result.getString("isbn")
                        );
                        RecensioneDAO recensioneDAO = new RecensioneDAO(connection);
                        libro.aggiungiRecensioni(recensioneDAO.getRecensione(libro));
                        libri.add(libro);
                    }
                    return libri;
                }
            } catch (SQLException ex) {
                System.out.println("Messaggio errore dalla query al DB:" + ex.getMessage());
                return null;
            }
        } catch (SQLException ex) {
            System.out.println("Messaggio errore dalla query al DB:" + ex.getMessage());
            return null;
        }
    }
}

