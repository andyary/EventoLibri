package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Message.Messaggio;
import it.polimi.eventolibri.Message.RichiestaLogin;
import it.polimi.eventolibri.Message.RispostaLogin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void start() throws Exception {
        socket = new Socket("localhost", 5000);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        // esempio: chiedo eventi
        out.writeObject(new RichiestaLogin());
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
            System.out.println("Ricevuto risposta login ");
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

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.start();
        client.startListening();
        client.close();

    }

}
