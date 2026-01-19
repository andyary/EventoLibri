package it.polimi.eventolibri.Network;

import it.polimi.eventolibri.Model.Utente;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
/** Classe che rappresenta il server dell'applicazione Eventolibri.
 * Si occupa di accettare connessioni dai client e gestire le comunicazioni.
 */
public class Server {
    // mappa dei client connessi e i relativi utenti
    private HashMap<ClientHandler, Utente> clients= new HashMap<>();
    private ServerSocket serverSocket;

    /** Avvia il server sulla porta specificata e ascolta le connessioni dei client.
     *
     * @param port la porta su cui ascoltare
     * @throws IOException se si verifica un errore di I/O durante l'apertura del socket
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

    /** Metodo principale per avviare il server.
     *
     * @param args argomenti della riga di comando
     * @throws IOException se si verifica un errore di I/O
     */
    public static void main(String[] args) throws IOException {
        new Server().start(5000);
    }

    /** Restituisce la mappa dei client connessi e i relativi utenti.
     *
     * @return mappa dei client e utenti
     */
    public HashMap<ClientHandler, Utente> getClients() {
        return clients;
    }
}
