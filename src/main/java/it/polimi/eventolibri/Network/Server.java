package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Model.Utente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
/**
 * Server class that listens for client connections and manages connected clients.
 */
public class Server {
    // mappa dei client connessi e i relativi utenti
    private HashMap<ClientHandler, Utente> clients= new HashMap<>();

    private ServerSocket serverSocket;
    /**
     * Starts the server on the specified port and listens for client connections.
     *
     * @param port the port number to listen on
     * @throws IOException if an I/O error occurs when opening the socket
     */
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

    /**
     * Main method to start the server.
     *
     * @param args command line arguments
     * @throws IOException if an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        new Server().start(5000);
    }

    /**
     * Returns the map of connected clients and their associated users.
     *
     * @return a HashMap of ClientHandler and Utente
     */
    public HashMap<ClientHandler, Utente> getClients() {
        return clients;
    }
}
