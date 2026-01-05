package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** Classe DAO per la gestione degli eventi nel database.
 * Fornisce metodi per creare, modificare, cancellare e recuperare eventi.
 */
public class EventoDAO {

    private Connection connection;

    /** Costruttore della classe EventoDAO.
     * @param connection Connessione al database.
     */
    public EventoDAO(Connection connection) {
        this.connection = connection;
    }

    /** Restituisce la lista degli eventi futuri creati da un lettore.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene solo i listener associati al creatore e ai lettori iscritti. Mancano i listener dei genitori con figli iscritti all'evento.
     * @param lettore Lettore di cui si vogliono ottenere gli eventi creati.
     * @return Lista degli eventi creati dal lettore con data futura (rispetto alla data odierna).
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public ArrayList<Evento> getEventiCreati(Lettore lettore) throws SQLException {
        ArrayList<Evento> eventiCreati = new ArrayList<>();
        String query = "SELECT * FROM eventi e JOIN luoghi l ON e.id_luogo=l.id WHERE e.id_creatore = ? AND e.data >= NOW() ORDER BY e.data ASC";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, lettore.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return eventiCreati;
                else {
                    while (result.next()) {
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                        Evento evento = new Evento(result.getInt("e.id"), lettore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")));
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

    /** Restituisce la lista dei prossimi eventi a partire da una data specifica.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene solo i listener associati al creatore e ai lettori iscritti. Mancano i listener dei genitori con figli iscritti all'evento.
     * @param data Data di inizio per la ricerca degli eventi.
     * @return Lista dei prossimi 10 eventi a partire dalla data specificata.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
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
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore creatore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")));
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

    /** Restituisce la lista dei prossimi eventi a partire dalla data di un evento specifico.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene solo i listener associati al creatore e ai lettori iscritti. Mancano i listener dei genitori con figli iscritti all'evento.
     * @param eventoUltimoVisto Evento di riferimento per la ricerca dei prossimi eventi.
     * @return Lista dei prossimi 10 eventi successivi all'evento specificato.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
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
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore creatore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")));
                        nextEventi.add(evento);
                    }
                    return nextEventi;
                }
            } catch (SQLException ex) {
                System.out.println("8" + ex.getMessage());
                throw ex;
            }

        } catch (SQLException ex) {
            System.out.println("9" + ex.getMessage());
            throw ex;
        }
    }

    /** Restituisce un evento dato il suo ID.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene solo i listener associati al creatore e ai lettori iscritti. Mancano i listener dei genitori con figli iscritti all'evento.
     * @param id_evento ID dell'evento da recuperare.
     * @return Evento corrispondente all'ID specificato, o null se non trovato.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public Evento getEventoDaId(int id_evento) throws SQLException {
        String query = "SELECT * FROM eventi e JOIN luoghi l JOIN utenti u ON e.id_luogo=l.id AND e.id_creatore=u.id WHERE e.id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, id_evento);
            try (ResultSet result = pstatement.executeQuery();) {
                if (result.next()) {
                    LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                    Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                    CreaUtente<Lettore> creaLettore = new CreaLettore();
                    Lettore creatore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                    Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")));
                    return evento;
                } else {
                    return null;
                }
            } catch (SQLException ex) {
                System.out.println("18" + ex.getMessage());
                return null;
            }

        } catch (SQLException ex) {
            System.out.println("19" + ex.getMessage());
            return null;
        }
    }

    /** Crea un nuovo evento nel database.
     * @param creatore Lettore che crea l'evento.
     * @param nome Nome dell'evento.
     * @param luogo Luogo in cui si svolge l'evento.
     * @param data Data e ora dell'evento.
     * @return ID dell'evento appena creato.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public int creaEvento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data) throws SQLException {
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

    /** Modifica un evento esistente nel database.
     * @param evento Evento da modificare.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void modificaEvento(Evento evento) throws SQLException {
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

    /** Cancella un evento esistente nel database tramite id.
     * @param evento Evento da cancellare.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaEvento(Evento evento) throws SQLException {
        String query = "DELETE FROM eventi e WHERE e.id = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId());
            pstatement.executeUpdate();
        }
    }

    /** Restituisce la lista degli eventi in conflitto con un evento specifico.
     * Due eventi sono in conflitto se si svolgono nello stesso luogo e i loro orari si sovrappongono.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene solo i listener associati al creatore e ai lettori iscritti. Mancano i listener dei genitori con figli iscritti all'evento.
     * @param evento Evento di riferimento per la ricerca dei conflitti.
     * @return Lista degli eventi in conflitto con l'evento specificato.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public ArrayList<Evento> eventiInConflitto(Evento evento) throws SQLException {
        String query = "SELECT e.id, e.data AS dataInizio, SUM(l.tempoLettura) AS durataEvento, DATE_ADD(e.data, INTERVAL SUM(l.tempoLettura) MINUTE) AS dataFine " +
                "FROM eventi e JOIN libroLettore ll JOIN libri l " +
                "ON e.id=ll.id_evento AND ll.id_libro=l.id " +
                "WHERE e.id_luogo = ? AND e.id <> ? " +
                "GROUP BY e.id HAVING " +
                "((e.data <= ? AND DATE_ADD(e.data, INTERVAL SUM(l.tempoLettura) MINUTE) >= ?) OR " +
                "(e.data <= ? AND DATE_ADD(e.data, INTERVAL SUM(l.tempoLettura) MINUTE) >= ?) OR " +
                "(e.data >= ? AND DATE_ADD(e.data, INTERVAL SUM(l.tempoLettura) MINUTE) <= ?)) ";
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

    /** Restituisce il numero di figli iscritti ad un evento specifico.
     * @param evento Evento di cui si vuole conoscere il numero di iscritti.
     * @return Numero di figli iscritti all'evento specificato.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int getIscrittiEvento(Evento evento) throws SQLException {
        String query = "SELECT COUNT(*) AS numIscritti FROM iscrizioni WHERE id_evento = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (result.next()) {
                    int numIscritti = result.getInt("numIscritti");
                    return numIscritti;
                } else {
                    return 0;
                }
            } catch (SQLException ex) {
                System.out.println("Errore query iscritto evento:" + ex.getMessage());
                throw new SQLException();
            }
        } catch (SQLException ex) {
            System.out.println("Errore query iscritto evento2:" + ex.getMessage());
            throw new SQLException();
        }
    }

    /** Restituisce la lista dei genitori con figli iscritti ad un evento specifico.
     * Il genitore ritornato contiene solo le informazioni base (id, nome, cognome, username). Non sono caricati i figli del genitore.
     * @param evento Evento di cui si vogliono ottenere i genitori con figli iscritti.
     * @return Lista dei genitori con figli iscritti all'evento specificato.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public ArrayList<Genitore> getGenitoriIscritti(Evento evento) throws SQLException {
        ArrayList<Genitore> genitoriIscritti = new ArrayList<>();
        String query = "SELECT DISTINCT u.id, u.nome, u.cognome, u.username FROM iscrizioni i JOIN figli f JOIN utenti u ON i.id_figlio=f.id AND f.id_genitore=u.id WHERE i.id_evento = ?";
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId());
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati
                    return genitoriIscritti;
                else {
                    while (result.next()) {
                        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                        Genitore genitore = creaGenitore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        genitoriIscritti.add(genitore);
                    }
                    return genitoriIscritti;
                }
            } catch (SQLException ex) {
                System.out.println("Errore query genitori iscritti evento:" + ex.getMessage());
                throw new SQLException();
            }

        } catch (SQLException ex) {
            System.out.println("Errore query genitori iscritti evento2:" + ex.getMessage());
            throw new SQLException();
        }
    }

}
