package it.polimi.eventolibri.Model.DAO;

import it.polimi.eventolibri.Model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Classe DAO per la gestione degli eventi nel database.
 * Fornisce metodi per creare, modificare, cancellare e recuperare eventi.
 */
public class EventoDAO {

    private Connection connection;

    /**
     * Costruttore della classe EventoDAO.
     *
     * @param connection Connessione al database.
     */
    public EventoDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Restituisce la lista degli eventi futuri creati da un lettore.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene i listener associati al creatore e ai lettori iscritti e ai genitori con figli iscritti all'evento.
     *
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
                        // Creazione dell'evento
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                        Evento evento = new Evento(result.getInt("e.id"), lettore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")));
                        EventoDAO eventoDAO = new EventoDAO(connection);
                        evento.addListenersGenitori(eventoDAO.getGenitoriIscritti(evento));
                        evento.setIscritti(eventoDAO.getIscrittiEvento(evento));
                        eventiCreati.add(evento); // Aggiunta dell'evento alla lista
                    }
                    return eventiCreati; // Restituzione della lista degli eventi creati
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

    /**
     * Restituisce la lista dei prossimi eventi a partire da una data specifica.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene i listener associati al creatore e ai lettori iscritti e ai genitori con figli iscritti all'evento.
     *
     * @param data Data di inizio per la ricerca degli eventi.
     * @return Lista dei prossimi 10 eventi a partire dalla data specificata.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public ArrayList<Evento> getNextEventi(LocalDateTime data) throws SQLException {
        ArrayList<Evento> nextEventi = new ArrayList<>(); // Lista per memorizzare i prossimi eventi
        String query = "SELECT * FROM eventi e JOIN luoghi l JOIN utenti u " +
                "ON e.id_luogo=l.id AND e.id_creatore=u.id WHERE e.data >= ? " +
                "ORDER BY e.data ASC, e.id ASC LIMIT 10"; // Query per ottenere i prossimi 10 eventi
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setTimestamp(1, Timestamp.valueOf(data)); // Imposta la data di inizio
            try (ResultSet result = pstatement.executeQuery();) {
                // Esegui la query e ottieni i risultati
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return nextEventi;
                else {
                    // Itera sui risultati della query
                    while (result.next()) {
                        // Creazione dell'evento
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection); // DAO per gestire i libri associati all'evento
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id")); // Creazione del luogo
                        CreaUtente<Lettore> creaLettore = new CreaLettore(); // Factory per creare l'utente lettore
                        Lettore creatore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username")); // Creazione del creatore dell'evento
                        Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id"))); // Creazione dell'evento
                        EventoDAO eventoDAO = new EventoDAO(connection); // DAO per gestire gli eventi
                        evento.addListenersGenitori(eventoDAO.getGenitoriIscritti(evento)); // Aggiunta dei genitori con figli iscritti come listener
                        evento.setIscritti(eventoDAO.getIscrittiEvento(evento)); // Impostazione del numero di iscritti all'evento
                        nextEventi.add(evento); // Aggiunta dell'evento alla lista
                    }
                    return nextEventi; // Restituzione della lista dei prossimi eventi
                }
            } catch (SQLException ex) {
                System.out.println("8" + ex.getMessage()); // Stampa dell'errore
                return nextEventi; // Restituzione della lista (potrebbe essere vuota in caso di errore)
            }

        } catch (SQLException ex) {
            System.out.println("9" + ex.getMessage()); // Stampa dell'errore
            return nextEventi; // Restituzione della lista (potrebbe essere vuota in caso di errore)
        }
    }

    /**
     * Restituisce la lista dei prossimi eventi a partire dalla data di un evento specifico.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene i listener associati al creatore e ai lettori iscritti e ai genitori con figli iscritti all'evento.
     *
     * @param eventoUltimoVisto Evento di riferimento per la ricerca dei prossimi eventi.
     * @return Lista dei prossimi 10 eventi successivi all'evento specificato.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public ArrayList<Evento> getNextEventi(Evento eventoUltimoVisto) throws SQLException {
        ArrayList<Evento> nextEventi = new ArrayList<>();
        String query = "SELECT * FROM eventi e JOIN luoghi l JOIN utenti u ON e.id_luogo=l.id AND e.id_creatore=u.id " +
                "WHERE (e.data > ?) OR ((e.data = ?) AND (e.id > ?)) " +
                "ORDER BY e.data ASC, e.id ASC LIMIT 10 "; // Query per ottenere i prossimi 10 eventi
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setTimestamp(1, Timestamp.valueOf(eventoUltimoVisto.getData())); // Imposta la data di inizio
            pstatement.setTimestamp(2, Timestamp.valueOf(eventoUltimoVisto.getData())); // Imposta la data di inizio
            pstatement.setInt(3, eventoUltimoVisto.getId()); // Imposta l'ID dell'ultimo evento visto
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati, non esiste utente con queste credenziali
                    return nextEventi; // Restituzione della lista (vuota)
                else {
                    // Itera sui risultati della query
                    while (result.next()) {
                        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
                        Luogo luogo = new Luogo(result.getString("l.nome"), result.getInt("l.capienza"), result.getInt("l.id"));
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore creatore = creaLettore.nuovoUtente(result.getInt("u.id"), result.getString("u.nome"), result.getString("u.cognome"), result.getString("u.username"));
                        Evento evento = new Evento(result.getInt("e.id"), creatore, result.getString("e.nome"), luogo, result.getTimestamp("e.data").toLocalDateTime(), libroLettoreDAO.getScaletta(result.getInt("e.id")));
                        EventoDAO eventoDAO = new EventoDAO(connection);
                        evento.addListenersGenitori(eventoDAO.getGenitoriIscritti(evento));
                        evento.setIscritti(eventoDAO.getIscrittiEvento(evento));
                        nextEventi.add(evento); // Aggiunta dell'evento alla lista
                    }
                    return nextEventi; // Restituzione della lista dei prossimi eventi
                }
            } catch (SQLException ex) {
                System.out.println("8" + ex.getMessage()); // Stampa dell'errore
                throw ex; // Rilancio dell'eccezione
            }

        } catch (SQLException ex) {
            System.out.println("9" + ex.getMessage()); // Stampa dell'errore
            throw ex; // Rilancio dell'eccezione
        }
    }

    /**
     * Restituisce un evento dato il suo ID.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene i listener associati al creatore e ai lettori iscritti e ai genitori con figli iscritti all'evento.
     *
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
                    EventoDAO eventoDAO = new EventoDAO(connection);
                    evento.addListenersGenitori(eventoDAO.getGenitoriIscritti(evento));
                    evento.setIscritti(eventoDAO.getIscrittiEvento(evento));
                    return evento;
                } else {
                    return null;
                }
            } catch (SQLException ex) {
                System.out.println("18" + ex.getMessage()); // Stampa dell'errore
                return null; // Ristituzione di null in caso di errore
            }

        } catch (SQLException ex) {
            System.out.println("19" + ex.getMessage()); // Stampa dell'errore
            return null; // Ristituzione di null in caso di errore
        }
    }

    /**
     * Crea un nuovo evento nel database.
     *
     * @param creatore Lettore che crea l'evento.
     * @param nome     Nome dell'evento.
     * @param luogo    Luogo in cui si svolge l'evento.
     * @param data     Data e ora dell'evento.
     * @return ID dell'evento appena creato.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public int creaEvento(Lettore creatore, String nome, Luogo luogo, LocalDateTime data) throws SQLException {
        String query = "INSERT into eventi (nome, data, id_creatore, id_luogo)   VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            pstatement.setString(1, nome); // Imposta il nome dell'evento
            pstatement.setTimestamp(2, Timestamp.valueOf(data)); // Imposta la data e l'ora dell'evento
            pstatement.setInt(3, creatore.getId()); // Imposta il creatore dell'evento
            pstatement.setInt(4, luogo.getId()); // Imposta il luogo dell'evento
            pstatement.executeUpdate(); // Esegui l'inserimento
            ResultSet rs = pstatement.getGeneratedKeys(); // Recupera le chiavi generate
            if (rs.next()) {
                return rs.getInt(1); // Restituisce l'ID generato dell'evento appena creato
            }
            return -1; // In caso di errore nel recupero dell'ID generato
        }
    }

    /**
     * Modifica un evento esistente nel database.
     *
     * @param evento Evento da modificare.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void modificaEvento(Evento evento) throws SQLException {
        LibroLettoreDAO libroLettoreDAO = new LibroLettoreDAO(connection);
        libroLettoreDAO.cancellaScaletta(evento); // Cancella la vecchia scaletta
        libroLettoreDAO.creaScaletta(evento); // Crea la nuova scaletta
        String query = "UPDATE eventi e SET e.nome = ?, e.data = ?, e.id_luogo = ? WHERE e.id = ?"; // Query di aggiornamento dell'evento
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setString(1, evento.getNome()); // Imposta il nuovo nome dell'evento
            pstatement.setTimestamp(2, Timestamp.valueOf(evento.getData())); // Imposta la nuova data e ora dell'evento
            pstatement.setInt(3, evento.getLuogo().getId()); // Imposta il nuovo luogo dell'evento
            pstatement.setInt(4, evento.getId()); // Imposta l'ID dell'evento da modificare
            pstatement.executeUpdate(); // Esegui l'aggiornamento
        }
    }

    /**
     * Cancella un evento esistente nel database tramite id.
     *
     * @param evento Evento da cancellare.
     * @throws SQLException Se si verifica un errore durante l'accesso al database.
     */
    public void cancellaEvento(Evento evento) throws SQLException {
        String query = "DELETE FROM eventi e WHERE e.id = ?"; // Query di cancellazione dell'evento
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId()); // Imposta l'ID dell'evento da cancellare
            pstatement.executeUpdate(); // Esegui la cancellazione
        }
    }

    /**
     * Restituisce la lista degli eventi in conflitto con un evento specifico.
     * Due eventi sono in conflitto se si svolgono nello stesso luogo e i loro orari si sovrappongono.
     * L'Evento contiene info circa creatore, luogo e scaletta.
     * Contiene solo i listener associati al creatore e ai lettori iscritti. Mancano i listener dei genitori con figli iscritti all'evento.
     *
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
                "(e.data >= ? AND DATE_ADD(e.data, INTERVAL SUM(l.tempoLettura) MINUTE) <= ?)) "; // Query per trovare eventi in conflitto
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getLuogo().getId()); // Imposta l'ID del luogo dell'evento
            pstatement.setInt(2, evento.getId()); // Imposta l'ID dell'evento di riferimento
            pstatement.setTimestamp(3, Timestamp.valueOf(evento.getData())); // Imposta l'inizio dell'evento di riferimento
            pstatement.setTimestamp(4, Timestamp.valueOf(evento.getData())); // Imposta l'inizio dell'evento di riferimento
            pstatement.setTimestamp(5, Timestamp.valueOf(evento.calcolaOraFine())); // Imposta la fine dell'evento di riferimento
            pstatement.setTimestamp(6, Timestamp.valueOf(evento.calcolaOraFine())); // Imposta la fine dell'evento di riferimento
            pstatement.setTimestamp(7, Timestamp.valueOf(evento.getData())); // Imposta l'inizio dell'evento di riferimento
            pstatement.setTimestamp(8, Timestamp.valueOf(evento.calcolaOraFine())); // Imposta la fine dell'evento di riferimento
            try (ResultSet result = pstatement.executeQuery();) {
                ArrayList<Evento> eventiConflitto = new ArrayList<>(); // Lista per memorizzare gli eventi in conflitto
                if (!result.isBeforeFirst()) // no risultati, non esiste evento in conflitto
                    return eventiConflitto; // Restituzione della lista (vuota)
                else {
                    while (result.next()) {
                        Evento eventoInConflitto = getEventoDaId(result.getInt("e.id")); // Recupera l'evento in conflitto
                        eventiConflitto.add(eventoInConflitto); // Aggiunta dell'evento in conflitto alla lista
                    }
                    return eventiConflitto; // Restituzione della lista degli eventi in conflitto
                }
            } catch (SQLException ex) {
                System.out.println("29" + ex.getMessage()); // Stampa dell'errore
                return null; // Ristituzione di null in caso di errore
            }
        }
    }

    /**
     * Restituisce il numero di figli iscritti ad un evento specifico.
     *
     * @param evento Evento di cui si vuole conoscere il numero di iscritti.
     * @return Numero di figli iscritti all'evento specificato.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public int getIscrittiEvento(Evento evento) throws SQLException {
        String query = "SELECT COUNT(*) AS numIscritti FROM iscrizioni WHERE id_evento = ?"; // Query per contare il numero di iscritti all'evento
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId()); // Imposta l'ID dell'evento
            try (ResultSet result = pstatement.executeQuery();) {
                if (result.next()) {
                    int numIscritti = result.getInt("numIscritti"); // Recupera il numero di iscritti
                    return numIscritti; // Restituisce il numero di iscritti
                } else {
                    return 0; // Nessun iscritto trovato
                }
            } catch (SQLException ex) {
                System.out.println("Errore query iscritto evento:" + ex.getMessage()); // Stampa dell'errore
                throw new SQLException(); // Rilancio dell'eccezione
            }
        } catch (SQLException ex) {
            System.out.println("Errore query iscritto evento2:" + ex.getMessage()); // Stampa dell'errore
            throw new SQLException(); // Rilancio dell'eccezione
        }
    }

    /**
     * Restituisce la lista dei genitori con figli iscritti ad un evento specifico.
     * Il genitore ritornato contiene solo le informazioni base (id, nome, cognome, username). Non sono caricati i figli del genitore.
     *
     * @param evento Evento di cui si vogliono ottenere i genitori con figli iscritti.
     * @return Lista dei genitori con figli iscritti all'evento specificato.
     * @throws SQLException Se si verifica un errore durante l'esecuzione della query.
     */
    public ArrayList<Genitore> getGenitoriIscritti(Evento evento) throws SQLException {
        ArrayList<Genitore> genitoriIscritti = new ArrayList<>();
        String query = "SELECT DISTINCT u.id, u.nome, u.cognome, u.username FROM iscrizioni i JOIN figli f JOIN utenti u ON i.id_figlio=f.id AND f.id_genitore=u.id WHERE i.id_evento = ?"; // Query per ottenere i genitori con figli iscritti all'evento
        try (PreparedStatement pstatement = connection.prepareStatement(query);) {
            pstatement.setInt(1, evento.getId()); // Imposta l'ID dell'evento
            try (ResultSet result = pstatement.executeQuery();) {
                if (!result.isBeforeFirst()) // no risultati
                    return genitoriIscritti; // Restituzione della lista (vuota)
                else {
                    while (result.next()) {
                        CreaUtente<Genitore> creaGenitore = new CreaGenitore(); // Factory per creare l'utente genitore
                        Genitore genitore = creaGenitore.nuovoUtente(result.getInt("u.id"),
                                result.getString("u.nome"), result.getString("u.cognome"),
                                result.getString("u.username")); // Creazione del genitore
                        genitoriIscritti.add(genitore); // Aggiunta del genitore alla lista
                    }
                    return genitoriIscritti; // Restituzione della lista dei genitori con figli iscritti
                }
            } catch (SQLException ex) {
                System.out.println("Errore query genitori iscritti evento:" + ex.getMessage()); // Stampa dell'errore
                throw new SQLException(); // Rilancio dell'eccezione
            }
        } catch (SQLException ex) {
            System.out.println("Errore query genitori iscritti evento2:" + ex.getMessage()); // Stampa dell'errore
            throw new SQLException(); // Rilancio dell'eccezione
        }
    }
}
