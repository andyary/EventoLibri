package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.View.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

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
    // add altre viste qui
    private ArrayList<Evento> eventi;
    private Utente utente;



    public void start(LoginView loginView, HomeGenitore homeGenitore, HomeAmministratore homeAmministratore,
                      EventoView eventoView, HomeLettore homeLettore, EventoViewLettore eventoViewLettore,
                      ProfiloGenitore profiloGenitore, ProfiloLettore profiloLettore,
                      ProfiloAmministratore profiloAmministratore,
                      RegistraNewGenitore registraNewGenitore, RegistraNewLettore registraNewLettore,
                      RegistraNewAmministratore registraNewAmministratore) throws Exception {
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

    }

    public void startListening() {
        Thread listenerThread = new Thread(() -> {
            try {
                Messaggio msg;
                while ((msg = (Messaggio) in.readObject()) != null) { // leggi finché c'è un messaggio
                    handleMessage(msg); // chiama la tua funzione per gestire il messaggio
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close();
            }
        });
        listenerThread.start();
    }



    private void handleMessage(Messaggio msg) {
        // Gestisci il messaggio ricevuto dal server
        System.out.println("Messaggio ricevuto dal server: " + msg);
        if (msg instanceof RispostaLogin) {
            if (((RispostaLogin) msg).isSuccesso()) {
                System.out.println("Login riuscito!");
                switch (((RispostaLogin) msg).getUtente()) {
                    case Genitore gen -> {
                        CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                        Genitore genitore = creaGenitore.nuovoUtente(gen.getId(), gen.getNome(),gen.getCognome(), gen.getUserName());
                        genitore.setFigli(gen.getFigli());
                        utente = genitore;
                        homeGenitore.show(loginView.getStage(), genitore, ((RispostaLogin) msg).getProssimiEventi(),
                                () -> {loginView.show(loginView.getStage());
                        });
                    }
                    case Lettore let -> {
                        CreaUtente<Lettore> creaLettore = new CreaLettore();
                        Lettore lettore = creaLettore.nuovoUtente(let.getId(), let.getNome(),let.getCognome(), let.getUserName());
                        lettore.setIscrizioniLettura(let.getIscrizioniLettura());
                        lettore.setEventiCreati(let.getEventiCreati());
                        utente = lettore;
                        homeLettore.show(loginView.getStage(), lettore, ((RispostaLogin) msg).getProssimiEventi(),
                        () -> {loginView.show(loginView.getStage());
                        });
                    }

                    case Amministratore amm -> {
                        CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore();
                        Amministratore amministratore = creaAmministratore.nuovoUtente(amm.getId(), amm.getNome(),amm.getCognome(), amm.getUserName());
                        utente = amministratore;
                        homeAmministratore.show(loginView.getStage(), amministratore,
                                () -> {loginView.show(loginView.getStage());
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

        if (msg instanceof RispostaIscrizioneEvento) {
            if (((RispostaIscrizioneEvento) msg).isSuccesso()) {
                for (Figlio f: eventoView.getGenitore().getFigli()) {
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
                System.out.println("Iscritto " + ((RispostaIscrizioneEvento) msg).getFiglio().getNome());}
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaIscrizioneEvento) msg).getMessaggioErrore());
                eventoView.mostraErrore(((RispostaIscrizioneEvento) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaIscrittiEvento) {
            if (((RispostaIscrittiEvento) msg).isSuccesso()) {
                if (utente instanceof Genitore) {
                    eventoView.aggiornaIscritti(((RispostaIscrittiEvento) msg).getEvento().getIscritti());
                }


                System.out.println("Numero iscritti aggiornato: " + ((RispostaIscrittiEvento) msg).getEvento().getIscritti());
            }
        }

        if (msg instanceof RispostaDisiscrizioneEvento) {
            if (((RispostaDisiscrizioneEvento) msg).isSuccesso()) {
                for (Figlio f: eventoView.getGenitore().getFigli()) {
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
                System.out.println("Disiscritto " + ((RispostaDisiscrizioneEvento) msg).getFiglio().getNome());}
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaDisiscrizioneEvento) msg).getMessaggioErrore());
                eventoView.mostraErrore(((RispostaDisiscrizioneEvento) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaAggiornaGenitore) {
            if (((RispostaAggiornaGenitore) msg).isSuccesso()) {
                profiloGenitore.getGenitore().setNome(((RispostaAggiornaGenitore) msg).getGenitore().getNome());
                profiloGenitore.getGenitore().setCognome(((RispostaAggiornaGenitore) msg).getGenitore().getCognome());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"", ButtonType.OK);
                    alert.setTitle("Aggiornamento Genitore");
                    alert.setHeaderText("Dati Genitore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiornaGenitore) msg).getMessaggioErrore());
                profiloGenitore.mostraErrore(((RispostaAggiornaGenitore) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaAggiungiFiglio) {
            if (((RispostaAggiungiFiglio) msg).isSuccesso()) {
                profiloGenitore.getGenitore().aggiungiFiglio((((RispostaAggiungiFiglio) msg).getNuovoFiglio()));
                profiloGenitore.aggiornaFigli((((RispostaAggiungiFiglio) msg).getNuovoFiglio()));

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"", ButtonType.OK);
                    alert.setTitle("Aggiornamento Genitore");
                    alert.setHeaderText("Aggiunto nuovo figlio!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiungiFiglio) msg).getMessaggioErrore());
                profiloGenitore.mostraErrore2(((RispostaAggiungiFiglio) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaNuovoGenitore) {
            if (((RispostaNuovoGenitore) msg).isSuccesso()) {
                registraNewGenitore.mostraSuccesso("Nuovo genitore registrato!");
            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoGenitore) msg).getMessaggioErrore());
                registraNewGenitore.mostraErrore(((RispostaNuovoGenitore) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaLettoriELuoghiELibri) {
            if (((RispostaLettoriELuoghiELibri) msg).isSuccesso()) {
                eventoViewLettore.aggiornaLettoriELuoghiELibri(((RispostaLettoriELuoghiELibri) msg).getLettori(),
                        ((RispostaLettoriELuoghiELibri) msg).getLuoghi(), ((RispostaLettoriELuoghiELibri) msg).getElencolibri());
            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaLettoriELuoghiELibri) msg).getMessaggioerrore());
                eventoViewLettore.mostraErrore(((RispostaLettoriELuoghiELibri) msg).getMessaggioerrore());
            }

        }

        if (msg instanceof RispostaNuovoLettore) {
            if (((RispostaNuovoLettore) msg).isSuccesso()) {
                registraNewLettore.mostraSuccesso("Nuovo lettore registrato!");
            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoLettore) msg).getMessaggioErrore());
                registraNewGenitore.mostraErrore(((RispostaNuovoLettore) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaNuovoAmministratore) {
            if (((RispostaNuovoAmministratore) msg).isSuccesso()) {
                registraNewAmministratore.mostraSuccesso("Nuovo amministratore registrato!");
            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaNuovoAmministratore) msg).getMessaggioErrore());
                registraNewAmministratore.mostraErrore(((RispostaNuovoAmministratore) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaAggiornaLettore) {
            if (((RispostaAggiornaLettore) msg).isSuccesso()) {
                profiloLettore.getLettore().setNome(((RispostaAggiornaLettore) msg).getLettore().getNome());
                profiloLettore.getLettore().setCognome(((RispostaAggiornaLettore) msg).getLettore().getCognome());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"", ButtonType.OK);
                    alert.setTitle("Aggiornamento Lettore");
                    alert.setHeaderText("Dati Lettore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiornaLettore) msg).getMessaggioErrore());
                profiloLettore.mostraErrore(((RispostaAggiornaLettore) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaAggiornaAmministratore) {
            if (((RispostaAggiornaAmministratore) msg).isSuccesso()) {
                profiloAmministratore.getAmministratore().setNome(((RispostaAggiornaAmministratore) msg).getAmministratore().getNome());
                profiloAmministratore.getAmministratore().setCognome(((RispostaAggiornaAmministratore) msg).getAmministratore().getCognome());

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"", ButtonType.OK);
                    alert.setTitle("Aggiornamento Amministratore");
                    alert.setHeaderText("Dati Amministratore aggiornati!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });

            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaAggiornaAmministratore) msg).getMessaggioErrore());
                profiloAmministratore.mostraErrore(((RispostaAggiornaAmministratore) msg).getMessaggioErrore());
            }
        }

        if (msg instanceof RispostaSalvaEvento) {
            if (((RispostaSalvaEvento) msg).isSuccesso()) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"", ButtonType.OK);
                    alert.setTitle("Salvataggio Evento");
                    alert.setHeaderText("Evento salvato con successo!");
                    alert.setContentText(null);
                    alert.showAndWait();
                });
                eventoViewLettore.setEvento(((RispostaSalvaEvento) msg).getEvento());
            }
            else {
                System.out.println("Messaggio di errore ricevuto : " + ((RispostaSalvaEvento) msg).getMessaggioErrore());
                eventoViewLettore.mostraErrore(((RispostaSalvaEvento) msg).getMessaggioErrore());
            }
        }
    }

    private void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Messaggio msg) throws IOException {
        out.writeObject(msg);
        out.flush();
        out.reset();
    }

}
