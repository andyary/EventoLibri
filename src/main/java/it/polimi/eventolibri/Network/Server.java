package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Model.Utente;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private HashMap<ClientHandler, Utente> clients= new HashMap<>();

    private ServerSocket serverSocket;
    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server avviato sulla porta " + port);
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("Nuovo client connesso: " + socket);
            // ogni client ha il suo thread
            ClientHandler clientHandler = new ClientHandler(socket, this);
            clientHandler.start();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server().start(5000);
    }

    public HashMap<ClientHandler, Utente> getClients() {
        return clients;
    }
}
