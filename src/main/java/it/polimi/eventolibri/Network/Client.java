package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.View.*;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Classe che rappresenta il client dell'applicazione Eventolibri.
 * Si occupa di gestire la connessione con il server e le comunicazioni.
 */
public class Client {

    private Socket socket; // socket per la connessione con il server
    private ObjectOutputStream out; // stream di output per inviare messaggi al server
    private ObjectInputStream in; // stream di input per ricevere messaggi dal server
    private LoginView loginView; // vista di login
    private HomeGenitore homeGenitore; // vista home per il genitore
    private HomeLettore homeLettore; // vista home per il lettore
    private HomeAmministratore homeAmministratore; // vista home per l'amministratore
    private EventoView eventoView; // vista dell'evento per il genitore
    private EventoViewLettore eventoViewLettore; // vista dell'evento per il lettore
    private ProfiloGenitore profiloGenitore; // vista del profilo del genitore
    private ProfiloLettore profiloLettore; // vista del profilo del lettore
    private ProfiloAmministratore profiloAmministratore; // vista del profilo dell'amministratore
    private RegistraNewGenitore registraNewGenitore; // vista per la registrazione di un nuovo genitore
    private RegistraNewLettore registraNewLettore; // vista per la registrazione di un nuovo lettore
    private RegistraNewAmministratore registraNewAmministratore; // vista per la registrazione di un nuovo amministratore
    private LibroDetailedView libroDetailedView; // vista dettagliata del libro
    // add le nuove viste qui
    private Utente utente; // utente corrente (genitore, lettore o amministratore)

    /**
     * Inizializza il client e stabilisce la connessione con il server.
     *
     * @param loginView                 La vista di login.
     * @param homeGenitore              La vista home per il genitore.
     * @param homeAmministratore        La vista home per l'amministratore.
     * @param eventoView                La vista dell'evento per il genitore.
     * @param homeLettore               La vista home per il lettore.
     * @param eventoViewLettore         La vista dell'evento per il lettore.
     * @param profiloGenitore           La vista del profilo del genitore.
     * @param profiloLettore            La vista del profilo del lettore.
     * @param profiloAmministratore     La vista del profilo dell'amministratore.
     * @param registraNewGenitore       La vista per la registrazione di un nuovo genitore.
     * @param registraNewLettore        La vista per la registrazione di un nuovo lettore.
     * @param registraNewAmministratore La vista per la registrazione di un nuovo amministratore.
     * @param libroDetailedView         La vista dettagliata del libro.
     * @throws Exception In caso di errore durante la connessione.
     */
    public void start(LoginView loginView, HomeGenitore homeGenitore, HomeAmministratore homeAmministratore,
                      EventoView eventoView, HomeLettore homeLettore, EventoViewLettore eventoViewLettore,
                      ProfiloGenitore profiloGenitore, ProfiloLettore profiloLettore,
                      ProfiloAmministratore profiloAmministratore,
                      RegistraNewGenitore registraNewGenitore, RegistraNewLettore registraNewLettore,
                      RegistraNewAmministratore registraNewAmministratore, LibroDetailedView libroDetailedView) throws Exception {
        socket = new Socket("localhost", 5000); // connessione al server sulla porta 5000
        out = new ObjectOutputStream(socket.getOutputStream()); // inizializzazione stream di output
        in = new ObjectInputStream(socket.getInputStream()); // inizializzazione stream di input
        // inizializzazione delle viste
        this.loginView = loginView;
        this.homeGenitore = homeGenitore;
        this.eventoView = eventoView;
        this.homeLettore = homeLettore;
        this.homeAmministratore = homeAmministratore;
        this.eventoViewLettore = eventoViewLettore;
        this.profiloGenitore = profiloGenitore;
        this.profiloLettore = profiloLettore;
        this.profiloAmministratore = profiloAmministratore;
        this.registraNewGenitore = registraNewGenitore;
        this.registraNewLettore = registraNewLettore;
        this.registraNewAmministratore = registraNewAmministratore;
        this.libroDetailedView = libroDetailedView;
    }

    //** Costruttore di default
    public Client() {
    }

    //** Costruisce un client con socket e stream specificati (usato per i test).
    public Client(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
    }

    /**
     * Inizia l'ascolto dei messaggi dal server in un thread separato.
     */
    public Thread startListening() {
        // crea un nuovo thread per ascoltare i messaggi dal server
        Thread listenerThread = new Thread(() -> {
            try { // ciclo di ascolto dei messaggi
                Messaggio msg;
                while ((msg = (Messaggio) in.readObject()) != null) { // leggi finché c'è un messaggio
                    handleMessage(msg); // chiama la tua funzione per gestire il messaggio
                }
            } catch (EOFException e) { // gestisci la chiusura della connessione
                System.out.println("Connnessione chiusa dal server.");
            } catch (Exception e) { // gestisci altre eccezioni
                e.printStackTrace();
            } finally {
                close();
            }
        });
        // avvia il thread di ascolto
        listenerThread.start();
        // restituisci il thread in modo che possa essere gestito esternamente
        return listenerThread;
    }

    /**
     * Gestisce i messaggi ricevuti dal server.
     *
     * @param msg Il messaggio ricevuto.
     */
    private void handleMessage(Messaggio msg) {
        System.out.println("Messaggio ricevuto dal client: " + msg);

        // gestisce messaggio di risposta dal server dopo verifica credenziali al login, e in base al tipo di utente apre la vista corrispondente
        if (msg instanceof RispostaLogin) {
            if (((RispostaLogin) msg).isSuccesso()) {
                System.out.println("Login riuscito!");
                // determina il tipo di utente e mostra la vista corrispondente
                switch (((RispostaLogin) msg).getUtente()) {
                    case Genitore gen -> {
                        // crea un oggetto Genitore usando la factory
                        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                        Genitore genitore = creaGenitore.nuovoUtente(gen.getId(), gen.getNome(), gen.getCognome(), gen.getUserName());
                        // imposta i figli del genitore
                        genitore.setFigli(gen.getFigli());
                        // imposta l'utente corrente
                        utente = genitore;
                        // mostra la vista home del genitore con i dati ricevuti
                        homeGenitore.show(loginView.getStage(), genitore, ((RispostaLogin) msg).getProssimiEventi(),
                                () -> {
                                    loginView.show(loginView.getStage());
                                });
                    }
                    case Lettore let -> {
                        // crea un oggetto Lettore usando la factory
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore lettore = creaLettore.nuovoUtente(let.getId(), let.getNome(), let.getCognome(), let.getUserName());
                        // imposta iscrizioni ed eventi creati del lettore
                        lettore.setIscrizioniLettura(let.getIscrizioniLettura());
                        lettore.setEventiCreati(let.getEventiCreati());
                        // imposta l'utente corrente
                        utente = lettore;
                        // mostra la vista home del lettore con i dati ricevuti
                        homeLettore.show(loginView.getStage(), lettore, ((RispostaLogin) msg).getProssimiEventi(),
                                () -> {
                                    loginView.show(loginView.getStage());
                                });
                    }

                    case Amministratore amm -> {
                        // crea un oggetto Amministratore usando la factory
                        CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore();
                        Amministratore amministratore = creaAmministratore.nuovoUtente(amm.getId(), amm.getNome(), amm.getCognome(), amm.getUserName());
                        // imposta l'utente corrente
                        utente = amministratore;
                        // mostra la vista home dell'amministratore con i dati ricevuti
                        homeAmministratore.show(loginView.getStage(), amministratore,
                                () -> {
                                    loginView.show(loginView.getStage());
                                });
                    }
                    // gestisci tipi di utenti non previsti
                    default -> {
                        System.out.println("Tipo di utente non gestito.");
                    }
                    // gestisci altri tipi di utenti qui
                }
            } else {
                // mostra l'errore di login nella vista di login
                loginView.mostraErrore(((RispostaLogin) msg).getMessaggioerrore());
            }
            System.out.println("Ricevuto risposta login ");
        }

        // gestisce messaggio di risposta dal server con i prossimi eventi e aggiorna la vista corrispondente
        if (msg instanceof RispostaNextEventi) {
            if (((RispostaNextEventi) msg).isSuccesso()) {
                System.out.println("Next Eventi Arrivati!");
                // caso in cui ci sono meno di 10 eventi (nasconde il bottone "Carica Eventi Successivi")
                if (((RispostaNextEventi) msg).getProssimiEventi().size() < 10) {
                    if (utente instanceof Lettore) {
                        homeLettore.nascondiBottoneNextEventi();
                    }
                    if (utente instanceof Genitore) {
                        homeGenitore.nascondiBottoneNextEventi();
                    }
                }
                // se piu di 10 eventi, aggiorna la lista degli eventi nella vista (lasciando visibile il bottone "Carica Eventi Successivi")
                else {
                    if (utente instanceof Lettore) {
                        homeLettore.aggiornaEventi(((RispostaNextEventi) msg).getProssimiEventi());
                    } // se l'utente è un lettore
                    if (utente instanceof Genitore) {
                        homeGenitore.aggiornaEventi(((RispostaNextEventi) msg).getProssimiEventi());
                    } // se l'utente è un genitore
                }
            }
        }

        // gestisce messaggio di risposta dal server di iscrizione evento e aggiorna la vista corrispondente
        if (msg instanceof RispostaIscrizioneEvento) {
            if (((RispostaIscrizioneEvento) msg).isSuccesso()) {
                // per ogni figlio del genitore, se l'id corrisponde a quello del figlio iscritto, esegue l'iscrizione
                for (Figlio f : eventoView.getGenitore().getFigli()) {
                    if (f.getId() == ((RispostaIscrizioneEvento) msg).getFiglio().getId()) {
                        f.iscrivi(eventoView.getEvento(), eventoView.getGenitore());
                        // dopo l'iscrizione, richiede il numero aggiornato di iscritti all'evento
                        RichiestaIscrittiEvento richiestaIscritti = new RichiestaIscrittiEvento(eventoView.getEvento());
                        try {
                            this.sendMessage(richiestaIscritti); // invia la richiesta al server
                        } catch (IOException e) {
                            System.out.println("Errore nel richiestaIscrittiEvento" + e.getMessage());
                        }
                    }
                }
                System.out.println("Iscritto " + ((RispostaIscrizioneEvento) msg).getFiglio().getNome());
            }
            // in caso di errore, mostra il messaggio di errore nella vista
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaIscrizioneEvento) msg).getMessaggioErrore());
                eventoView.mostraErrore(((RispostaIscrizioneEvento) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server con il numero di iscritti e i listeners (genitori) ad un evento e aggiorna la vista corrispondente
        if (msg instanceof RispostaIscrittiEvento) {
            if (((RispostaIscrittiEvento) msg).isSuccesso()) {
                // aggiorna il numero di iscritti e i listeners nella vista corrispondente, in base al tipo di utente
                if (utente instanceof Genitore) {
                    eventoView.aggiornaIscritti(((RispostaIscrittiEvento) msg).getEvento().getIscritti());
                    for (Listener l : ((RispostaIscrittiEvento) msg).getEvento().getListeners()) {
                        eventoView.getEvento().addListener(l);
                    }
                }
                if (utente instanceof Lettore) {
                    eventoViewLettore.aggiornaIscritti(((RispostaIscrittiEvento) msg).getEvento().getIscritti());
                    for (Listener l : ((RispostaIscrittiEvento) msg).getEvento().getListeners()) {
                        eventoViewLettore.getEvento().addListener(l);
                    }
                }
                System.out.println("Numero iscritti aggiornato: " + ((RispostaIscrittiEvento) msg).getEvento().getIscritti());
                System.out.println("Listeners aggiornati.");
            }
        }

        // gestisce messaggio di risposta dal server di disiscrizione evento e aggiorna la vista corrispondente
        if (msg instanceof RispostaDisiscrizioneEvento) {
            if (((RispostaDisiscrizioneEvento) msg).isSuccesso()) {
                // per ogni figlio del genitore, se l'id corrisponde a quello del figlio disiscritto, esegue la disiscrizione
                for (Figlio f : eventoView.getGenitore().getFigli()) {
                    if (f.getId() == ((RispostaDisiscrizioneEvento) msg).getFiglio().getId()) {
                        f.disiscrivi(eventoView.getEvento(), eventoView.getGenitore());
                        RichiestaIscrittiEvento richiestaIscritti = new RichiestaIscrittiEvento(eventoView.getEvento());
                        try {
                            this.sendMessage(richiestaIscritti);
                        } catch (IOException e) {
                            System.out.println("Errore nel richiestaIscrittiEvento" + e.getMessage());
                        }
                    }
                }
                System.out.println("Disiscritto " + ((RispostaDisiscrizioneEvento) msg).getFiglio().getNome());
            }
            // in caso di errore, mostra il messaggio di errore nella vista
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaDisiscrizioneEvento) msg).getMessaggioErrore());
                eventoView.mostraErrore(((RispostaDisiscrizioneEvento) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiornamento profilo genitore e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiornaGenitore) {
            if (((RispostaAggiornaGenitore) msg).isSuccesso()) {
                // aggiorna i dati del genitore nella vista profiloGenitore
                profiloGenitore.getGenitore().setNome(((RispostaAggiornaGenitore) msg).getGenitore().getNome());
                profiloGenitore.getGenitore().setCognome(((RispostaAggiornaGenitore) msg).getGenitore().getCognome());
                // mostra il messaggio di successo
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Genitore");
                    alert.setHeaderText("Dati Genitore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });
            }
            // in caso di errore, mostra il messaggio di errore nella vista
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiornaGenitore) msg).getMessaggioErrore());
                profiloGenitore.mostraErrore(((RispostaAggiornaGenitore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiunta figlio e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiungiFiglio) {
            if (((RispostaAggiungiFiglio) msg).isSuccesso()) {
                // aggiunge il nuovo figlio al genitore nella vista profiloGenitore
                profiloGenitore.getGenitore().aggiungiFiglio((((RispostaAggiungiFiglio) msg).getNuovoFiglio()));
                profiloGenitore.aggiornaFigli((((RispostaAggiungiFiglio) msg).getNuovoFiglio()));
                // mostra il messaggio di successo
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Genitore");
                    alert.setHeaderText("Aggiunto nuovo figlio!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });
            }
            // in caso di errore, mostra il messaggio di errore nella vista
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiungiFiglio) msg).getMessaggioErrore());
                profiloGenitore.mostraErrore2(((RispostaAggiungiFiglio) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di registrazione nuovo genitore e aggiorna la vista corrispondente
        if (msg instanceof RispostaNuovoGenitore) {
            if (((RispostaNuovoGenitore) msg).isSuccesso()) { // mostra il messaggio di successo
                registraNewGenitore.mostraSuccesso("Nuovo genitore registrato!");
            } else { // in caso di errore, mostra il messaggio di errore nella vista
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoGenitore) msg).getMessaggioErrore());
                registraNewGenitore.mostraErrore(((RispostaNuovoGenitore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server con l'elenco di lettori, luoghi e libri (per menu a tendina) e aggiorna la vista corrispondente
        if (msg instanceof RispostaLettoriELuoghiELibri) {
            if (((RispostaLettoriELuoghiELibri) msg).isSuccesso()) { // aggiorna lettori, luoghi e libri nelle viste corrispondenti
                eventoViewLettore.aggiornaLettoriELuoghiELibri(((RispostaLettoriELuoghiELibri) msg).getLettori(),
                        ((RispostaLettoriELuoghiELibri) msg).getLuoghi(), ((RispostaLettoriELuoghiELibri) msg).getElencolibri());
                homeGenitore.aggiornaLibri(((RispostaLettoriELuoghiELibri) msg).getElencolibri());
                homeAmministratore.aggiornaLibri(((RispostaLettoriELuoghiELibri) msg).getElencolibri());
                homeLettore.aggiornaLibri(((RispostaLettoriELuoghiELibri) msg).getElencolibri());
            } else { // in caso di errore, mostra il messaggio di errore nella vista
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaLettoriELuoghiELibri) msg).getMessaggioerrore());
                eventoViewLettore.mostraErrore(((RispostaLettoriELuoghiELibri) msg).getMessaggioerrore());
                homeGenitore.mostraErrore(((RispostaLettoriELuoghiELibri) msg).getMessaggioerrore());
                homeAmministratore.mostraErrore(((RispostaLettoriELuoghiELibri) msg).getMessaggioerrore());
                homeLettore.mostraErrore(((RispostaLettoriELuoghiELibri) msg).getMessaggioerrore());
            }

        }

        // gestisce messaggio di risposta dal server con le recensioni e la recensibilità di un libro e aggiorna la vista corrispondente
        if (msg instanceof RispostaRecensioniERecensibilita) {
            if (((RispostaRecensioniERecensibilita) msg).isSuccesso()) {
                // aggiorna la recensibilità e le recensioni del libro nelle viste corrispondenti
                homeGenitore.setRecensibile(((RispostaRecensioniERecensibilita) msg).isRecensibile());
                homeGenitore.setRecensioni(((RispostaRecensioniERecensibilita) msg).getRecensioni());
                homeAmministratore.setRecensibile(((RispostaRecensioniERecensibilita) msg).isRecensibile());
                homeAmministratore.setRecensioni(((RispostaRecensioniERecensibilita) msg).getRecensioni());
                homeLettore.setRecensibile(((RispostaRecensioniERecensibilita) msg).isRecensibile());
                homeLettore.setRecensioni(((RispostaRecensioniERecensibilita) msg).getRecensioni());
            } else { // in caso di errore, mostra il messaggio di errore nella vista
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaRecensioniERecensibilita) msg).getMessaggioerrore());
                homeGenitore.mostraErrore(((RispostaRecensioniERecensibilita) msg).getMessaggioerrore());
                homeAmministratore.mostraErrore(((RispostaRecensioniERecensibilita) msg).getMessaggioerrore());
                homeLettore.mostraErrore(((RispostaRecensioniERecensibilita) msg).getMessaggioerrore());
            } // imposta lo stato di attesa a false in tutte le viste
            homeGenitore.setAttendi(false);
            homeAmministratore.setAttendi(false);
            homeLettore.setAttendi(false);
        }

        // gestisce messaggio di risposta dal server di registrazione nuovo lettore e aggiorna la vista corrispondente
        if (msg instanceof RispostaNuovoLettore) {
            if (((RispostaNuovoLettore) msg).isSuccesso()) { // mostra il messaggio di successo
                registraNewLettore.mostraSuccesso("Nuovo lettore registrato!");
            } else { // in caso di errore, mostra il messaggio di errore nella vista
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoLettore) msg).getMessaggioErrore());
                registraNewLettore.mostraErrore(((RispostaNuovoLettore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di registrazione nuovo amministratore e aggiorna la vista corrispondente
        if (msg instanceof RispostaNuovoAmministratore) {
            if (((RispostaNuovoAmministratore) msg).isSuccesso()) { // mostra il messaggio di successo
                registraNewAmministratore.mostraSuccesso("Nuovo amministratore registrato!");
            } else {  // in caso di errore, mostra il messaggio di errore nella vista
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoAmministratore) msg).getMessaggioErrore());
                registraNewAmministratore.mostraErrore(((RispostaNuovoAmministratore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiornamento profilo lettore e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiornaLettore) {
            if (((RispostaAggiornaLettore) msg).isSuccesso()) { // aggiorna i dati del lettore nella vista profiloLettore
                profiloLettore.getLettore().setNome(((RispostaAggiornaLettore) msg).getLettore().getNome());
                profiloLettore.getLettore().setCognome(((RispostaAggiornaLettore) msg).getLettore().getCognome());

                Platform.runLater(() -> { // mostra alert di successo
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Lettore");
                    alert.setHeaderText("Dati Lettore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });
                // in caso di errore, mostra il messaggio di errore nella vista
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiornaLettore) msg).getMessaggioErrore());
                profiloLettore.mostraErrore(((RispostaAggiornaLettore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiornamento profilo amministratore e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiornaAmministratore) {
            if (((RispostaAggiornaAmministratore) msg).isSuccesso()) { // aggiorna i dati dell'amministratore nella vista profiloAmministratore
                profiloAmministratore.getAmministratore().setNome(((RispostaAggiornaAmministratore) msg).getAmministratore().getNome());
                profiloAmministratore.getAmministratore().setCognome(((RispostaAggiornaAmministratore) msg).getAmministratore().getCognome());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Amministratore");
                    alert.setHeaderText("Dati Amministratore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                }); // mostra alert di successo
                // in caso di errore, mostra il messaggio di errore nella vista
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiornaAmministratore) msg).getMessaggioErrore());
                profiloAmministratore.mostraErrore(((RispostaAggiornaAmministratore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di salvataggio evento e aggiorna i dati nella view e la vista corrispondente
        if (msg instanceof RispostaSalvaEvento) {
            if (((RispostaSalvaEvento) msg).isSuccesso()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Salvataggio Evento");
                    alert.setHeaderText("Evento salvato con successo!");
                    alert.setContentText(null);
                    alert.showAndWait();
                }); // mostra alert di successo
                // aggiorna i dati del lettore in base all'evento salvato
                if (eventoViewLettore.getLettore().getId() == (((RispostaSalvaEvento) msg).getEvento().getCreatore().getId())) {
                    eventoViewLettore.getLettore().aggiungiEventiCreati(((RispostaSalvaEvento) msg).getEvento());
                } // se il lettore è il creatore dell'evento, aggiunge l'evento agli eventi creati
                if (((RispostaSalvaEvento) msg).getEvento().isIscritto(eventoViewLettore.getLettore())) {
                    eventoViewLettore.getLettore().aggiungiIscrizioneLettura(((RispostaSalvaEvento) msg).getEvento());
                } // se il lettore è iscritto all'evento, aggiunge l'evento alle iscrizioni di lettura
                if (!((RispostaSalvaEvento) msg).getEvento().isIscritto(eventoViewLettore.getLettore())) {
                    eventoViewLettore.getLettore().rimuoviIscrizioneLettura(((RispostaSalvaEvento) msg).getEvento());
                }
                // aggiorna i dati nell'eventoViewLettore (che è anche un elemento dell'arraylist eventiprossimi del homeLettore)
                eventoViewLettore.setEvento(((RispostaSalvaEvento) msg).getEvento());
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaSalvaEvento) msg).getMessaggioErrore());
                eventoViewLettore.mostraErrore(((RispostaSalvaEvento) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiunta recensione e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiungiRecensione) {
            if (((RispostaAggiungiRecensione) msg).isSuccesso()) { // aggiunge la nuova recensione alla lista delle recensioni nella vista
                libroDetailedView.aggiornaRecensioni(((RispostaAggiungiRecensione) msg).getRecensione());
                Platform.runLater(() -> { // mostra alert di successo
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Recensione");
                    alert.setHeaderText("Aggiunto nuova recensione!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });
                // in caso di errore, mostra il messaggio di errore nella vista
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiungiRecensione) msg).getMessaggioErrore());
                libroDetailedView.mostraErrore(((RispostaAggiungiRecensione) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di cancellazione recensione e aggiorna la vista corrispondente
        if (msg instanceof RispostaCancellaRecensione) { // cancella la recensione dalla lista delle recensioni nella vista
            if (((RispostaCancellaRecensione) msg).isSuccesso()) { // aggiorna la vista rimuovendo la recensione cancellata
                libroDetailedView.cancellaRecensione(((RispostaCancellaRecensione) msg).getId());
                System.out.println("Cancellata recensione con ID: " + ((RispostaCancellaRecensione) msg).getId());
                libroDetailedView.mostraErrore2("Cancellata recensione con ID: " + ((RispostaCancellaRecensione) msg).getId());
                Platform.runLater(() -> { // mostra alert di successo
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK); // crea un alert di successo
                    alert.setTitle("Cancellazione Recensione"); // titolo dell'alert
                    alert.setHeaderText("Recensione cancellata correttamente!"); // messaggio di successo
                    alert.setContentText(null); // nessun contenuto
                    alert.showAndWait(); // mostra alert di successo
                });
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaCancellaRecensione) msg).getMessaggioErrore()); // usa mostraErrore2 per non sovrapporsi con l'alert di successo
                libroDetailedView.mostraErrore2(((RispostaCancellaRecensione) msg).getMessaggioErrore()); // usa mostraErrore2 per non sovrapporsi con l'alert di successo
            }
            libroDetailedView.setAttendi(false); // termina l'attesa del client
        }

        // gestisce notifica di aggiornamento evento e aggiorna i dati nella view e la vista corrispondente
        if (msg instanceof NotificaAggiornamentoEvento) {
            Stage stage = loginView.getStage(); // ottiene lo stage principale
            Scene scene = stage.getScene(); // ottiene la scena attualmente visualizzata

            // Verifica se vista attualmente visualizzata è compatibile con un refresh
            boolean viewCoerente = false; // inizializza come non compatibile
            if (stage != null && stage.isShowing()) {
                if (utente instanceof Genitore) { // se l'utente è un genitore
                    if (scene == homeGenitore.getScene()) {
                        viewCoerente = true; // compatibile per refresh
                    }
                }
                if (utente instanceof Lettore) { // se l'utente è un lettore
                    if (scene == homeLettore.getScene()) {
                        viewCoerente = true; // compatibile per refresh
                    }
                }
            }
            // mostra un alert di notifica aggiornamento evento
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                alert.setTitle("Aggiornamento Live Evento");
                alert.setHeaderText("Ricevuto aggiornamento evento: "
                        + ((NotificaAggiornamentoEvento) msg).getEvento().getNome() + " con successo!");
                alert.setContentText(null);
                alert.showAndWait();
            });
            if (utente instanceof Genitore) { // se l'utente è un genitore, aggiorna l'evento per i figli iscritti
                for (Figlio f : ((Genitore) utente).getFigli()) {
                    if (f.isIscritto(((NotificaAggiornamentoEvento) msg).getEvento())) {
                        f.aggiornaEvento(((NotificaAggiornamentoEvento) msg).getEvento());
                    }
                }
                // aggiorna l'evento nell'arraylist eventiProssimi del homeGenitore
                for (Evento e : homeGenitore.getEventiProssimi()) {
                    // se l'id corrisponde, aggiorna l'evento
                    if (e.getId() == ((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                        homeGenitore.getEventiProssimi().set(homeGenitore.getEventiProssimi().indexOf(e),
                                ((NotificaAggiornamentoEvento) msg).getEvento());
                    }
                }
                // aggiorna la vista eventoView se è aperta e corrisponde all'evento aggiornato
                if (eventoView.getScene() != null && eventoView.getScene() == stage.getScene() && eventoView.getEvento().getId() == ((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                    eventoView.aggiornaEvento(((NotificaAggiornamentoEvento) msg).getEvento());
                }
                // aggiorna la lista degli eventi nella vista solo se la vista è coerente
                if (viewCoerente) {
                    homeGenitore.aggiornaEventi(homeGenitore.getEventiProssimi());
                }
            }
            if (utente instanceof Lettore) { // se l'utente è il creatore, aggiorna l'evento creato
                if (((NotificaAggiornamentoEvento) msg).getEvento().getCreatore().getId() == utente.getId()) {
                    // aggiorna l'evento creato nell'arraylist eventiCreati del lettore
                    for (Evento e : homeLettore.getLettore().getEventiCreati()) {
                        // se l'id corrisponde, aggiorna l'evento
                        if (e.getId() == ((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                            e.aggiornaEvento(((NotificaAggiornamentoEvento) msg).getEvento());
                        }
                    }
                }
                // aggiunge l'iscrizione se è iscritto
                if (((NotificaAggiornamentoEvento) msg).getEvento().isIscritto(homeLettore.getLettore())) {
                    homeLettore.getLettore().aggiungiIscrizioneLettura(((NotificaAggiornamentoEvento) msg).getEvento());
                }
                // rimuove l'iscrizione se non è più iscritto
                if (!((NotificaAggiornamentoEvento) msg).getEvento().isIscritto(homeLettore.getLettore())) {
                    homeLettore.getLettore().rimuoviIscrizioneLettura(((NotificaAggiornamentoEvento) msg).getEvento());
                }
                // aggiorna l'evento nell'arraylist eventiProssimi del homeLettore
                for (Evento e : homeLettore.getEventiProssimi()) {
                    // se l'id corrisponde, aggiorna l'evento
                    if (e.getId() == ((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                        homeLettore.getEventiProssimi().set(homeLettore.getEventiProssimi().indexOf(e),
                                ((NotificaAggiornamentoEvento) msg).getEvento());
                    }
                }
                // aggiorna la vista eventoViewLettore se è aperta e corrisponde all'evento aggiornato
                if (eventoViewLettore.getScene() != null && eventoViewLettore.getScene() == stage.getScene() && eventoViewLettore.getEvento().getId() == ((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                    eventoViewLettore.mostraErrore("Aggiornamento da altro utente: la copia su cui lavori è non aggiornata. \n" +
                            "Il tuo salvataggio potrebbe sovrascrivere le modifiche altrui.");
                }
                // aggiorna la lista degli eventi nella vista solo se la vista è coerente
                if (viewCoerente) {
                    homeLettore.aggiornaEventi(homeLettore.getEventiProssimi());
                }
            }
        }
    }

    /**
     * Chiude le risorse del client.
     */
    private void close() {
        // chiude le risorse in modo sicuro
        try {
            if (in != null) in.close(); // chiude lo stream di input
            if (out != null) out.close(); // chiude lo stream di output
            if (socket != null) socket.close(); // chiude il socket
        } catch (IOException e) {
            e.printStackTrace(); // stampa lo stack trace in caso di errore
        }
    }

    /**
     * Invia un messaggio al server.
     *
     * @param msg Il messaggio da inviare.
     * @throws IOException In caso di errore durante l'invio.
     */
    public void sendMessage(Messaggio msg) throws IOException {
        out.writeObject(msg); // invia il messaggio al server
        out.flush(); // assicura che tutti i dati siano inviati
        out.reset(); // resetta lo stream per evitare problemi con oggetti serializzati
    }

}
