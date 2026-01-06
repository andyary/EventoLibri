package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.CloseUI;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main class for the JavaFX application.
 * It initializes the client and various views, and starts the application.
 */
public class Window extends Application {

    /** Starts the JavaFX application.
     *
     * @param stage the primary stage for this application
     * @throws Exception if an error occurs during initialization
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

    /** Main method to launch the JavaFX application (Front End).
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }



}
