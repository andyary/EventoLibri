package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Controller.Controller;
import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/** Il ClientHandler gestisce la comunicazione con un client connesso.
 * Riceve i messaggi dal client, li elabora e invia le risposte appropriate.
 * Ogni client connesso ha una propria istanza di ClientHandler che gira in un thread separato.
 */
public class ClientHandler extends Thread {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Controller controller;
    private Utente utente;
    private Server server;

    /** Restituisce l'utente associato a questo client handler.
     *
     * @return oggetto Utente
     */
    public Utente getUtente() {
        return utente;
    }

    /** Costruttore della classe ClientHandler per un dato socket e server.
     *
     * @param socket socket connesso al client
     * @param server istanza del server
     */
    public ClientHandler(Socket socket, Server server) {
        this.server = server;
        this.socket = socket;
        this.controller = new Controller();

    }


    /** Avvoia il thread per gestire la comunicazione con il client. */
        @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream()); // crea il flusso di output per inviare messaggi al client
            in = new ObjectInputStream(socket.getInputStream()); // crea il flusso di input per ricevere messaggi dal client
            while (true) {
                Messaggio msg = (Messaggio) in.readObject(); // legge il messaggio dal client
                handleMessage(msg); // gestisce il messaggio ricevuto
            }
        } catch (Exception e) {
            System.out.println("Client disconnesso: " + socket);
        }

    }

    /** Gestisce i messaggi ricevuti dal client e invia le risposte appropriate.
     *
     * @param msg il messaggio ricevuto dal client
     * @throws IOException se si verifica un errore di I/O
     */
    private void handleMessage(Messaggio msg) throws IOException {
        System.out.println("Messaggio ricevuto dal client: " + msg);

        // gestisce messaggio di richiesta login dal client
        if (msg instanceof RichiestaLogin) {
            RispostaLogin risposta = controller.controllaLogin(((RichiestaLogin) msg).getUsername(), ((RichiestaLogin) msg).getPassword());
            if (risposta.isSuccesso()){
                // caso genitore
                if (risposta.getUtente() instanceof Genitore) {
                    CreaUtente<Genitore> creaUtente = new CreaGenitore();
                    this.utente = creaUtente.nuovoUtente(risposta.getUtente().getId(),risposta.getUtente().getNome(),risposta.getUtente().getCognome(),risposta.getUtente().getUserName());
                }
                // caso lettore
                if (risposta.getUtente() instanceof Lettore) {
                    CreaUtente<Lettore> creaUtente = new CreaLettore();
                    this.utente = creaUtente.nuovoUtente(risposta.getUtente().getId(),risposta.getUtente().getNome(),risposta.getUtente().getCognome(),risposta.getUtente().getUserName());
                }
                // caso amministratore
                if (risposta.getUtente() instanceof Amministratore) {
                    CreaUtente<Amministratore> creaUtente = new CreaAmministratore();
                    this.utente = creaUtente.nuovoUtente(risposta.getUtente().getId(),risposta.getUtente().getNome(),risposta.getUtente().getCognome(),risposta.getUtente().getUserName());
                }
                server.getClients().put(this, this.utente);  // aggiunge il client handler alla mappa dei client connessi
            }
            sendMessage(risposta); // invia la risposta di login al client
        }

        // gestisce messaggio di chiusura connessione dal client
        if (msg instanceof CloseUI) {
            server.getClients().remove(this); // rimuove il client handler dalla mappa dei client connessi
            socket.close(); // chiude la connessione con il client
            System.out.println("Connessione chiusa con il client: " + socket); // log di chiusura
        }

        // gestisce la richiesta dei prossimi eventi
        if (msg instanceof RichiestaNextEventi) {
            // gestisci la richiesta di next eventi
            Evento ultimoEvento = ((RichiestaNextEventi) msg).getUltimoEvento();
            RispostaNextEventi risposta = controller.getNextEventi(ultimoEvento);
            sendMessage(risposta);
        }

        // gestisce la richiesta di iscrizione di un figlio ad un evento, implementa il pattern observer per notificare ai listeners connessi
        if (msg instanceof RichiestaIscrizioneEvento) {
            Evento evento = ((RichiestaIscrizioneEvento) msg).getEvento();
            Figlio figlio = ((RichiestaIscrizioneEvento) msg).getFiglio();
            Genitore genitore = ((RichiestaIscrizioneEvento) msg).getGenitore();
            RispostaIscrizioneEvento risposta = controller.iscriviFiglioEvento(figlio, evento, genitore);
            sendMessage(risposta);
            if (risposta.isSuccesso()) {
                // Implementazione Pattern Observer
                for (Listener l: evento.getListeners()) {
                    for (ClientHandler ch: server.getClients().keySet()) {
                        if (ch.getUtente().getId() == l.getId() && /*l.getId()!= utente.getId()*/ ch != this) {
                            NotificaAggiornamentoEvento notifica = new NotificaAggiornamentoEvento(evento);
                            ch.sendMessage(notifica);
                        }
                    }
                }
            }
        }

        // gestisce la richiesta del numero di iscritti e dei listeners (genitori) associati alle iscrizioni ad un evento
        if (msg instanceof RichiestaIscrittiEvento) {
            RispostaIscrittiEvento risposta = controller.getIscrittiEvento(((RichiestaIscrittiEvento) msg).getEvento());
            sendMessage(risposta);
        }

        // gestisce la richiesta di disiscrizione di un figlio da un evento, implementa il pattern observer per notificare ai listeners connessi
        if (msg instanceof RichiestaDisiscrizioneEvento) {
            Evento evento = ((RichiestaDisiscrizioneEvento) msg).getEvento();
            Figlio figlio = ((RichiestaDisiscrizioneEvento) msg).getFiglio();
            Genitore genitore = ((RichiestaDisiscrizioneEvento) msg).getGenitore();
            RispostaDisiscrizioneEvento risposta = controller.disiscriviFiglioEvento(figlio, evento, genitore);
            sendMessage(risposta);
            if (risposta.isSuccesso()) {
                // Implementazione Pattern Observer
                for (Listener l: evento.getListeners()) {
                    for (ClientHandler ch: server.getClients().keySet()) {
                        if (ch.getUtente().getId() == l.getId() && /*l.getId()!= utente.getId()*/ ch != this) {
                            NotificaAggiornamentoEvento notifica = new NotificaAggiornamentoEvento(evento);
                            ch.sendMessage(notifica);
                        }
                    }
                }
            }
        }

        // gestisce la richiesta di aggiornamento del profilo genitore
        if (msg instanceof RichiestaAggiornaGenitore) {
            Genitore genitore = ((RichiestaAggiornaGenitore) msg).getGenitore();
            RispostaAggiornaGenitore risposta = controller.aggiornaGenitore(genitore);
            sendMessage(risposta);
        }

        // gestisce la richiesta di aggiunta di un figlio al profilo genitore
        if (msg instanceof RichiestaAggiungiFiglio) {
            Genitore genitore = ((RichiestaAggiungiFiglio) msg).getGenitore();
            Figlio nuovoFiglio = ((RichiestaAggiungiFiglio) msg).getFiglioNuovo();
            RispostaAggiungiFiglio risposta = controller.aggiungiFiglio(genitore, nuovoFiglio);
            sendMessage(risposta);
        }

        // gestisce la richiesta di registrazione di un nuovo genitore
        if (msg instanceof RichiestaNuovoGenitore) {
            Genitore nuovoGenitore = ((RichiestaNuovoGenitore) msg).getNuovoGenitore();
            RispostaNuovoGenitore risposta = controller.registraNuovoGenitore(nuovoGenitore, ((RichiestaNuovoGenitore) msg).getPassword());
            sendMessage(risposta);
        }

        // gestisce la richiesta di registrazione di un nuovo lettore
        if (msg instanceof RichiestaNuovoLettore) {
            Lettore nuovoLettore = ((RichiestaNuovoLettore) msg).getNuovoLettore();
            RispostaNuovoLettore risposta = controller.registraNuovoLettore(nuovoLettore, ((RichiestaNuovoLettore) msg).getPassword());
            sendMessage(risposta);
        }

        // gestisce la richiesta di registrazione di un nuovo amministratore
        if (msg instanceof RichiestaNuovoAmministratore) {
            Amministratore nuovoAmministratore = ((RichiestaNuovoAmministratore) msg).getNuovoAmministratore();
            RispostaNuovoAmministratore risposta = controller.registraNuovoAmministratore(nuovoAmministratore, ((RichiestaNuovoAmministratore) msg).getPassword());
            sendMessage(risposta);
        }

        // gestisce la richiesta di lettori, luoghi e libri per la gestione dei menu a tendina
        if (msg instanceof RichiestaLettoriELuoghiELibri) {
            RispostaLettoriELuoghiELibri risposta = controller.richiestaLettoriELuoghiELibri();
            sendMessage(risposta);
        }

        // gestisce la richiesta di aggiornamento del profilo lettore
        if (msg instanceof RichiestaAggiornaLettore) {
            Lettore lettore = ((RichiestaAggiornaLettore) msg).getLettore();
            RispostaAggiornaLettore risposta = controller.aggiornaLettore(lettore);
            sendMessage(risposta);
        }

        // gestisce la richiesta di aggiornamento del profilo amministratore
        if (msg instanceof RichiestaAggiornaAmministratore) {
            Amministratore amministratore = ((RichiestaAggiornaAmministratore) msg).getAmministratore();
            RispostaAggiornaAmministratore risposta = controller.aggiornaAmministratore(amministratore);
            sendMessage(risposta);
        }

        // gestisce la richiesta di salvataggio di un evento, implementa il pattern observer per notificare ai listeners connessi
        if (msg instanceof RichiestaSalvaEvento) {
            Evento evento = ((RichiestaSalvaEvento) msg).getEvento();
            RispostaSalvaEvento risposta = controller.salvaEvento(evento);
            Evento eventoAggiornato = risposta.getEvento();
            sendMessage(risposta);
            // Implementazione Pattern Observer
            if (risposta.isSuccesso()) {
                for (Listener l: eventoAggiornato.getListeners()) {
                    for (ClientHandler ch: server.getClients().keySet()) {
                        if (ch.getUtente().getId() == l.getId() && /*l.getId()!= utente.getId()*/ ch != this) {
                            NotificaAggiornamentoEvento notifica = new NotificaAggiornamentoEvento(eventoAggiornato);
                            ch.sendMessage(notifica);
                        }
                    }
                }
            }
        }

        // gestisce la richiesta di recensioni e recensibilita di un libro
        if (msg instanceof RichiestaRecensioniERecensibilita) {
            RispostaRecensioniERecensibilita risposta = controller.richiestaRecensioniERecensibilita(
                    ((RichiestaRecensioniERecensibilita) msg).getLibro(),
                    ((RichiestaRecensioniERecensibilita) msg).getUtente()
            );
            sendMessage(risposta);
        }

        // gestisce la richiesta di aggiunta di una recensione da parte di un genitore
        if (msg instanceof RichiestaAggiungiRecensione) {
            Recensione recensione = ((RichiestaAggiungiRecensione) msg).getRecensione();
            RispostaAggiungiRecensione risposta = controller.aggiungiRecensione(recensione);
            sendMessage(risposta);
        }

        // gestisce la richiesta di cancellazione di una recensione da parte di un amministratore
        if (msg instanceof RichiestaCancellaRecensione) {
            Recensione recensione = ((RichiestaCancellaRecensione) msg).getRecensione();
            RispostaCancellaRecensione risposta = controller.cancellaRecensione(recensione);
            sendMessage(risposta);
        }
        // QUI CONTINUI AD AGGIUNGERE I NUOVI MESSAGGI
    }

    /** Invia un messaggio al client.
     *
     * @param msg il messaggio da inviare
     * @throws IOException se si verifica un errore di I/O
     */
    public void sendMessage(Messaggio msg) throws IOException {
        out.writeObject(msg); // scrive l'oggetto nel flusso di output
        out.flush(); // forza la scrittura immediata
        out.reset(); // resetta lo stream per evitare problemi di serializzazione con oggetti ripetuti
    }
}
