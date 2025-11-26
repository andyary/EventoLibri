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
        String query = "SELECT * FROM eventi e JOIN luoghi l JOIN utenti u ON e.id_luogo=l.id AND e.id_creatore=u.id WHERE e.id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_evento);
            try (ResultSet result = pstatement.executeQuery();) {
                if (result.next()) {
                    LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                    Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                    Lettore creatore = new CreaLettore().nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                    Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")));
                    return evento;
                } else {
                    return null;
                }
            }catch (SQLException ex) {
                System.out.println("18" + ex.getMessage());
                return null;
            }

        } catch (SQLException ex) {
            System.out.println("19" + ex.getMessage());
            return null;
        }
    }

    public int creaEvento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data) throws SQLException {

        // aggiungere controllo in base alla ora di inizio e al luogo scelto

        String query = "INSERT into eventi (nome, data, id_creatore, id_luogo)   VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
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

        // aggiungere controllo in base alla ora di inizio e al luogo scelto

        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
        libroLettoreDAO.cancellaScaletta(evento);
        libroLettoreDAO.creaScaletta(evento);
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

    public ArrayList<Evento> eventiInConflitto(Evento evento) throws SQLException {
        String query = "SELECT e.id, e.data AS dataInizio, SUM(l.tempoLettura) AS durataEvento, DATE_ADD(e.data, INTERVAL SUM(l.tempoLettura) MINUTE)  AS dataFine " +
                "FROM eventi e JOIN libroLettore ll JOIN libri l " +
                "ON e.id=ll.id_evento AND ll.id_libro=l.id " +
                "WHERE e.id_luogo = ? AND e.id <> ? AND " +
                "(datainizio < ? AND dataFine > ?) AND " +
                "(datainizio < ? AND dataFine > ?) AND " +
                "(datainizio > ? AND dataFine < ?) " +
                "GROUP BY e.id";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getLuogo().getId());
            pstatement.setInt(2, evento.getId());
            pstatement.setTimestamp(3, Timestamp.valueOf(evento.getData()));
            pstatement.setTimestamp(4, Timestamp.valueOf(evento.getData()));
            pstatement.setTimestamp(5, Timestamp.valueOf(evento.calcolaOraFine()));
            pstatement.setTimestamp(6, Timestamp.valueOf(evento.calcolaOraFine()));
            pstatement.setTimestamp(7, Timestamp.valueOf(evento.getData()));
            pstatement.setTimestamp(8, Timestamp.valueOf(evento.calcolaOraFine()));
            try (ResultSet result = pstatement.executeQuery();) {
                ArrayList<Evento> eventiConflitto = new ArrayList<>();
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return eventiConflitto;
                else {
                    while (result.next()) {
                        Evento eventoInConflitto = getEventoDaId(result.getInt("e.id"));
                        eventiConflitto.add(eventoInConflitto);
                    }
                    return eventiConflitto;
                }
            } catch (SQLException ex) {
                System.out.println("29" + ex.getMessage());
                return null;
            }
        }
    }


}
