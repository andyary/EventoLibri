package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.*;
import java.time.LocalDateTime;
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


    public ArrayList<Evento> getNextEventi(LocalDateTime data) throws SQLException {
        ArrayList<Evento> nextEventi = new ArrayList<>();
        String query = "SELECT * FROM eventi e JOIN luoghi l JOIN utenti u " +
                "ON e.id_luogo=l.id AND e.id_creatore=u.id WHERE e.data >= ? " +
                "ORDER BY e.data ASC, e.id ASC LIMIT 10";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setTimestamp(1, Timestamp.valueOf(data));
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return nextEventi;
                else {
                    while (result.next()) {
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"),result.getInt("l.id"));
                        Lettore creatore = new CreaLettore().nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo , result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")) );
                        nextEventi.add(evento);
                    }
                    return nextEventi;
                }
            } catch (SQLException ex) {
                System.out.println("8" + ex.getMessage());
                return nextEventi;
            }

        } catch (SQLException ex) {
            System.out.println("9" + ex.getMessage());
            return nextEventi;
        }
    }

    public ArrayList<Evento> getNextEventi(Evento eventoUltimoVisto) throws SQLException {
        ArrayList<Evento> nextEventi = new ArrayList<>();
        String query = "SELECT * FROM eventi e JOIN luoghi l JOIN utenti u ON e.id_luogo=l.id AND e.id_creatore=u.id " +
                "WHERE (e.data > ?) OR ((e.data = ?) AND (e.id > ?)) " +
                "ORDER BY e.data ASC, e.id ASC LIMIT 10 ";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setTimestamp(1, Timestamp.valueOf(eventoUltimoVisto.getData()));
            pstatement.setTimestamp(2, Timestamp.valueOf(eventoUltimoVisto.getData()));
            pstatement.setInt(3, eventoUltimoVisto.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return nextEventi;
                else {
                    while (result.next()) {
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"),result.getInt("l.id"));
                        Lettore creatore = new CreaLettore().nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo , result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")) );
                        nextEventi.add(evento);
                    }
                    return nextEventi;
                }
            } catch (SQLException ex) {
                System.out.println("8" + ex.getMessage());
                return nextEventi;
            }

        } catch (SQLException ex) {
            System.out.println("9" + ex.getMessage());
            return nextEventi;
        }
    }

    public Evento getEventoDaId(int id_evento) throws SQLException {
        Evento evento = null;
        String query = "SELECT * FROM eventi e JOIN luoghi l JOIN utenti u ON e.id_luogo=l.id AND e.id_creatore=u.id WHERE e.id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_evento);
            try (ResultSet result = pstatement.executeQuery();) {

                if (result.next()) {
                    LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                    Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                    Lettore creatore = new CreaLettore().nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                    Evento evento2 = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")));
                    return evento2;
                } else {
                    return evento;
                }
            }catch (SQLException ex) {
                System.out.println("18" + ex.getMessage());
                return evento;
            }

        } catch (SQLException ex) {
            System.out.println("19" + ex.getMessage());
            return evento;
        }
    }

    public int creaEvento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data) throws SQLException {
        String query = "INSERT into evento (nome, data. id_creatore, id_luogo)   VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, nome);
            pstatement.setTimestamp(2, Timestamp.valueOf(data));
            pstatement.setInt(3, creatore.getId());
            pstatement.setInt(4, luogo.getId());
            pstatement.executeUpdate();
            ResultSet rs = pstatement.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    public void modificaEvento(Evento evento) throws SQLException {
        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
        libroLettoreDAO.cancellaScaletta(evento);
        libroLettoreDAO.creaScaletta(evento, evento.getScaletta());
        String query = "UPDATE eventi e SET e.nome = ?, e.data = ?, e.id_luogo = ? WHERE e.id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, evento.getNome());
            pstatement.setTimestamp(2, Timestamp.valueOf(evento.getData()));
            pstatement.setInt(3, evento.getLuogo().getId());
            pstatement.setInt(4, evento.getId());
            pstatement.executeUpdate();
        }
    }

    public void cancellaEvento(Evento evento) throws SQLException {
        String query = "DELETE FROM eventi e WHERE e.id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId());
            pstatement.executeUpdate();
        }
    }


}
