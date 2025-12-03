package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Message.*;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.View.HomeGenitore;
import it.polimi.eventolibri.View.LoginView;

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
    // add altre viste qui
    private ArrayList<Evento> eventi;
    private Utente utente;



    public void start(LoginView loginView, HomeGenitore homeGenitore) throws Exception {
        socket = new Socket("localhost", 5000);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        this.loginView = loginView;
        this.homeGenitore = homeGenitore;
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
                        homeGenitore.show(loginView.getStage(), genitore, ((RispostaLogin) msg).getProssimiEventi());
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
                if (((RispostaNextEventi) msg).getProssimiEventi().isEmpty()) {
                    homeGenitore.nascondiBottoneNextEventi();
                } else {
                    homeGenitore.aggiornaEventi(((RispostaNextEventi) msg).getProssimiEventi());
                }

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
    }

}
