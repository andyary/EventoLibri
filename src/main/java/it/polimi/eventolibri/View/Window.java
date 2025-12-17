package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Network.Client;
import javafx.application.Application;
import javafx.stage.Stage;

public class Window extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Client client = new Client();
        RegistraNewGenitore registraNewGenitore = new RegistraNewGenitore(client);
        LoginView loginView = new LoginView(client, registraNewGenitore);
        EventoView eventoView = new EventoView(client);
        EventoViewLettore eventoViewLettore = new EventoViewLettore(client);
        ProfiloGenitore profiloGenitore = new ProfiloGenitore(client);
        ProfiloLettore profiloLettore = new ProfiloLettore(client);
        HomeGenitore homeGenitore = new HomeGenitore(client, eventoView, profiloGenitore);
        HomeLettore homeLettore = new HomeLettore(client, eventoViewLettore, profiloLettore);


        try {
            client.start(loginView, homeGenitore, eventoView, homeLettore, eventoViewLettore, profiloGenitore, profiloLettore, registraNewGenitore);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        client.startListening();
        loginView.show(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }



}
