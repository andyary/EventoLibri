package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Model.Utente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

    // private ArrayList<ClientHandler, Utente> clients;

    private ServerSocket serverSocket;
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Nuovo client connesso: " + socket);
            // ogni client ha il suo thread
            new ClientHandler(socket).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().start(5000);
    }

}
