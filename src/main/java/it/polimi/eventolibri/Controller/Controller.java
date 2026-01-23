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
            Connection con = DBGestore.getConnection();
            utenteDAO = new UtenteDAO(con);
            eventoDAO = new EventoDAO(con);
            figlioDAO = new FiglioDAO(con);
            libroDAO = new LibroDAO(con);
            libroLettoreDAO = new LibroLettoreDAO(con);
            luogoDAO = new LuogoDAO(con);
            recensioneDAO = new RecensioneDAO(con);
        } catch (Exception e) {
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
        RispostaLogin risposta = new RispostaLogin();
        try {
            if (utenteDAO.checkCredentials(username, password) != null) {
                risposta.setSuccesso(true);
                risposta.setUtente(utenteDAO.checkCredentials(username, password));
                risposta.setProssimiEventi(eventoDAO.getNextEventi(LocalDateTime.now()));
            } else {
                risposta.setSuccesso(false);
                risposta.setMessaggioerrore("Credenziali non valide.");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioerrore("Errore richiesta al server.");// e.printStackTrace();
        }
        return risposta;
    }

    /** Recupera i prossimi eventi in calendario.
     * @param ultimoEvento Ultimo evento ricevuto dal client, usato per il caricamento progressivo.
     * @return RispostaNextEventi contenente il risultato della query,
     * la lista dei prossimi eventi ed eventuale messaggio di errore.
     */
    public RispostaNextEventi getNextEventi(Evento ultimoEvento) {
        RispostaNextEventi risposta = new RispostaNextEventi();
        try {
            if (ultimoEvento == null)
                risposta.setProssimiEventi(eventoDAO.getNextEventi(LocalDateTime.now()));
            else {
                risposta.setProssimiEventi(eventoDAO.getNextEventi(ultimoEvento));
            }
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server.");
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Iscrive un figlio a un evento.
     * @param figlio Figlio da iscrivere.
     * @param evento Evento a cui iscrivere il figlio.
     * @param genitore Genitore che effettua l'iscrizione.
     * @return RispostaIscrizioneEvento contenente il risultato dell'operazione su database,
     * dell'iscrizione ed eventuale messaggio di errore.
     */
    public RispostaIscrizioneEvento iscriviFiglioEvento(Figlio figlio, Evento evento, Genitore genitore) {
        RispostaIscrizioneEvento risposta = new RispostaIscrizioneEvento(figlio, evento, genitore);
        try {
            evento.setIscritti(eventoDAO.getIscrittiEvento(evento));
            figlioDAO.iscriviFiglioEvento(figlio, evento);
            figlio.iscrivi(evento, genitore);
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Disiscrive un figlio da un evento.
     * @param figlio Figlio da disiscrivere.
     * @param evento Evento da cui disiscrivere il figlio.
     * @param genitore Genitore che effettua la disiscrizione.
     * @return RispostaDisiscrizioneEvento contenente il risultato dell'operazione su database,
     * della disiscrizione ed eventuale messaggio di errore.
     */
    public RispostaDisiscrizioneEvento disiscriviFiglioEvento(Figlio figlio, Evento evento, Genitore genitore) {
        RispostaDisiscrizioneEvento risposta = new RispostaDisiscrizioneEvento(figlio, evento, genitore);
        try {
            evento.setIscritti(eventoDAO.getIscrittiEvento(evento));
            figlioDAO.cancellaFiglioEvento(figlio, evento);
            figlio.disiscrivi(evento, genitore);
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Recupera la lista dei figli iscritti e dei genitori associati (per gestione listener) a un evento.
     * @param evento Evento di cui recuperare gli iscritti.
     * @return RispostaIscrittiEvento contenente il risultato della query,
     * la lista dei figli iscritti e i genitori associati, ed eventuale messaggio di errore.
     */
    public RispostaIscrittiEvento getIscrittiEvento(Evento evento) {
        RispostaIscrittiEvento risposta = new RispostaIscrittiEvento(evento);
        try {
            risposta.getEvento().setIscritti(eventoDAO.getIscrittiEvento(evento));
            risposta.getEvento().addListenersGenitori(eventoDAO.getGenitoriIscritti(evento));
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioerrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Aggiorna le informazioni di un genitore.
     * @param genitore Genitore da aggiornare.
     * @return RispostaAggiornaGenitore contenente il risultato dell'operazione su database,
     * il genitore aggiornato ed eventuale messaggio di errore.
     */
    public RispostaAggiornaGenitore aggiornaGenitore(Genitore genitore) {
        RispostaAggiornaGenitore risposta = new RispostaAggiornaGenitore();
        try {
            if (utenteDAO.aggiornaGenitore(genitore)==1) {
                risposta.setSuccesso(true);
                risposta.setGenitore(genitore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore aggiornamento genitore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Aggiunge un nuovo figlio a un genitore.
     * @param genitore Genitore a cui aggiungere il figlio.
     * @param nuovoFiglio Figlio da aggiungere.
     * @return RispostaAggiungiFiglio contenente il risultato dell'operazione su database,
     * il genitore e il nuovo figlio aggiunto, ed eventuale messaggio di errore.
     */
    public RispostaAggiungiFiglio aggiungiFiglio(Genitore genitore, Figlio nuovoFiglio) {
        RispostaAggiungiFiglio risposta = new RispostaAggiungiFiglio();
        try {
            int id = figlioDAO.creaFiglio(nuovoFiglio.getNome(), nuovoFiglio.getDataNascita(), genitore);
            if (id !=-1) {
                nuovoFiglio.setId(id);
                risposta.setSuccesso(true);
                risposta.setGenitore(genitore);
                risposta.setNuovoFiglio(nuovoFiglio);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore creazione figlio su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Registra un nuovo genitore nel sistema.
     * @param nuovoGenitore Genitore da registrare.
     * @param psw Password del genitore.
     * @return RispostaNuovoGenitore contenente il risultato dell'operazione su database,
     * il nuovo genitore registrato, ed eventuale messaggio di errore.
     */
    public RispostaNuovoGenitore registraNuovoGenitore(Genitore nuovoGenitore, String psw) {
        RispostaNuovoGenitore risposta = new RispostaNuovoGenitore();
        try {
            if (utenteDAO.checkUserName(nuovoGenitore.getUserName())!=-1) {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Username già esistente. Scegliere un altro username.");
                return risposta;
            }
            int id = utenteDAO.creaGenitore(nuovoGenitore.getNome(), nuovoGenitore.getCognome(), nuovoGenitore.getUserName(), psw);
            if (id !=-1) {
                nuovoGenitore.setId(id);
                risposta.setSuccesso(true);
                risposta.setNuovoGenitore(nuovoGenitore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore creazione nuovo genitore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Registra un nuovo lettore nel sistema.
     * @param nuovoLettore Lettore da registrare.
     * @param psw Password del lettore.
     * @return RispostaNuovoLettore contenente il risultato dell'operazione su database,
     * il nuovo lettore registrato, ed eventuale messaggio di errore.
     */
    public RispostaNuovoLettore registraNuovoLettore(Lettore nuovoLettore, String psw) {
        RispostaNuovoLettore risposta = new RispostaNuovoLettore();
        try {
            if (utenteDAO.checkUserName(nuovoLettore.getUserName())!=-1) {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Username già esistente. Scegliere un altro username.");
                return risposta;
            }

            int id = utenteDAO.creaLettore(nuovoLettore.getNome(), nuovoLettore.getCognome(),
            nuovoLettore.getUserName(), psw);
            if (id !=-1) {
                nuovoLettore.setId(id);
                risposta.setSuccesso(true);
                risposta.setNuovoLettore(nuovoLettore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore creazione nuovo lettore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Registra un nuovo amministratore nel sistema.
     * @param nuovoAmministratore Amministratore da registrare.
     * @param psw Password dell'amministratore.
     * @return RispostaNuovoAmministratore contenente il risultato dell'operazione su database,
     * il nuovo amministratore registrato, ed eventuale messaggio di errore.
     */
    public RispostaNuovoAmministratore registraNuovoAmministratore(Amministratore nuovoAmministratore, String psw) {
        RispostaNuovoAmministratore risposta = new RispostaNuovoAmministratore();
        try {
            if (utenteDAO.checkUserName(nuovoAmministratore.getUserName())!=-1) {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Username già esistente. Scegliere un altro username.");
                return risposta;
            }

            int id = utenteDAO.creaAmministratore(nuovoAmministratore.getNome(), nuovoAmministratore.getCognome(),
                    nuovoAmministratore.getUserName(), psw);
            if (id !=-1) {
                nuovoAmministratore.setId(id);
                risposta.setSuccesso(true);
                risposta.setNuovoAmministratore(nuovoAmministratore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore creazione nuovo amministratore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Recupera la lista di tutti i lettori, tutti i luoghi e tutti i libri dal database.
     * Utile per menu di selezione nelle interfacce grafiche.
     * @return RispostaLettoriELuoghiELibri contenente il risultato della query,
     * la lista di lettori, luoghi e libri, ed eventuale messaggio di errore.
     */
    public RispostaLettoriELuoghiELibri richiestaLettoriELuoghiELibri() {
        RispostaLettoriELuoghiELibri risposta = new RispostaLettoriELuoghiELibri();
        try {
            risposta.setLuoghi(luogoDAO.getLuoghi());
            risposta.setLettori(utenteDAO.getLettori());
            risposta.setElencolibri(libroDAO.getLibri());
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Recupera le recensioni di un libro e verifica se un utente può recensirlo.
     * @param libro Libro di cui recuperare le recensioni.
     * @param utente Utente che vuole recensire il libro.
     * @return RispostaRecensioniERecensibilita contenente il risultato della query,
     * la lista delle recensioni del libro, la conferma della possibilità di recensirlo ed eventuale messaggio di errore.
     */
    public RispostaRecensioniERecensibilita richiestaRecensioniERecensibilita(Libro libro, Utente utente) {
        RispostaRecensioniERecensibilita risposta = new RispostaRecensioniERecensibilita();
        try {
            risposta.setRecensioni(recensioneDAO.getRecensione(libro));
            risposta.setRecensibile(recensioneDAO.getRecensibilita(libro, utente));
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioerrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Aggiorna le informazioni di un lettore.
     * @param lettore Lettore da aggiornare.
     * @return RispostaAggiornaLettore contenente il risultato dell'operazione su database,
     * il lettore aggiornato ed eventuale messaggio di errore.
     */
    public RispostaAggiornaLettore aggiornaLettore(Lettore lettore) {
        RispostaAggiornaLettore risposta = new RispostaAggiornaLettore();
        try {
            if (utenteDAO.aggiornaLettore(lettore)==1) {
                risposta.setSuccesso(true);
                risposta.setLettore(lettore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore aggiornamento lettore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Aggiorna le informazioni di un amministratore.
     * @param amministratore Amministratore da aggiornare.
     * @return RispostaAggiornaAmministratore contenente il risultato dell'operazione su database,
     * l'amministratore aggiornato ed eventuale messaggio di errore.
     */
    public RispostaAggiornaAmministratore aggiornaAmministratore(Amministratore amministratore) {
        RispostaAggiornaAmministratore risposta = new RispostaAggiornaAmministratore();
        try {
            if (utenteDAO.aggiornaAmministratore(amministratore)==1) {
                risposta.setSuccesso(true);
                risposta.setAmministratore(amministratore);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore aggiornamento lettore su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Salva in database un nuovo evento o un evento modificato, controllando prima eventuali conflitti di orario con altri eventi.
     * Relativamente all'evento salvato, la scaletta viene salvata sovrascrivendo eventuali scalette precedentemente salvate
     * @param evento Evento da salvare.
     * @return RispostaSalvaEvento contenente il risultato dell'operazione su database,
     * l'evento salvato ed eventuale messaggio di errore.
     */
    public RispostaSalvaEvento salvaEvento(Evento evento) {
        RispostaSalvaEvento risposta = new RispostaSalvaEvento();
        try {
            ArrayList<Evento> eventiInConflitto = eventoDAO.eventiInConflitto(evento);
            if (!eventiInConflitto.isEmpty()) {
                risposta.setSuccesso(false);
                String eventiConflittoStr = "";
                for (Evento ev : eventiInConflitto) {
                    eventiConflittoStr += "- " + ev.getNome() + " dalle " + ev.getData().toLocalTime().toString() + " alle " + ev.calcolaOraFine().toLocalTime().toString() + "\n";
                }
                risposta.setMessaggioErrore("Conflitto di orario con altri eventi: \n" + eventiConflittoStr);
            } else {
                // Nessun conflitto, procedi con il salvataggio
                if (evento.getId() != 0) {
                    eventoDAO.modificaEvento(evento);
                    evento.setIscritti(eventoDAO.getIscrittiEvento(evento));
                    risposta.setSuccesso(true);
                    risposta.setEvento(evento);
                } else {
                    int id = eventoDAO.creaEvento(evento.getCreatore(), evento.getNome(), evento.getLuogo(), evento.getData());
                    if (id == -1) {
                        risposta.setSuccesso(false);
                        risposta.setMessaggioErrore("Errore salvataggio evento su DB");
                    } else {
                    evento.setId(id);
                    libroLettoreDAO.creaScaletta(evento);
                    evento.setIscritti(eventoDAO.getIscrittiEvento(evento));
                    risposta.setSuccesso(true);
                    risposta.setEvento(evento);
                    }
                }
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Aggiunge una nuova recensione a un libro.
     * @param recensione Recensione da aggiungere.
     * @return RispostaAggiungiRecensione contenente il risultato dell'operazione su database,
     * la recensione aggiunta ed eventuale messaggio di errore.
     */
    public RispostaAggiungiRecensione aggiungiRecensione(Recensione recensione) {
        RispostaAggiungiRecensione risposta = new RispostaAggiungiRecensione();
        try {
            int id = recensioneDAO.creaRecensione(recensione.getTesto(), recensione.getLibro(), recensione.getGenitore());
            if (id !=-1) {
                recensione.setId(id);
                risposta.setSuccesso(true);
                risposta.setRecensione(recensione);
            }
            else {
                risposta.setSuccesso(false);
                risposta.setMessaggioErrore("Errore creazione nuova recensione su DB");
            }
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta nuova recensione al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }

    /** Cancella una recensione da un libro.
     * @param recensione Recensione da cancellare.
     * @return RispostaCancellaRecensione contenente il risultato dell'operazione su database,
     * la recensione cancellata ed eventuale messaggio di errore.
     */
    public RispostaCancellaRecensione cancellaRecensione(Recensione recensione) {
        RispostaCancellaRecensione risposta = new RispostaCancellaRecensione(recensione);
        try {
            recensioneDAO.cancellaRecensione(recensione);
            risposta.setSuccesso(true);
        } catch (Exception e) {
            risposta.setSuccesso(false);
            risposta.setMessaggioErrore("Errore richiesta cancellazione recensione al server." + e.getMessage());
            // e.printStackTrace();
        }
        return risposta;
    }
}
