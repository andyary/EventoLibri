package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.CloseUI;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

/** Classe principale per l'avvio dell'applicazione JavaFX.
 * Estende la classe Application di JavaFX.
 */
public class Window extends Application {

    /**
     * Avvia l'applicazione JavaFX.
     *
     * @param stage il palco primario per questa applicazione
     * @throws Exception se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void start(Stage stage) throws Exception {

        Client client = new Client();
        RegistraNewGenitore registraNewGenitore = new RegistraNewGenitore(client);
        RegistraNewLettore registraNewLettore = new RegistraNewLettore(client);
        RegistraNewAmministratore registraNewAmministratore = new RegistraNewAmministratore(client);
        EventoView eventoView = new EventoView(client);
        EventoViewLettore eventoViewLettore = new EventoViewLettore(client);
        ProfiloGenitore profiloGenitore = new ProfiloGenitore(client);
        ProfiloLettore profiloLettore = new ProfiloLettore(client);
        ProfiloAmministratore profiloAmministratore = new ProfiloAmministratore(client);
        LibroDetailedView libroDetailedView = new LibroDetailedView(client);
        HomeGenitore homeGenitore = new HomeGenitore(client, eventoView, profiloGenitore, libroDetailedView);
        HomeLettore homeLettore = new HomeLettore(client, eventoViewLettore, profiloLettore, libroDetailedView);
        HomeAmministratore homeAmministratore = new HomeAmministratore(client, profiloAmministratore, registraNewLettore, registraNewAmministratore, libroDetailedView);
        LoginView loginView = new LoginView(client, registraNewGenitore, homeGenitore, homeLettore, homeAmministratore);
        try {
            client.start(loginView, homeGenitore, homeAmministratore, eventoView, homeLettore, eventoViewLettore,
                    profiloGenitore, profiloLettore, profiloAmministratore,
                    registraNewGenitore, registraNewLettore, registraNewAmministratore, libroDetailedView);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        client.startListening();
        loginView.show(stage);

        // Gestione della chiusura della finestra
        // Invia un messaggio di chiusura al server prima di chiudere l'applicazione
        // Utilizza un thread separato per evitare di bloccare il thread dell'interfaccia utente
        // durante l'invio del messaggio
        // Dopo aver inviato il messaggio, chiude effettivamente la finestra sul thread JavaFX
        // Utilizza Platform.runLater per eseguire la chiusura della finestra sul thread JavaFX
        // Questo garantisce che tutte le operazioni dell'interfaccia utente siano eseguite correttamente
        // senza causare problemi di concorrenza

        stage.setOnCloseRequest(event -> {
            event.consume(); // blocca temporaneamente la chiusura
            new Thread(() -> {
                try {
                    client.sendMessage(new CloseUI());
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
                // chiudi davvero la finestra sul FX thread
                Platform.runLater(() -> stage.close());
            }).start();
        });


    }

    /** Metodo principale per avviare l'applicazione JavaFX (Front End).
     *
     * @param args argomenti della riga di comando
     */
    public static void main(String[] args) {
        launch(args);
    }



}
