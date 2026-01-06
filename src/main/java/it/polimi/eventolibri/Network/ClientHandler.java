package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Controller.Controller;
import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Controller controller;
    private Utente utente;
    private Server server;

    public Utente getUtente() {
        return utente;
    }

    public ClientHandler(Socket socket, Server server) {
        this.server = server;
        this.socket = socket;
        this.controller = new Controller();

    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Messaggio msg = (Messaggio) in.readObject();
                handleMessage(msg);
            }
        } catch (Exception e) {
            System.out.println("Client disconnesso: " + socket);
        }

    }

    private void handleMessage(Messaggio msg) throws IOException {
        // esempio di DAO
        if (msg instanceof RichiestaLogin) {
            RispostaLogin risposta = controller.controllaLogin(((RichiestaLogin) msg).getUsername(), ((RichiestaLogin) msg).getPassword());
            if (risposta.isSuccesso()){
                if (risposta.getUtente() instanceof Genitore) {
                    CreaUtente<Genitore> creaUtente = new CreaGenitore();
                    this.utente = creaUtente.nuovoUtente(risposta.getUtente().getId(),risposta.getUtente().getNome(),risposta.getUtente().getCognome(),risposta.getUtente().getUserName());
                }
                if (risposta.getUtente() instanceof Lettore) {
                    CreaUtente<Lettore> creaUtente = new CreaLettore();
                    this.utente = creaUtente.nuovoUtente(risposta.getUtente().getId(),risposta.getUtente().getNome(),risposta.getUtente().getCognome(),risposta.getUtente().getUserName());
                }
                if (risposta.getUtente() instanceof Amministratore) {
                    CreaUtente<Amministratore> creaUtente = new CreaAmministratore();
                    this.utente = creaUtente.nuovoUtente(risposta.getUtente().getId(),risposta.getUtente().getNome(),risposta.getUtente().getCognome(),risposta.getUtente().getUserName());
                }
                server.getClients().put(this, this.utente);
            }
            sendMessage(risposta);
        }

        if (msg instanceof CloseUI) {
            server.getClients().remove(this);
            socket.close();
            System.out.println("Connessione chiusa con il client: " + socket);
        }

        if (msg instanceof RichiestaNextEventi) {
            // gestisci la richiesta di next eventi
            Evento ultimoEvento = ((RichiestaNextEventi) msg).getUltimoEvento();
            RispostaNextEventi risposta = controller.getNextEventi(ultimoEvento);
            sendMessage(risposta);
        }

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

        if (msg instanceof RichiestaIscrittiEvento) {
            RispostaIscrittiEvento risposta = controller.getIscrittiEvento(((RichiestaIscrittiEvento) msg).getEvento());
            sendMessage(risposta);
        }

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

        if (msg instanceof RichiestaAggiornaGenitore) {
            Genitore genitore = ((RichiestaAggiornaGenitore) msg).getGenitore();
            RispostaAggiornaGenitore risposta = controller.aggiornaGenitore(genitore);
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaAggiungiFiglio) {
            Genitore genitore = ((RichiestaAggiungiFiglio) msg).getGenitore();
            Figlio nuovoFiglio = ((RichiestaAggiungiFiglio) msg).getFiglioNuovo();
            RispostaAggiungiFiglio risposta = controller.aggiungiFiglio(genitore, nuovoFiglio);
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaNuovoGenitore) {
            Genitore nuovoGenitore = ((RichiestaNuovoGenitore) msg).getNuovoGenitore();
            RispostaNuovoGenitore risposta = controller.registraNuovoGenitore(nuovoGenitore, ((RichiestaNuovoGenitore) msg).getPassword());
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaNuovoLettore) {
            Lettore nuovoLettore = ((RichiestaNuovoLettore) msg).getNuovoLettore();
            RispostaNuovoLettore risposta = controller.registraNuovoLettore(nuovoLettore, ((RichiestaNuovoLettore) msg).getPassword());
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaNuovoAmministratore) {
            Amministratore nuovoAmministratore = ((RichiestaNuovoAmministratore) msg).getNuovoAmministratore();
            RispostaNuovoAmministratore risposta = controller.registraNuovoAmministratore(nuovoAmministratore, ((RichiestaNuovoAmministratore) msg).getPassword());
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaLettoriELuoghiELibri) {
            RispostaLettoriELuoghiELibri risposta = controller.richiestaLettoriELuoghiELibri();
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaAggiornaLettore) {
            Lettore lettore = ((RichiestaAggiornaLettore) msg).getLettore();
            RispostaAggiornaLettore risposta = controller.aggiornaLettore(lettore);
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaAggiornaAmministratore) {
            Amministratore amministratore = ((RichiestaAggiornaAmministratore) msg).getAmministratore();
            RispostaAggiornaAmministratore risposta = controller.aggiornaAmministratore(amministratore);
            sendMessage(risposta);
        }

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

        if (msg instanceof RichiestaRecensioniERecensibilita) {
            RispostaRecensioniERecensibilita risposta = controller.richiestaRecensioniERecensibilita(
                    ((RichiestaRecensioniERecensibilita) msg).getLibro(),
                    ((RichiestaRecensioniERecensibilita) msg).getUtente()
            );
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaAggiungiRecensione) {
            Recensione recensione = ((RichiestaAggiungiRecensione) msg).getRecensione();
            RispostaAggiungiRecensione risposta = controller.aggiungiRecensione(recensione);
            sendMessage(risposta);
        }

        if (msg instanceof RichiestaCancellaRecensione) {
            Recensione recensione = ((RichiestaCancellaRecensione) msg).getRecensione();
            RispostaCancellaRecensione risposta = controller.cancellaRecensione(recensione);
            sendMessage(risposta);
        }

        // QUI CONTINUI AD AGGIUNGERE I MESSAGGI
    }

    public void sendMessage(Messaggio msg) throws IOException {
        out.writeObject(msg);
        out.flush();
        out.reset();
    }

}
