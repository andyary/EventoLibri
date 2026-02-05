package it.polimi.eventolibri.Controller;

import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Model.DAO.*;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;

/** Classe Controller che gestisce la logica di business dell'applicazione.
 * Si occupa di interagire con i DAO per eseguire operazioni sul database
 * e di elaborare le richieste ricevute dai client.
 */
public class Controller {
    // DAO per l'accesso ai dati
    private UtenteDAO utenteDAO;
    private EventoDAO eventoDAO;
    private FiglioDAO figlioDAO;
    private LibroDAO libroDAO;
    private LibroLettoreDAO libroLettoreDAO;
    private LuogoDAO luogoDAO;
    private RecensioneDAO recensioneDAO;

    /** Costruttore della classe Controller.
     * Inizializza i DAO necessari per le operazioni sul database.
     */
    public Controller() {
        try {
            // Ottieni la connessione al database
            Connection con = DBGestore.getConnection();
            // Inizializza i DAO con la connessione
            utenteDAO = new UtenteDAO(con);
            eventoDAO = new EventoDAO(con);
            figlioDAO = new FiglioDAO(con);
            libroDAO = new LibroDAO(con);
            libroLettoreDAO = new LibroLettoreDAO(con);
            luogoDAO = new LuogoDAO(con);
            recensioneDAO = new RecensioneDAO(con);
        } catch (Exception e) {
            // Gestione dell'eccezione in caso di errore nella connessione o nell'inizializzazione dei DAO
            e.printStackTrace();
        }
    }

    /** Controlla le credenziali di login di un utente.
     * @param username Username dell'utente.
     * @param password Password dell'utente.
     * @return RispostaLogin contenente il risultato del controllo e i dati dell'utente se il login ha successo,
     * i prossimi eventi in calendario ed eventuale messaggio di errore.
     */
    public RispostaLogin controllaLogin(String username, String password) {
        RispostaLogin risposta = new RispostaLogin(); // Inizializza la risposta
        try {
            // Controlla le credenziali dell'utente
            if (utenteDAO.checkCredentials(username, password) != null) {
                risposta.setSuccesso(true); // Login riuscito
                risposta.setUtente(utenteDAO.checkCredentials(username, password)); // Imposta l'utente nella risposta
                risposta.setProssimiEventi(eventoDAO.getNextEventi(LocalDateTime.now())); // Recupera i prossimi eventi
            } else {
                risposta.setSuccesso(false); // Login fallito
                risposta.setMessaggioerrore("Credenziali non valide."); // Imposta il messaggio di errore
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di login
            risposta.setMessaggioerrore("Errore richiesta al server."); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Recupera i prossimi eventi in calendario.
     * @param ultimoEvento Ultimo evento ricevuto dal client, usato per il caricamento progressivo.
     * @return RispostaNextEventi contenente il risultato della query,
     * la lista dei prossimi eventi ed eventuale messaggio di errore.
     */
    public RispostaNextEventi getNextEventi(Evento ultimoEvento) {
        RispostaNextEventi risposta = new RispostaNextEventi(); // Inizializza la risposta
        try {
            // Recupera i prossimi eventi in base all'ultimo evento ricevuto
            if (ultimoEvento == null)
                risposta.setProssimiEventi(eventoDAO.getNextEventi(LocalDateTime.now())); // Primo caricamento
            else {
                risposta.setProssimiEventi(eventoDAO.getNextEventi(ultimoEvento)); // Caricamento progressivo
            }
            risposta.setSuccesso(true); // Query riuscita
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il recupero degli eventi
            risposta.setMessaggioErrore("Errore richiesta al server."); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Iscrive un figlio a un evento.
     * @param figlio Figlio da iscrivere.
     * @param evento Evento a cui iscrivere il figlio.
     * @param genitore Genitore che effettua l'iscrizione.
     * @return RispostaIscrizioneEvento contenente il risultato dell'operazione su database,
     * dell'iscrizione ed eventuale messaggio di errore.
     */
    public RispostaIscrizioneEvento iscriviFiglioEvento(Figlio figlio, Evento evento, Genitore genitore) {
        RispostaIscrizioneEvento risposta = new RispostaIscrizioneEvento(figlio, evento, genitore); // Inizializza la risposta
        try {
            evento.setIscritti(eventoDAO.getIscrittiEvento(evento)); // Aggiorna la lista degli iscritti
            figlioDAO.iscriviFiglioEvento(figlio, evento); // Esegue l'iscrizione nel database
            figlio.iscrivi(evento, genitore); // Aggiorna lo stato del figlio
            risposta.setSuccesso(true); // Iscrizione riuscita
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante l'iscrizione
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Disiscrive un figlio da un evento.
     * @param figlio Figlio da disiscrivere.
     * @param evento Evento da cui disiscrivere il figlio.
     * @param genitore Genitore che effettua la disiscrizione.
     * @return RispostaDisiscrizioneEvento contenente il risultato dell'operazione su database,
     * della disiscrizione ed eventuale messaggio di errore.
     */
    public RispostaDisiscrizioneEvento disiscriviFiglioEvento(Figlio figlio, Evento evento, Genitore genitore) {
        RispostaDisiscrizioneEvento risposta = new RispostaDisiscrizioneEvento(figlio, evento, genitore); // Inizializza la risposta
        try {
            evento.setIscritti(eventoDAO.getIscrittiEvento(evento)); // Aggiorna la lista degli iscritti
            figlioDAO.cancellaFiglioEvento(figlio, evento); // Esegue la disiscrizione nel database
            figlio.disiscrivi(evento, genitore); // Aggiorna lo stato del figlio
            risposta.setSuccesso(true); // Disiscrizione riuscita
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante la disiscrizione
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Recupera la lista dei figli iscritti e dei genitori associati (per gestione listener) a un evento.
     * @param evento Evento di cui recuperare gli iscritti.
     * @return RispostaIscrittiEvento contenente il risultato della query,
     * la lista dei figli iscritti e i genitori associati, ed eventuale messaggio di errore.
     */
    public RispostaIscrittiEvento getIscrittiEvento(Evento evento) {
        RispostaIscrittiEvento risposta = new RispostaIscrittiEvento(evento); // Inizializza la risposta
        try {
            risposta.getEvento().setIscritti(eventoDAO.getIscrittiEvento(evento)); // Recupera i figli iscritti
            risposta.getEvento().addListenersGenitori(eventoDAO.getGenitoriIscritti(evento)); // Recupera i genitori associati
            risposta.setSuccesso(true); // Query riuscita
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il recupero degli iscritti
            risposta.setMessaggioerrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta;
    }

    /** Aggiorna le informazioni di un genitore.
     * @param genitore Genitore da aggiornare.
     * @return RispostaAggiornaGenitore contenente il risultato dell'operazione su database,
     * il genitore aggiornato ed eventuale messaggio di errore.
     */
    public RispostaAggiornaGenitore aggiornaGenitore(Genitore genitore) {
        RispostaAggiornaGenitore risposta = new RispostaAggiornaGenitore(); // Inizializza la risposta
        try {
            // Aggiorna le informazioni del genitore nel database
            if (utenteDAO.aggiornaGenitore(genitore)==1) {
                risposta.setSuccesso(true); // Aggiornamento riuscito
                risposta.setGenitore(genitore); // Imposta il genitore aggiornato nella risposta
            }
            else {
                risposta.setSuccesso(false); // Errore durante l'aggiornamento
                risposta.setMessaggioErrore("Errore aggiornamento genitore su DB"); // Imposta il messaggio di errore
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di aggiornamento
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Aggiunge un nuovo figlio a un genitore.
     * @param genitore Genitore a cui aggiungere il figlio.
     * @param nuovoFiglio Figlio da aggiungere.
     * @return RispostaAggiungiFiglio contenente il risultato dell'operazione su database,
     * il genitore e il nuovo figlio aggiunto, ed eventuale messaggio di errore.
     */
    public RispostaAggiungiFiglio aggiungiFiglio(Genitore genitore, Figlio nuovoFiglio) {
        RispostaAggiungiFiglio risposta = new RispostaAggiungiFiglio(); // Inizializza la risposta
        try {
            int id = figlioDAO.creaFiglio(nuovoFiglio.getNome(), nuovoFiglio.getDataNascita(), genitore); // Crea il nuovo figlio nel database
            if (id !=-1) {
                nuovoFiglio.setId(id); // Imposta l'ID del nuovo figlio
                risposta.setSuccesso(true); // Aggiunta riuscita
                risposta.setGenitore(genitore); // Imposta il genitore nella risposta
                risposta.setNuovoFiglio(nuovoFiglio); // Imposta il nuovo figlio nella risposta
            }
            else {
                risposta.setSuccesso(false); // Errore durante l'aggiunta
                risposta.setMessaggioErrore("Errore creazione figlio su DB"); // Imposta il messaggio di errore
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di aggiunta
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Registra un nuovo genitore nel sistema.
     * @param nuovoGenitore Genitore da registrare.
     * @param psw Password del genitore.
     * @return RispostaNuovoGenitore contenente il risultato dell'operazione su database,
     * il nuovo genitore registrato, ed eventuale messaggio di errore.
     */
    public RispostaNuovoGenitore registraNuovoGenitore(Genitore nuovoGenitore, String psw) {
        RispostaNuovoGenitore risposta = new RispostaNuovoGenitore(); // Inizializza la risposta
        try {
            // Controlla se l'username è già esistente
            if (utenteDAO.checkUserName(nuovoGenitore.getUserName())!=-1) {
                risposta.setSuccesso(false); // Registrazione fallita
                risposta.setMessaggioErrore("Username già esistente. Scegliere un altro username.");   // Imposta il messaggio di errore
                return risposta; // Restituisce la risposta
            }
            int id = utenteDAO.creaGenitore(nuovoGenitore.getNome(), nuovoGenitore.getCognome(), nuovoGenitore.getUserName(), psw); // Crea il nuovo genitore nel database
            // Imposta l'ID del nuovo genitore e aggiorna la risposta
            if (id !=-1) {
                nuovoGenitore.setId(id);  // Imposta l'ID del nuovo genitore
                risposta.setSuccesso(true); // Registrazione riuscita
                risposta.setNuovoGenitore(nuovoGenitore); // Imposta il nuovo genitore nella risposta
            }
            else {
                risposta.setSuccesso(false); // Errore durante la registrazione
                risposta.setMessaggioErrore("Errore creazione nuovo genitore su DB"); // Imposta il messaggio di errore
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di registrazione
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Registra un nuovo lettore nel sistema.
     * @param nuovoLettore Lettore da registrare.
     * @param psw Password del lettore.
     * @return RispostaNuovoLettore contenente il risultato dell'operazione su database,
     * il nuovo lettore registrato, ed eventuale messaggio di errore.
     */
    public RispostaNuovoLettore registraNuovoLettore(Lettore nuovoLettore, String psw) {
        RispostaNuovoLettore risposta = new RispostaNuovoLettore();  // Inizializza la risposta
        try {
            // Controlla se l'username è già esistente
            if (utenteDAO.checkUserName(nuovoLettore.getUserName())!=-1) {
                risposta.setSuccesso(false); // Registrazione fallita
                risposta.setMessaggioErrore("Username già esistente. Scegliere un altro username.");  // Imposta il messaggio di errore
                return risposta; // Restituisce la risposta
            }
            int id = utenteDAO.creaLettore(nuovoLettore.getNome(), nuovoLettore.getCognome(),
            nuovoLettore.getUserName(), psw); // Crea il nuovo lettore nel database
            // Imposta l'ID del nuovo lettore e aggiorna la risposta
            if (id !=-1) {
                nuovoLettore.setId(id); // Imposta l'ID del nuovo lettore
                risposta.setSuccesso(true); // Registrazione riuscita
                risposta.setNuovoLettore(nuovoLettore); // Imposta il nuovo lettore nella risposta
            }
            // Gestione dell'errore durante la registrazione
            else {
                risposta.setSuccesso(false); // Errore durante la registrazione
                risposta.setMessaggioErrore("Errore creazione nuovo lettore su DB"); // Imposta il messaggio di errore
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di registrazione
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Registra un nuovo amministratore nel sistema.
     * @param nuovoAmministratore Amministratore da registrare.
     * @param psw Password dell'amministratore.
     * @return RispostaNuovoAmministratore contenente il risultato dell'operazione su database,
     * il nuovo amministratore registrato, ed eventuale messaggio di errore.
     */
    public RispostaNuovoAmministratore registraNuovoAmministratore(Amministratore nuovoAmministratore, String psw) {
        RispostaNuovoAmministratore risposta = new RispostaNuovoAmministratore(); // Inizializza la risposta
        // Controlla se l'username è già esistente
        try {
            // Controlla se l'username è già esistente
            if (utenteDAO.checkUserName(nuovoAmministratore.getUserName())!=-1) {
                risposta.setSuccesso(false); // Registrazione fallita
                risposta.setMessaggioErrore("Username già esistente. Scegliere un altro username."); // Imposta il messaggio di errore
                return risposta; // Restituisce la risposta
            }
            int id = utenteDAO.creaAmministratore(nuovoAmministratore.getNome(), nuovoAmministratore.getCognome(),
                    nuovoAmministratore.getUserName(), psw); // Crea il nuovo amministratore nel database
            // Imposta l'ID del nuovo amministratore e aggiorna la risposta
            if (id !=-1) {
                nuovoAmministratore.setId(id); // Imposta l'ID del nuovo amministratore
                risposta.setSuccesso(true); // Registrazione riuscita
                risposta.setNuovoAmministratore(nuovoAmministratore); // Imposta il nuovo amministratore nella risposta
            }
            // Gestione dell'errore durante la registrazione
            else {
                risposta.setSuccesso(false); // Errore durante la registrazione
                risposta.setMessaggioErrore("Errore creazione nuovo amministratore su DB"); // Imposta il messaggio di errore
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di registrazione
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Recupera la lista di tutti i lettori, tutti i luoghi e tutti i libri dal database.
     * Utile per menu di selezione nelle interfacce grafiche.
     * @return RispostaLettoriELuoghiELibri contenente il risultato della query,
     * la lista di lettori, luoghi e libri, ed eventuale messaggio di errore.
     */
    public RispostaLettoriELuoghiELibri richiestaLettoriELuoghiELibri() {
        RispostaLettoriELuoghiELibri risposta = new RispostaLettoriELuoghiELibri(); // Inizializza la risposta
        // Recupera lettori, luoghi e libri dal database
        try {
            risposta.setLuoghi(luogoDAO.getLuoghi()); // Recupera i luoghi
            risposta.setLettori(utenteDAO.getLettori()); // Recupera i lettori
            risposta.setElencolibri(libroDAO.getLibri()); // Recupera i libri
            risposta.setSuccesso(true); // Query riuscita
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il recupero dei dati
        }
        return risposta; // Restituisce la risposta
    }

    /** Recupera le recensioni di un libro e verifica se un utente può recensirlo.
     * @param libro Libro di cui recuperare le recensioni.
     * @param utente Utente che vuole recensire il libro.
     * @return RispostaRecensioniERecensibilita contenente il risultato della query,
     * la lista delle recensioni del libro, la conferma della possibilità di recensirlo ed eventuale messaggio di errore.
     */
    public RispostaRecensioniERecensibilita richiestaRecensioniERecensibilita(Libro libro, Utente utente) {
        RispostaRecensioniERecensibilita risposta = new RispostaRecensioniERecensibilita(); // Inizializza la risposta
        try {
            risposta.setRecensioni(recensioneDAO.getRecensione(libro)); // Recupera le recensioni del libro
            risposta.setRecensibile(recensioneDAO.getRecensibilita(libro, utente)); // Verifica se l'utente può recensire il libro
            risposta.setSuccesso(true); // Query riuscita
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il recupero delle recensioni
            risposta.setMessaggioerrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
            // e.printStackTrace();  // Stampa lo stack trace per il debug
        }
        return risposta; // Restituisce la risposta
    }

    /** Aggiorna le informazioni di un lettore.
     * @param lettore Lettore da aggiornare.
     * @return RispostaAggiornaLettore contenente il risultato dell'operazione su database,
     * il lettore aggiornato ed eventuale messaggio di errore.
     */
    public RispostaAggiornaLettore aggiornaLettore(Lettore lettore) {
        RispostaAggiornaLettore risposta = new RispostaAggiornaLettore(); // Inizializza la risposta
        // Aggiorna le informazioni del lettore nel database
        try {
            if (utenteDAO.aggiornaLettore(lettore)==1) {
                risposta.setSuccesso(true); // Aggiornamento riuscito
                risposta.setLettore(lettore); // Imposta il lettore aggiornato nella risposta
            }
            else {
                risposta.setSuccesso(false); // Errore durante l'aggiornamento
                risposta.setMessaggioErrore("Errore aggiornamento lettore su DB"); // Imposta il messaggio di errore
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di aggiornamento
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Aggiorna le informazioni di un amministratore.
     * @param amministratore Amministratore da aggiornare.
     * @return RispostaAggiornaAmministratore contenente il risultato dell'operazione su database,
     * l'amministratore aggiornato ed eventuale messaggio di errore.
     */
    public RispostaAggiornaAmministratore aggiornaAmministratore(Amministratore amministratore) {
        RispostaAggiornaAmministratore risposta = new RispostaAggiornaAmministratore(); // Inizializza la risposta
        // Aggiorna le informazioni dell'amministratore nel database
        try {
            if (utenteDAO.aggiornaAmministratore(amministratore)==1) {
                risposta.setSuccesso(true); // Aggiornamento riuscito
                risposta.setAmministratore(amministratore); // Imposta l'amministratore aggiornato nella risposta
            }
            // Gestione dell'errore durante l'aggiornamento
            else {
                risposta.setSuccesso(false); // Errore durante l'aggiornamento
                risposta.setMessaggioErrore("Errore aggiornamento lettore su DB"); // Imposta il messaggio di errore
            }
        }
        // Gestione delle eccezioni
        catch (Exception e){
            risposta.setSuccesso(false); // Errore durante il processo di aggiornamento
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Salva in database un nuovo evento o un evento modificato, controllando prima eventuali conflitti di orario con altri eventi.
     * Relativamente all'evento salvato, la scaletta viene salvata sovrascrivendo eventuali scalette precedentemente salvate
     * @param evento Evento da salvare.
     * @return RispostaSalvaEvento contenente il risultato dell'operazione su database,
     * l'evento salvato ed eventuale messaggio di errore.
     */
    public RispostaSalvaEvento salvaEvento(Evento evento) {
        RispostaSalvaEvento risposta = new RispostaSalvaEvento(); // Inizializza la risposta
        try {
            ArrayList<Evento> eventiInConflitto = eventoDAO.eventiInConflitto(evento); // Controlla conflitti di orario con altri eventi
            // Se ci sono conflitti, prepara il messaggio di errore
            if (!eventiInConflitto.isEmpty()) {
                risposta.setSuccesso(false); // Conflitto di orario trovato
                String eventiConflittoStr = ""; // Costruisce il messaggio di errore con i dettagli dei conflitti
                // Cicla attraverso gli eventi in conflitto e aggiunge i dettagli al messaggio
                for (Evento ev : eventiInConflitto) {
                    eventiConflittoStr += "- " + ev.getNome() + " dalle " + ev.getData().toLocalTime().toString() + " alle " + ev.calcolaOraFine().toLocalTime().toString() + "\n"; // Aggiunge i dettagli dell'evento in conflitto
                }
                risposta.setMessaggioErrore("Conflitto di orario con altri eventi: \n" + eventiConflittoStr); // Imposta il messaggio di errore nella risposta
            } else {
                // Nessun conflitto, procedi con il salvataggio
                if (evento.getId() != 0) {
                    eventoDAO.modificaEvento(evento); // Modifica l'evento esistente nel database
                    evento.setIscritti(eventoDAO.getIscrittiEvento(evento)); // Aggiorna la lista degli iscritti
                    risposta.setSuccesso(true); // Salvataggio riuscito
                    risposta.setEvento(evento); // Imposta l'evento salvato nella risposta
                } else {
                    int id = eventoDAO.creaEvento(evento.getCreatore(), evento.getNome(), evento.getLuogo(), evento.getData()); // Crea un nuovo evento nel database
                    if (id == -1) {
                        risposta.setSuccesso(false); // Errore durante il salvataggio
                        risposta.setMessaggioErrore("Errore salvataggio evento su DB"); // Imposta il messaggio di errore
                    } else {
                    evento.setId(id); // Imposta l'ID del nuovo evento
                    libroLettoreDAO.creaScaletta(evento); // Crea la scaletta per il nuovo evento
                    evento.setIscritti(eventoDAO.getIscrittiEvento(evento)); // Inizializza la lista degli iscritti
                    risposta.setSuccesso(true); // Salvataggio riuscito
                    risposta.setEvento(evento); // Imposta l'evento salvato nella risposta
                    }
                }
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di salvataggio
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Aggiunge una nuova recensione a un libro.
     * @param recensione Recensione da aggiungere.
     * @return RispostaAggiungiRecensione contenente il risultato dell'operazione su database,
     * la recensione aggiunta ed eventuale messaggio di errore.
     */
    public RispostaAggiungiRecensione aggiungiRecensione(Recensione recensione) {
        RispostaAggiungiRecensione risposta = new RispostaAggiungiRecensione(); // Inizializza la risposta
        try {
            int id = recensioneDAO.creaRecensione(recensione.getTesto(), recensione.getLibro(), recensione.getGenitore()); // Crea la nuova recensione nel database
            if (id !=-1) {
                recensione.setId(id); // Imposta l'ID della nuova recensione
                risposta.setSuccesso(true); // Aggiunta riuscita
                risposta.setRecensione(recensione); // Imposta la recensione aggiunta nella risposta
            }
            else {
                risposta.setSuccesso(false); // Errore durante l'aggiunta
                risposta.setMessaggioErrore("Errore creazione nuova recensione su DB"); // Imposta il messaggio di errore
            }
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante il processo di aggiunta
            risposta.setMessaggioErrore("Errore richiesta nuova recensione al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }

    /** Cancella una recensione da un libro.
     * @param recensione Recensione da cancellare.
     * @return RispostaCancellaRecensione contenente il risultato dell'operazione su database,
     * la recensione cancellata ed eventuale messaggio di errore.
     */
    public RispostaCancellaRecensione cancellaRecensione(Recensione recensione) {
        RispostaCancellaRecensione risposta = new RispostaCancellaRecensione(recensione); // Inizializza la risposta
        try {
            recensioneDAO.cancellaRecensione(recensione); // Esegue la cancellazione della recensione nel database
            risposta.setSuccesso(true); // Cancellazione riuscita
        } catch (Exception e) {
            risposta.setSuccesso(false); // Errore durante la cancellazione
            risposta.setMessaggioErrore("Errore richiesta cancellazione recensione al server." + e.getMessage()); // Imposta il messaggio di errore
        }
        return risposta; // Restituisce la risposta
    }
}
