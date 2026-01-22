package it.polimi.eventolibri.View;

import it.polimi.eventolibri.Message.RichiestaNuovoGenitore;
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

/**
 * Classe View per la registrazione di un nuovo genitore (non è richiesta autenticazione).
 */
public class RegistraNewGenitore {
    private final Client client;
    private Stage stage;
    private Label messaggioerrore;

    /**
     * Costruttore della classe RegistraNewGenitore.
     *
     * @param client l'istanza del client per la comunicazione con il server
     */
    public RegistraNewGenitore(Client client) {
        this.client = client;
        this.messaggioerrore = new Label("");
        this.messaggioerrore.setId("errorLabel"); // id per test
    }

    /**
     * Mostra la schermata di registrazione di un nuovo genitore.
     *
     * @param stage  lo stage principale dell'applicazione
     * @param onBack l'azione da eseguire quando si preme il pulsante "Indietro"
     */
    public void show(Stage stage, Runnable onBack) {
        this.stage = stage;
        this.messaggioerrore.setText("");
        // ===========================
        // TITOLO
        // ===========================
        Label title = new Label("Registra Nuovo Genitore");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        // ===========================
        // DATI NUOVO GENITORE
        // ===========================
        VBox datiBox = new VBox(10);
        datiBox.setPadding(new Insets(10));
        TextField nomeField = new TextField("");
        nomeField.setId("nomeField"); // id per test
        TextField cognomeField = new TextField("");
        cognomeField.setId("cognomeField"); // id per test
        TextField usernameField = new TextField("");
        usernameField.setId("usernameField"); // id per test
        PasswordField pswField = new PasswordField();
        pswField.setId("pswField"); // id per test
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
        Button salvaDati = new Button("Registra nuovo genitore");
        salvaDati.setId("saveButton"); // id per test
        salvaDati.setOnAction(e -> {
            if ((!nomeField.getText().isBlank()) && (!cognomeField.getText().isBlank()) && (!usernameField.getText().isBlank()) && (!pswField.getText().isBlank())) {
                this.messaggioerrore.setText("");
                CreaUtente<Genitore> creaGenitore = new CreaGenitore();
                Genitore genitoreTemp = creaGenitore.nuovoUtente(nomeField.getText(), cognomeField.getText(), usernameField.getText());
                try {
                    client.sendMessage(new RichiestaNuovoGenitore(genitoreTemp, pswField.getText()));
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
        back.setId("backButton"); // id per test
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
        Scene scene = new Scene(scrollPane,  750, 780);
        Platform.runLater(() -> {
            stage.setTitle("Registra Nuovo Genitore");
            stage.setScene(scene);
            stage.show();
        });
        ;
    }

    /**
     * Mostra un messaggio di successo nella schermata.
     *
     * @param messaggioerrore il messaggio di successo da visualizzare
     */
    public void mostraSuccesso(String messaggioerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setStyle("-fx-text-fill: green;");
            this.messaggioerrore.setText(messaggioerrore);
        });
    }

    /**
     * Mostra un messaggio di errore nella schermata.
     *
     * @param messaggioerrore il messaggio di errore da visualizzare
     */
    public void mostraErrore(String messaggioerrore) {
        Platform.runLater(()->{
            this.messaggioerrore.setStyle("-fx-text-fill: red;");
            this.messaggioerrore.setText(messaggioerrore);
        });
    }

}
