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

    public ClientHandler(Socket socket) {
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
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }
        if (msg instanceof RichiestaNextEventi) {
            // gestisci la richiesta di next eventi
            Evento ultimoEvento = ((RichiestaNextEventi) msg).getUltimoEvento();
            RispostaNextEventi risposta = controller.getNextEventi(ultimoEvento);
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaIscrizioneEvento) {
            Evento evento = ((RichiestaIscrizioneEvento) msg).getEvento();
            Figlio figlio = ((RichiestaIscrizioneEvento) msg).getFiglio();
            Genitore genitore = ((RichiestaIscrizioneEvento) msg).getGenitore();
            RispostaIscrizioneEvento risposta = controller.iscriviFiglioEvento(figlio, evento, genitore);
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaIscrittiEvento) {
            RispostaIscrittiEvento risposta = controller.getIscrittiEvento(((RichiestaIscrittiEvento) msg).getEvento());
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaDisiscrizioneEvento) {
            Evento evento = ((RichiestaDisiscrizioneEvento) msg).getEvento();
            Figlio figlio = ((RichiestaDisiscrizioneEvento) msg).getFiglio();
            Genitore genitore = ((RichiestaDisiscrizioneEvento) msg).getGenitore();
            RispostaDisiscrizioneEvento risposta = controller.disiscriviFiglioEvento(figlio, evento, genitore);
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaAggiornaGenitore) {
            Genitore genitore = ((RichiestaAggiornaGenitore) msg).getGenitore();
            RispostaAggiornaGenitore risposta = controller.aggiornaGenitore(genitore);
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaAggiungiFiglio) {
            Genitore genitore = ((RichiestaAggiungiFiglio) msg).getGenitore();
            Figlio nuovoFiglio = ((RichiestaAggiungiFiglio) msg).getFiglioNuovo();
            RispostaAggiungiFiglio risposta = controller.aggiungiFiglio(genitore, nuovoFiglio);
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaNuovoGenitore) {
            Genitore nuovoGenitore = ((RichiestaNuovoGenitore) msg).getNuovoGenitore();
            RispostaNuovoGenitore risposta = controller.registraNuovoGenitore(nuovoGenitore, ((RichiestaNuovoGenitore) msg).getPassword());
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaNuovoLettore) {
            Lettore nuovoLettore = ((RichiestaNuovoLettore) msg).getNuovoLettore();
            RispostaNuovoLettore risposta = controller.registraNuovoLettore(nuovoLettore, ((RichiestaNuovoLettore) msg).getPassword());
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaNuovoAmministratore) {
            Amministratore nuovoAmministratore = ((RichiestaNuovoAmministratore) msg).getNuovoAmministratore();
            RispostaNuovoAmministratore risposta = controller.registraNuovoAmministratore(nuovoAmministratore, ((RichiestaNuovoAmministratore) msg).getPassword());
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaLettoriELuoghiELibri) {
            RispostaLettoriELuoghiELibri risposta = controller.richiestaLettoriELuoghiELibri();
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaAggiornaLettore) {
            Lettore lettore = ((RichiestaAggiornaLettore) msg).getLettore();
            RispostaAggiornaLettore risposta = controller.aggiornaLettore(lettore);
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        if (msg instanceof RichiestaAggiornaAmministratore) {
            Amministratore amministratore = ((RichiestaAggiornaAmministratore) msg).getAmministratore();
            RispostaAggiornaAmministratore risposta = controller.aggiornaAmministratore(amministratore);
            out.writeObject(risposta);
            out.flush();
            out.reset();
        }

        // QUI CONTINUI AD AGGIUNGERE I MESSAGGI
    }

}
