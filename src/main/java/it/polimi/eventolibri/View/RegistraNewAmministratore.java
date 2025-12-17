package it.polimi.eventolibri.View;


import it.polimi.eventolibri.Message.RichiestaNuovoAmministratore;
import it.polimi.eventolibri.Model.*;
import it.polimi.eventolibri.Network.Client;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistraNewAmministratore {
    private final Client client;
    private Stage stage;
    private Label messaggioerrore;

    public RegistraNewAmministratore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
    }

    public void show(Stage stage, Runnable onBack) {
        this.stage = stage;
        // ===========================
        // TITOLO
        // ===========================
        Label title = new Label("Registra Nuovo Amministratore");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // ===========================
        // DATI NUOVO AMMINISTRATORE
        // ===========================
        VBox datiBox = new VBox(10);
        datiBox.setPadding(new Insets(10));
        TextField nomeField = new TextField("");
        TextField cognomeField = new TextField("");
        TextField usernameField = new TextField("");
        PasswordField pswField = new PasswordField();
        datiBox.getChildren().addAll(
                new Label("Nome:"),
                nomeField,
                new Label("Cognome:"),
                cognomeField,
                new Label("Username:"),
                usernameField,
                new Label("Password:"),
                pswField
        );
        Button salvaDati = new Button("Registra nuovo amministratore");
        salvaDati.setOnAction(e -> {
            if ((!nomeField.getText().isBlank()) && (!cognomeField.getText().isBlank()) && (!usernameField.getText().isBlank()) && (!pswField.getText().isBlank())) {
                this.messaggioerrore.setText("");
                CreaUtente<Amministratore> creaAmministratore = new CreaAmministratore();
                Amministratore amministratoreTemp = creaAmministratore.nuovoUtente(nomeField.getText(), cognomeField.getText(),
                        usernameField.getText());
                try {
                    client.sendMessage(new RichiestaNuovoAmministratore(amministratoreTemp, pswField.getText()));
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            } else {
                messaggioerrore.setText("Tutti i campi sono obbligatori.");
                messaggioerrore.setStyle("-fx-text-fill: red;");
            }
        });
        datiBox.getChildren().add(salvaDati);
        datiBox.getChildren().add(messaggioerrore);

        // ===========================
        // INDIETRO
        // ===========================
        Button back = new Button("Indietro");
        back.setOnAction(e -> onBack.run());
        HBox backBox = new HBox(back);
        backBox.setAlignment(Pos.TOP_RIGHT);
        backBox.setPadding(new Insets(10));
        // ===========================
        // LAYOUT COMPLETO
        // ===========================
        VBox contenuto = new VBox(25, backBox, title, datiBox);
        contenuto.setPadding(new Insets(20));
        ScrollPane scrollPane = new ScrollPane(contenuto);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(scrollPane, 700, 700);
        Platform.runLater(() -> {
            stage.setTitle("Registra Nuovo Amministratore");
            stage.setScene(scene);
            stage.show();
        });
        ;
    }

    public void mostraSuccesso(String messaggioerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setStyle("-fx-text-fill: green;");
            this.messaggioerrore.setText(messaggioerrore);
        });
    }

    public void mostraErrore(String messaggioerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setStyle("-fx-text-fill: red;");
            this.messaggioerrore.setText(messaggioerrore);
        });
    }

}
