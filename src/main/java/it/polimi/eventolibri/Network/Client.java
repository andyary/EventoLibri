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

/** Classe che rappresenta il client dell'applicazione Eventolibri.
 * Si occupa di gestire la connessione con il server e le comunicazioni.
 */
public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private LoginView loginView;
    private HomeGenitore homeGenitore;
    private HomeLettore homeLettore;
    private HomeAmministratore homeAmministratore;
    private EventoView eventoView;
    private EventoViewLettore eventoViewLettore;
    private ProfiloGenitore profiloGenitore;
    private ProfiloLettore profiloLettore;
    private ProfiloAmministratore profiloAmministratore;
    private RegistraNewGenitore registraNewGenitore;
    private RegistraNewLettore registraNewLettore;
    private RegistraNewAmministratore registraNewAmministratore;
    private LibroDetailedView libroDetailedView;
    // add le nuove viste qui
    private Utente utente;

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
        socket = new Socket("localhost", 5000);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
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
        Thread listenerThread = new Thread(() -> {
            try {
                Messaggio msg;
                while ((msg = (Messaggio) in.readObject()) != null) { // leggi finché c'è un messaggio
                    handleMessage(msg); // chiama la tua funzione per gestire il messaggio
                }
            } catch (EOFException e) {
                System.out.println("Connnessione chiusa dal server.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }
        });
        listenerThread.start();
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
                switch (((RispostaLogin) msg).getUtente()) {
                    case Genitore gen -> {
                        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                        Genitore genitore = creaGenitore.nuovoUtente(gen.getId(), gen.getNome(), gen.getCognome(), gen.getUserName());
                        genitore.setFigli(gen.getFigli());
                        utente = genitore;
                        homeGenitore.show(loginView.getStage(), genitore, ((RispostaLogin) msg).getProssimiEventi(),
                                () -> {
                                    loginView.show(loginView.getStage());
                                });
                    }
                    case Lettore let -> {
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore lettore = creaLettore.nuovoUtente(let.getId(), let.getNome(), let.getCognome(), let.getUserName());
                        lettore.setIscrizioniLettura(let.getIscrizioniLettura());
                        lettore.setEventiCreati(let.getEventiCreati());
                        utente = lettore;

                        homeLettore.show(loginView.getStage(), lettore, ((RispostaLogin) msg).getProssimiEventi(),
                                () -> {
                                    loginView.show(loginView.getStage());
                                });
                    }

                    case Amministratore amm -> {
                        CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore();
                        Amministratore amministratore = creaAmministratore.nuovoUtente(amm.getId(), amm.getNome(), amm.getCognome(), amm.getUserName());
                        utente = amministratore;
                        homeAmministratore.show(loginView.getStage(), amministratore,
                                () -> {
                                    loginView.show(loginView.getStage());
                                });
                    }

                    default -> {
                        System.out.println("Tipo di utente non gestito.");
                    }
                    // gestisci altri tipi di utenti qui
                }


            } else {
                loginView.mostraErrore(((RispostaLogin) msg).getMessaggioerrore());
            }
            System.out.println("Ricevuto risposta login ");
        }

        // gestisce messaggio di risposta dal server con i prossimi eventi e aggiorna la vista corrispondente
        if (msg instanceof RispostaNextEventi) {
            if (((RispostaNextEventi) msg).isSuccesso()) {
                System.out.println("Next Eventi Arrivati!");
                if (((RispostaNextEventi) msg).getProssimiEventi().size() < 10) {
                    if (utente instanceof Lettore) {
                        homeLettore.nascondiBottoneNextEventi();
                    }
                    if (utente instanceof Genitore) {
                        homeGenitore.nascondiBottoneNextEventi();
                    }
                } else {
                    if (utente instanceof Lettore) {
                        homeLettore.aggiornaEventi(((RispostaNextEventi) msg).getProssimiEventi());
                    }
                    if (utente instanceof Genitore) {
                        homeGenitore.aggiornaEventi(((RispostaNextEventi) msg).getProssimiEventi());
                    }

                }

            }
        }

        // gestisce messaggio di risposta dal server di iscrizione evento e aggiorna la vista corrispondente
        if (msg instanceof RispostaIscrizioneEvento) {
            if (((RispostaIscrizioneEvento) msg).isSuccesso()) {
                for (Figlio f : eventoView.getGenitore().getFigli()) {
                    if (f.getId() == ((RispostaIscrizioneEvento) msg).getFiglio().getId()) {
                        f.iscrivi(eventoView.getEvento(), eventoView.getGenitore());
                        RichiestaIscrittiEvento richiestaIscritti = new RichiestaIscrittiEvento(eventoView.getEvento());
                        try {
                            this.sendMessage(richiestaIscritti);
                        } catch (IOException e) {
                            System.out.println("Errore nel richiestaIscrittiEvento" + e.getMessage());
                        }
                    }
                }
                System.out.println("Iscritto " + ((RispostaIscrizioneEvento) msg).getFiglio().getNome());
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaIscrizioneEvento) msg).getMessaggioErrore());
                eventoView.mostraErrore(((RispostaIscrizioneEvento) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server con il numero di iscritti e i listeners (genitori) ad un evento e aggiorna la vista corrispondente
        if (msg instanceof RispostaIscrittiEvento) {
            if (((RispostaIscrittiEvento) msg).isSuccesso()) {
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
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaDisiscrizioneEvento) msg).getMessaggioErrore());
                eventoView.mostraErrore(((RispostaDisiscrizioneEvento) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiornamento profilo genitore e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiornaGenitore) {
            if (((RispostaAggiornaGenitore) msg).isSuccesso()) {
                profiloGenitore.getGenitore().setNome(((RispostaAggiornaGenitore) msg).getGenitore().getNome());
                profiloGenitore.getGenitore().setCognome(((RispostaAggiornaGenitore) msg).getGenitore().getCognome());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Genitore");
                    alert.setHeaderText("Dati Genitore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiornaGenitore) msg).getMessaggioErrore());
                profiloGenitore.mostraErrore(((RispostaAggiornaGenitore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiunta figlio e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiungiFiglio) {
            if (((RispostaAggiungiFiglio) msg).isSuccesso()) {
                profiloGenitore.getGenitore().aggiungiFiglio((((RispostaAggiungiFiglio) msg).getNuovoFiglio()));
                profiloGenitore.aggiornaFigli((((RispostaAggiungiFiglio) msg).getNuovoFiglio()));

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Genitore");
                    alert.setHeaderText("Aggiunto nuovo figlio!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiungiFiglio) msg).getMessaggioErrore());
                profiloGenitore.mostraErrore2(((RispostaAggiungiFiglio) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di registrazione nuovo genitore e aggiorna la vista corrispondente
        if (msg instanceof RispostaNuovoGenitore) {
            if (((RispostaNuovoGenitore) msg).isSuccesso()) {
                registraNewGenitore.mostraSuccesso("Nuovo genitore registrato!");
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoGenitore) msg).getMessaggioErrore());
                registraNewGenitore.mostraErrore(((RispostaNuovoGenitore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server con l'elenco di lettori, luoghi e libri (per menu a tendina) e aggiorna la vista corrispondente
        if (msg instanceof RispostaLettoriELuoghiELibri) {
            if (((RispostaLettoriELuoghiELibri) msg).isSuccesso()) {
                eventoViewLettore.aggiornaLettoriELuoghiELibri(((RispostaLettoriELuoghiELibri) msg).getLettori(),
                        ((RispostaLettoriELuoghiELibri) msg).getLuoghi(), ((RispostaLettoriELuoghiELibri) msg).getElencolibri());
                homeGenitore.aggiornaLibri(((RispostaLettoriELuoghiELibri) msg).getElencolibri());
                homeAmministratore.aggiornaLibri(((RispostaLettoriELuoghiELibri) msg).getElencolibri());
                homeLettore.aggiornaLibri(((RispostaLettoriELuoghiELibri) msg).getElencolibri());
            } else {
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
                homeGenitore.setRecensibile(((RispostaRecensioniERecensibilita) msg).isRecensibile());
                homeGenitore.setRecensioni(((RispostaRecensioniERecensibilita) msg).getRecensioni());
                homeAmministratore.setRecensibile(((RispostaRecensioniERecensibilita) msg).isRecensibile());
                homeAmministratore.setRecensioni(((RispostaRecensioniERecensibilita) msg).getRecensioni());
                homeLettore.setRecensibile(((RispostaRecensioniERecensibilita) msg).isRecensibile());
                homeLettore.setRecensioni(((RispostaRecensioniERecensibilita) msg).getRecensioni());
                // Aggiorna le recensioni e la recensibilità nella vista
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaRecensioniERecensibilita) msg).getMessaggioerrore());
                homeGenitore.mostraErrore(((RispostaRecensioniERecensibilita) msg).getMessaggioerrore());
                homeAmministratore.mostraErrore(((RispostaRecensioniERecensibilita) msg).getMessaggioerrore());
                homeLettore.mostraErrore(((RispostaRecensioniERecensibilita) msg).getMessaggioerrore());
            }
            homeGenitore.setAttendi(false);
            homeAmministratore.setAttendi(false);
            homeLettore.setAttendi(false);
        }

        // gestisce messaggio di risposta dal server di registrazione nuovo lettore e aggiorna la vista corrispondente
        if (msg instanceof RispostaNuovoLettore) {
            if (((RispostaNuovoLettore) msg).isSuccesso()) {
                registraNewLettore.mostraSuccesso("Nuovo lettore registrato!");
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoLettore) msg).getMessaggioErrore());
                registraNewLettore.mostraErrore(((RispostaNuovoLettore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di registrazione nuovo amministratore e aggiorna la vista corrispondente
        if (msg instanceof RispostaNuovoAmministratore) {
            if (((RispostaNuovoAmministratore) msg).isSuccesso()) {
                registraNewAmministratore.mostraSuccesso("Nuovo amministratore registrato!");
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoAmministratore) msg).getMessaggioErrore());
                registraNewAmministratore.mostraErrore(((RispostaNuovoAmministratore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiornamento profilo lettore e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiornaLettore) {
            if (((RispostaAggiornaLettore) msg).isSuccesso()) {
                profiloLettore.getLettore().setNome(((RispostaAggiornaLettore) msg).getLettore().getNome());
                profiloLettore.getLettore().setCognome(((RispostaAggiornaLettore) msg).getLettore().getCognome());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Lettore");
                    alert.setHeaderText("Dati Lettore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiornaLettore) msg).getMessaggioErrore());
                profiloLettore.mostraErrore(((RispostaAggiornaLettore) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di aggiornamento profilo amministratore e aggiorna la vista corrispondente
        if (msg instanceof RispostaAggiornaAmministratore) {
            if (((RispostaAggiornaAmministratore) msg).isSuccesso()) {
                profiloAmministratore.getAmministratore().setNome(((RispostaAggiornaAmministratore) msg).getAmministratore().getNome());
                profiloAmministratore.getAmministratore().setCognome(((RispostaAggiornaAmministratore) msg).getAmministratore().getCognome());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Amministratore");
                    alert.setHeaderText("Dati Amministratore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

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
                });
                if (eventoViewLettore.getLettore().getId() == (((RispostaSalvaEvento) msg).getEvento().getCreatore().getId())) {
                    eventoViewLettore.getLettore().aggiungiEventiCreati(((RispostaSalvaEvento) msg).getEvento());
                }
                if (((RispostaSalvaEvento) msg).getEvento().isIscritto(eventoViewLettore.getLettore())) {
                    eventoViewLettore.getLettore().aggiungiIscrizioneLettura(((RispostaSalvaEvento) msg).getEvento());
                }
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
            if (((RispostaAggiungiRecensione) msg).isSuccesso()) {
                libroDetailedView.aggiornaRecensioni(((RispostaAggiungiRecensione) msg).getRecensione());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Aggiornamento Recensione");
                    alert.setHeaderText("Aggiunto nuova recensione!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiungiRecensione) msg).getMessaggioErrore());
                libroDetailedView.mostraErrore(((RispostaAggiungiRecensione) msg).getMessaggioErrore());
            }
        }

        // gestisce messaggio di risposta dal server di cancellazione recensione e aggiorna la vista corrispondente
        if (msg instanceof RispostaCancellaRecensione) {
            if (((RispostaCancellaRecensione) msg).isSuccesso()) {
                libroDetailedView.cancellaRecensione(((RispostaCancellaRecensione) msg).getId());
                System.out.println("Cancellata recensione con ID: " + ((RispostaCancellaRecensione) msg).getId());
                libroDetailedView.mostraErrore2("Cancellata recensione con ID: " + ((RispostaCancellaRecensione) msg).getId());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                    alert.setTitle("Cancellazione Recensione");
                    alert.setHeaderText("Recensione cancellata correttamente!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });
            } else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaCancellaRecensione) msg).getMessaggioErrore());
                libroDetailedView.mostraErrore2(((RispostaCancellaRecensione) msg).getMessaggioErrore());
            }
            libroDetailedView.setAttendi(false);
        }

        // gestisce notifica di aggiornamento evento e aggiorna i dati nella view e la vista corrispondente
        if (msg instanceof NotificaAggiornamentoEvento) {
            Stage stage = loginView.getStage();
            Scene scene = stage.getScene();

            // Verifica se vista attualmente visualizzata è compatibile con un refresh
            boolean viewCoerente = false;
            if (stage != null && stage.isShowing()) {
                if (utente instanceof Genitore) {
                    if (scene == homeGenitore.getScene()) {
                        viewCoerente = true;
                    }
                }
                if (utente instanceof Lettore) {
                    if (scene == homeLettore.getScene()) {
                        viewCoerente = true;
                    }}
            }

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
                alert.setTitle("Aggiornamento Live Evento");
                alert.setHeaderText("Ricevuto aggiornamento evento: "
                        + ((NotificaAggiornamentoEvento) msg).getEvento().getNome() + " con successo!");
                alert.setContentText(null);
                alert.showAndWait();
            });
            if (utente instanceof Genitore) {
                for (Figlio f : ((Genitore) utente).getFigli()) {
                    if (f.isIscritto(((NotificaAggiornamentoEvento) msg).getEvento())) {
                        f.aggiornaEvento(((NotificaAggiornamentoEvento) msg).getEvento());
                    }
                }
                for (Evento e : homeGenitore.getEventiProssimi()) {
                    if (e.getId() == ((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                        homeGenitore.getEventiProssimi().set(homeGenitore.getEventiProssimi().indexOf(e),
                                ((NotificaAggiornamentoEvento) msg).getEvento());
                    }
                }

                if (eventoView.getScene()!=null && eventoView.getScene()==stage.getScene() && eventoView.getEvento().getId()==((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                    eventoView.aggiornaEvento(((NotificaAggiornamentoEvento) msg).getEvento());
                }

                if (viewCoerente) {
                    homeGenitore.aggiornaEventi(homeGenitore.getEventiProssimi());
                }
            }
            if (utente instanceof Lettore) {
                if (((NotificaAggiornamentoEvento) msg).getEvento().getCreatore().getId() == utente.getId()) {
                    for (Evento e : homeLettore.getLettore().getEventiCreati()) {
                        if (e.getId() == ((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                            e.aggiornaEvento(((NotificaAggiornamentoEvento) msg).getEvento());
                        }
                    }
                }
                if (((NotificaAggiornamentoEvento) msg).getEvento().isIscritto(homeLettore.getLettore())) {
                    homeLettore.getLettore().aggiungiIscrizioneLettura(((NotificaAggiornamentoEvento) msg).getEvento());
                }
                if (!((NotificaAggiornamentoEvento) msg).getEvento().isIscritto(homeLettore.getLettore())) {
                    homeLettore.getLettore().rimuoviIscrizioneLettura(((NotificaAggiornamentoEvento) msg).getEvento());
                }
                for (Evento e : homeLettore.getEventiProssimi()) {
                    if (e.getId() == ((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                        homeLettore.getEventiProssimi().set(homeLettore.getEventiProssimi().indexOf(e),
                                ((NotificaAggiornamentoEvento) msg).getEvento());
                    }
                }

                if (eventoViewLettore.getScene()!=null && eventoViewLettore.getScene()==stage.getScene() && eventoViewLettore.getEvento().getId()==((NotificaAggiornamentoEvento) msg).getEvento().getId()) {
                    eventoViewLettore.mostraErrore("Aggiornamento da altro utente: la copia su cui lavori è non aggiornata. \n" +
                            "Il tuo salvataggio potrebbe sovrascrivere le modifiche altrui.");
                }

                if (viewCoerente) {
                        homeLettore.aggiornaEventi(homeLettore.getEventiProssimi());
                }
            }
        }
    }

    /** Chiude le risorse del client.
     */
    private void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Invia un messaggio al server.
     *
     * @param msg Il messaggio da inviare.
     * @throws IOException In caso di errore durante l'invio.
     */
    public void sendMessage(Messaggio msg) throws IOException {
        out.writeObject(msg);
        out.flush();
        out.reset();
    }

}
