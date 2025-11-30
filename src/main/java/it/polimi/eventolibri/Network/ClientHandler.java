package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Controller.Controller;
import it.polimi.eventolibri.Message.Messaggio;
import it.polimi.eventolibri.Message.RichiestaLogin;
import it.polimi.eventolibri.Message.RispostaLogin;

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
        }
        // QUI CONTINUI AD AGGIUNGERE I MESSAGGI
    }

}
