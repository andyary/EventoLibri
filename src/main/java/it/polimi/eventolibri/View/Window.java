package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Network.Client;
import javafx.application.Application;
import javafx.stage.Stage;

public class Window extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Client client = new Client();
        LoginView loginView = new LoginView(client);
        HomeGenitore homeGenitore = new HomeGenitore(client);
        EventoView eventoView = new EventoView(client);


        try {
            client.start(loginView, homeGenitore);
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
